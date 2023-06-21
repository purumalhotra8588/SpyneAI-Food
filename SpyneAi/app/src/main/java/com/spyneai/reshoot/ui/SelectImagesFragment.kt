package com.spyneai.reshoot.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.enable
import com.spyneai.databinding.FragmentSelectImagesBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.processedimages.data.ProcessedViewModelApp
import com.spyneai.reshoot.SelectImageAdapter
import com.spyneai.reshoot.data.ReshootImage
import com.spyneai.reshoot.data.SelectedImagesHelper
import com.spyneai.shootapp.repository.model.image.Image


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SelectImagesFragment : BaseFragment<ProcessedViewModelApp,FragmentSelectImagesBinding>(),OnItemClickListener{

    private var selectImageAdapter : SelectImageAdapter? = null

    private val permissions = mutableListOf(
        Manifest.permission.CAMERA,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            add(Manifest.permission.READ_MEDIA_IMAGES)
        }else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }
    }

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            onPermissionGranted()
        } else {
            Toast.makeText(requireContext(), R.string.message_no_permissions, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getImages()

        binding.ivBackShowImages.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnReshoot.setOnClickListener {
            if (allPermissionsGranted()) {
                onPermissionGranted()
            } else {
                permissionRequest.launch(permissions.toTypedArray())
            }
        }
    }

    protected fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    open fun onPermissionGranted() {

        setCategoryData()
        observeCategoryData()
    }


    private fun setCategoryData(){
        Utilities.showProgressDialog(requireContext())
        viewModel.getCategoryDataV2(requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString())
    }

    private fun observeCategoryData() {
        viewModel.categoryResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    val list = selectImageAdapter?.listItems as ArrayList<Image>

                    val selectedList = list.filter {
                        it.isSelected
                    } as ArrayList<Image>


                    var selectedSubCatId=""
                    var overlayListSize=0


                    GlobalScope.launch(Dispatchers.IO) {
                       selectedSubCatId= viewModel.getSubCatIDBySku(selectedList[0].skuId.toString())

                        GlobalScope.launch(Dispatchers.Main) {
                                if(it.value.data.isNotEmpty()) {
                                    val subCategoryV2s = it.value.data[0].subCategoryV2s?.find {
                                        it.prodSubCatId == selectedSubCatId
                                    }
                                    overlayListSize= subCategoryV2s?.overlayApps?.size ?: 0

                                    var reshootIntent: Intent? = null

                                    when (viewModel.categoryId) {


                                        AppConstants.ECOM_CATEGORY_ID,
                                        AppConstants.PHOTO_BOX_CATEGORY_ID -> {

                                            val selectedIdsMap = HashMap<Int, ReshootImage>()



                                            if(overlayListSize>0){
                                                selectedList.forEachIndexed { _, data ->
                                                    selectedIdsMap[data.overlayId.toInt()] =
                                                        ReshootImage(data.name, data.sequence)
                                                }
                                                SelectedImagesHelper.selectedOverlayIds = selectedIdsMap
                                            }else {
                                                selectedList.forEach {
                                                    it.isSelected = false
                                                }
                                                SelectedImagesHelper.selectedImages = selectedList
                                            }

                                            reshootIntent =
                                                Intent(requireActivity(), ReshootPortraitActivity::class.java)
                                        }
                                        AppConstants.FOOD_AND_BEV_CATEGORY_ID -> {
                                            val selectedIdsMap = HashMap<Int, ReshootImage>()

                                            selectedList.forEachIndexed { _, data ->
                                                selectedIdsMap[data.overlayId.toInt()] =
                                                    ReshootImage(data.name, data.sequence)
                                            }

                                            SelectedImagesHelper.selectedOverlayIds = selectedIdsMap
                                            reshootIntent =
                                                Intent(requireActivity(), ReshootPortraitActivity::class.java)

                                        }
                                        else -> {
                                            val selectedIdsMap = HashMap<Int, ReshootImage>()

                                            selectedList.forEachIndexed { _, data ->
                                                selectedIdsMap[data.overlayId.toInt()] =
                                                    ReshootImage(data.name, data.sequence)
                                            }

                                            SelectedImagesHelper.selectedOverlayIds = selectedIdsMap
                                            reshootIntent =
                                                Intent(requireActivity(), ReshootPortraitActivity::class.java)
                                        }
                                    }
                                    reshootIntent.apply {
                                        putExtra(AppConstants.PROJECT_ID, viewModel.projectId)
                                        putExtra(AppConstants.PROJECT_UUIID, viewModel.projectUuid)
                                        putExtra(AppConstants.SKU_UUID, viewModel.skuUuid)
                                        putExtra(AppConstants.SKU_ID, viewModel.skuId)
                                        putExtra(AppConstants.SKU_NAME, viewModel.skuName)
                                        putExtra(AppConstants.CATEGORY_ID, requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID))
                                        putExtra(
                                            AppConstants.CATEGORY_NAME,
                                            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_NAME)
                                        )
                                        putExtra(
                                            AppConstants.SUB_CAT_ID,
                                            requireActivity().intent.getStringExtra(AppConstants.SUB_CAT_ID)
                                        )
                                        putExtra(
                                            AppConstants.EXTERIOR_ANGLES,
                                            requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0)
                                        )
                                        startActivity(this)
                                    }
                                }
                        }
                    }

                }
                else -> {

                }
            }
        }
    }

    private fun getImages() {
        try {
            val imagesResponse = (viewModel.imagesOfSkuRes.value as Resource.Success).value
            selectImageAdapter = SelectImageAdapter(imagesResponse.data,this)




            imagesResponse.data.forEach { list ->
                if (list.qcStatus == "reshoot"){
                    list.isSelected = true
                    binding.btnReshoot.enable(true)
                    binding.btnReshoot.visibility = View.VISIBLE
                }
            }

            binding.rvSkuImages.apply {
                layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
                adapter = selectImageAdapter
            }
        }catch (e : Exception){
        }

    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when(data){
            is Image -> {
                if(!Utilities.getBool(BaseApplication.getContext(),AppConstants.CHECK_QC,false)) {
                    data.isSelected = !data.isSelected
                    selectImageAdapter?.notifyItemChanged(position)

                }

                val list = selectImageAdapter?.listItems as ArrayList<Image>

                val selectedList = list.filter {
                    it.isSelected
                }

                if(!Utilities.getBool(BaseApplication.getContext(),AppConstants.CHECK_QC,false)) {
                    if (selectedList.isNullOrEmpty()) {
                        binding.btnReshoot.text = getString(R.string.no_reshoot)
                        binding.btnReshoot.enable(false)
                    } else {
                        binding.btnReshoot.text =
                            getString(R.string.no_reshoot) + " " + selectedList.size + " Angles"
                        binding.btnReshoot.enable(true)
                    }
                }
            }
        }
    }
    override fun getViewModel() = ProcessedViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectImagesBinding.inflate(inflater, container, false)


}