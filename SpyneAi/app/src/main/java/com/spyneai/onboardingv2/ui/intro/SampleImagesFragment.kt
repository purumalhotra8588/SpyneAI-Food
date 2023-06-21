package com.spyneai.onboardingv2.ui.intro

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSampleImagesBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.data.SampleImagesRes
import com.spyneai.onboardingv2.ui.intro.adapter.SampleImagesAdapter
import com.spyneai.permissions.Permission
import com.spyneai.permissions.PermissionManager
import com.spyneai.singleimageprocessing.data.SingleImageViewModel
import com.spyneai.singleimageprocessing.ui.SingleImageClickActivity
import com.spyneai.singleimageprocessing.ui.SingleImageClickPortraitActivity
import com.spyneai.singleimageprocessing.ui.SingleImageConfirmationDialog

class SampleImagesFragment : BaseFragment<SingleImageViewModel, FragmentSampleImagesBinding>(),
    PickiTCallbacks, OnItemClickListener {

    private val permissionManager = PermissionManager.from(this)
    var pickIt: PickiT? = null
    var filePath = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pickIt = PickiT(requireContext(), this, requireActivity())

        setImages()

        binding.flUpload.setOnClickListener {
            permissionManager
                .request(Permission.Storage)
                .rationale("We need storage permission to select image")
                .checkDetailedPermission { result: Map<Permission, Boolean> ->
                    if (result.all { it.value }) {
                        selectImageFromFiles()
                    } else {
                        Toast.makeText(requireContext(),"Permission Denied!",Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.flShoot.setOnClickListener {
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
                                    )
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
                        Toast.makeText(requireContext(),"Permission Denied!",Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun setImages() {

        viewModel.getSampleImages()

        viewModel.sampleImagesRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.isVisible = false

                    val sampleImagesAdapter = SampleImagesAdapter(it.value.data.sampleImages, this)

                    binding.rvImages.apply {
                        layoutManager = GridLayoutManager(
                            requireContext(),
                            2,
                            GridLayoutManager.VERTICAL,
                            false
                        )
                        adapter = sampleImagesAdapter
                    }
                }

                is Resource.Failure -> {
                    binding.progressBar.isVisible = false
                    handleApiError(it) { viewModel.getSampleImages() }
                }
                else -> {

                }
            }
        }
    }

    private fun selectImageFromFiles() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startForResult.launch(intent)
    }

    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.data
            pickIt?.getPath(data, Build.VERSION.SDK_INT)
        }
    }

    override fun getViewModel() = SingleImageViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSampleImagesBinding.inflate(inflater,container,false)

    override fun PickiTonUriReturned() {

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
        filePath = path!!
        uploadImage(filePath)
    }

    private fun uploadImage(filePath: String) {
        val bundle = Bundle().apply {
            putString("captured_image",filePath)
            putString("cta","Reselect")
            putBoolean(AppConstants.FROM_SELECTION,true)
        }

        val fragment = SingleImageConfirmationDialog()
        fragment.arguments = bundle

        fragment.show(requireActivity().supportFragmentManager,"SingleImageConfirmationDialog")
    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
            when(data){
                is SampleImagesRes.Data.SampleImage -> {
                    //start processing image activity
                    createProjectAndSkuId()
                    observeCreateProjectAndSku(data)
                }
            }
    }

    private fun observeCreateProjectAndSku(data: SampleImagesRes.Data.SampleImage) {
        viewModel.gcpUrlResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    Intent(requireContext(), ImageProcessingActivity::class.java)
                        .apply {
                            putExtra("selected_image", data.inputImage)
                            putExtra(AppConstants.SUB_CAT_ID, viewModel.subcategoryV2?.prodSubCatId)
                            putExtra(AppConstants.PROJECT_ID, it.value.data.projectId)
                            putExtra(AppConstants.SKU_ID, it.value.data.skuId)
                            putExtra(AppConstants.IMAGE_URL, data.inputImage)
                            putExtra(AppConstants.IMAGE_NAME, "demo_image.jpeg")
                            putExtra(
                                AppConstants.IMAGE_ANGLE,
                                arguments?.getInt(AppConstants.IMAGE_ANGLE, 0)
                            )
                            startActivity(this)
                        }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { createProjectAndSkuId() }
                }

                else -> {

                }
            }
        }
    }

    private fun createProjectAndSkuId() {
        Utilities.showProgressDialog(requireContext())
        viewModel.getPreSignedUrl("demo_image.jpeg")
    }
}



















