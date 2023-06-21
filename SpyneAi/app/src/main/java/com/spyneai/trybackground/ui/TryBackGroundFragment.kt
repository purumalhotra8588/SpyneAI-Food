package com.spyneai.trybackground.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.repository.model.GetGCPUrlRes
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentTryBackgroundBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.permissions.Permission
import com.spyneai.permissions.PermissionManager
import com.spyneai.shootapp.data.model.CarsBackgroundRes
import com.spyneai.shootapp.ui.dialogs.ShootExitDialog
import com.spyneai.singleimageprocessing.data.SingleImageDownloadingService
import com.spyneai.singleimageprocessing.data.SingleImageViewModel
import com.spyneai.singleimageprocessing.ui.SingleImageClickActivity
import com.spyneai.singleimageprocessing.ui.SingleImageClickPortraitActivity
import com.spyneai.singleimageprocessing.ui.SingleImageConfirmationDialog


class TryBackGroundFragment : BaseFragment<SingleImageViewModel, FragmentTryBackgroundBinding>(),
    OnItemClickListener, PickiTCallbacks {

    private val permissionManager = PermissionManager.from(this)
    var pickIt: PickiT? = null
    var filePath = ""
    var backgroundId = ""

    private var imageUrl: String? = null
    private var tryBackgroundAdapter: TryBackgroundAdapter? = null
    private var currentSelectedItem: CarsBackgroundRes.BackgroundApp? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(AppConstants.IMAGE_URL)?.let {
            if (it != "null")
                imageUrl = it
        }

        arguments?.getString(AppConstants.BACKGROUND_ID)?.let {
            if (it != "null")
                backgroundId = it
        }

        if (imageUrl != null) {
            //set upload data
            viewModel.uploadData = GetGCPUrlRes.Data(
                fileUrl = arguments?.getString(AppConstants.UPLOAD_URL).toString(),
                presignedUrl = "",
                projectId = arguments?.getString(AppConstants.PROJECT_ID).toString(),
                skuId = arguments?.getString(AppConstants.SKU_ID).toString(),
            )
        }

        pickIt = PickiT(requireContext(), this, requireActivity())

        getBackgrounds()

        binding.ivUpload.setOnClickListener {
            permissionManager
                .request(Permission.Storage)
                .rationale("We need storage permission to select image")
                .checkDetailedPermission { result: Map<Permission, Boolean> ->
                    if (result.all { it.value }) {
                        selectImageFromFiles()
                    } else {
                        Toast.makeText(requireContext(), "Permission Denied!", Toast.LENGTH_LONG)
                            .show()
                    }
                }
        }
        binding.ivBack!!.setOnClickListener {
            ShootExitDialog().show(requireActivity().supportFragmentManager, "ShootExitDialog")
        }


        binding.ivShoot.setOnClickListener {
            permissionManager
                .request(Permission.Camera)
                .rationale("We need permission to shoot image with camera")
                .checkPermission { granted: Boolean ->
                    if (granted) {
                        viewModel.getOrientation()?.observe(viewLifecycleOwner) {
                            if (it == "landscape")
                                startActivity(
                                    Intent(
                                        requireContext(),
                                        SingleImageClickActivity::class.java
                                    ).apply {
                                        putExtra(AppConstants.PROCESS_IMAGE, true)
                                        putExtra(AppConstants.BACKGROUND_ID, backgroundId)
                                    }
                                )
                            else
                                startActivity(
                                    Intent(
                                        requireContext(),
                                        SingleImageClickPortraitActivity::class.java
                                    )
                                )
                        }
                    } else {
                        Toast.makeText(requireContext(), "Permission Denied!", Toast.LENGTH_LONG)
                            .show()
                    }
                }
        }

        binding.btnDownload.setOnClickListener {
            val imageList = ArrayList<String>()
            val nameList = ArrayList<String>()
            imageList.add(viewModel.outputUrl)
            nameList.add(System.currentTimeMillis().toString())

            var imageDownloadingServiceIntent = Intent(
                requireContext(),
                SingleImageDownloadingService::class.java
            )
            imageDownloadingServiceIntent.action = "START"
            imageDownloadingServiceIntent.putExtra(AppConstants.LIST_HD_QUALITY, imageList)
            imageDownloadingServiceIntent.putExtra(AppConstants.LIST_IMAGE_NAME, nameList)
            ContextCompat.startForegroundService(requireContext(), imageDownloadingServiceIntent)

            Toast.makeText(requireContext(), "Downloading Started...", Toast.LENGTH_LONG).show()
        }

        viewModel.imageProcessed.observe(viewLifecycleOwner) {
            if (it) {
                imageUrl = viewModel.outputUrl
                loadImage(viewModel.outputUrl)
            }
        }
    }

    private fun selectImageFromFiles() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startForResult.launch(intent)
    }

    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.data
                pickIt?.getPath(data, Build.VERSION.SDK_INT)
            }
        }

    private fun getBackgrounds() {
        val list = ArrayList<CarsBackgroundRes.BackgroundApp>()

        //get backgrounds
        viewModel.getBackgroundsHome()?.let {
            list.addAll(it)
        }

        if (imageUrl == null) {
            list[0].isSelected = true
            chooseBackground(list[0])
            currentSelectedItem = list[0]
        } else {
            loadImage(imageUrl!!)
            list.forEach {
                if (it.imageId == arguments?.getString(AppConstants.BACKGROUND_ID)) {
                    it.isSelected = true
                    chooseBackground(it)
                    currentSelectedItem = it
                }
            }
        }


        tryBackgroundAdapter = TryBackgroundAdapter(list, this)

        binding.rvImages.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)
            adapter = tryBackgroundAdapter
        }

    }

    private fun chooseBackground(data: CarsBackgroundRes.BackgroundApp) {
        backgroundId = data.imageId

        if (imageUrl == null) {
            data.imageUrl?.let { loadImage(it) }
        } else {
            //enable download option
            binding.btnDownload.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_gradient_button)
            binding.btnDownload.isClickable = true
            binding.btnDownload.setTextColor(Color.WHITE)

            if (data.imageId == arguments?.getString(AppConstants.BACKGROUND_ID).toString()) {
                loadImage(imageUrl!!)
            } else {
                processImage(viewModel.uploadData!!)
                observeProcessImage(viewModel.uploadData!!)
            }
        }
    }

    private fun processImage(data: GetGCPUrlRes.Data) {
        Utilities.showProgressDialog(requireContext())
        val processDataMap = HashMap<String, Any>()

        processDataMap.apply {
            put("prod_cat_id", viewModel.category?.categoryId!!)
            put("prod_sub_cat_id", arguments?.getString(AppConstants.SUB_CAT_ID).toString())
            put("image_category", viewModel.category?.imageCategories?.get(0)!!)
            put("project_id", data.projectId)
            put("sku_id", data.skuId)
            put("image_url", data.fileUrl)
            put("background_id", backgroundId)
            put("source", "App_android_single")
            put("image_name", "demo_image.jpeg")
            put("angle", 0)

        }
        viewModel.processImage(processDataMap)
    }

    private fun observeProcessImage(data: GetGCPUrlRes.Data) {
        viewModel.processSingleImage.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    viewModel.outputUrl = it.value.data.outputImage
                    loadImage(it.value.data.outputImage)
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) {
                        processImage(data)
                    }
                }
                else -> {

                }
            }
        }
    }

    private fun loadImage(lowResImageUrl: String) {
        binding.progressBar.isVisible = true
        binding.ivSelected.settings.javaScriptEnabled = true
        binding.ivSelected.settings.loadWithOverviewMode = true
        binding.ivSelected.settings.useWideViewPort = true
        binding.ivSelected.loadUrl(lowResImageUrl)

        binding.progressBar.isVisible = false

    }


    override fun getViewModel() = SingleImageViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTryBackgroundBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when (data) {
            is CarsBackgroundRes.BackgroundApp -> {
                //unselect last selected item
                currentSelectedItem?.let {
                    it.isSelected = false
                }

                data.isSelected = true
                currentSelectedItem = data

                chooseBackground(data)

                tryBackgroundAdapter?.notifyDataSetChanged()
            }

        }
    }

    override fun PickiTonUriReturned() {
        Utilities.showProgressDialog(requireContext())

    }

    override fun PickiTonStartListener() {

    }

    override fun PickiTonProgressUpdate(progress: Int) {

    }

    override fun PickiTonCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        Utilities.hideProgressDialog()
        filePath = path!!
        uploadImage(filePath)
    }

    private fun uploadImage(filePath: String) {
        val bundle = Bundle().apply {
            putString("captured_image", filePath)
            putString("cta", "Reselect")
            putBoolean(AppConstants.FROM_SELECTION, true)
            putBoolean(AppConstants.PROCESS_IMAGE, true)
            putString(AppConstants.BACKGROUND_ID, backgroundId)
            putString(AppConstants.IMAGE_NAME, "demo_image.jpeg")
        }

        val fragment = SingleImageConfirmationDialog()
        fragment.arguments = bundle

        fragment.show(requireActivity().supportFragmentManager, "SingleImageConfirmationDialog")
    }
}