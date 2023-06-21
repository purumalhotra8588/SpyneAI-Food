package com.spyneai.reshoot.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentReshootRequestBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.reshoot.ReshootSkuAdapter
import com.spyneai.reshoot.data.ReshootImage
import com.spyneai.reshoot.data.ReshootSkuRes
import com.spyneai.reshoot.data.SelectedImagesHelper
import com.spyneai.shootapp.repository.model.image.Image

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReshootRequestFragment : BaseFragment<MyOrdersViewModel, FragmentReshootRequestBinding>(),
    OnItemClickListener {

    val TAG = "ReshootRequestFragment"
    var categoryId: String = ""
    var skuId: String = ""
    var list = ArrayList<Image>()
    var selectedSubCatId=""
    var overlayListSize=0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getReshootSkus()

        viewModel.reshootRequestSkuRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    binding.rvReshootRequests.apply {
                        layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        adapter = ReshootSkuAdapter(it.value.data, this@ReshootRequestFragment)
                    }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getReshootSkus() }
                }
                else -> {}
            }
        }

        viewModel.onReshootDataSynced.observe(viewLifecycleOwner) {
            setCategoryData()
            observeCategoryData()

        }

    }

    private fun startReshoot() {
        list.clear()

        GlobalScope.launch(Dispatchers.IO) {
            val sku = viewModel.getSku(skuId)

            sku.let { sku ->
                val selectedList = sku.images

                selectedList?.let { images ->
                    var reshootIntent : Intent? = null


                    when(categoryId){
                        AppConstants.ECOM_CATEGORY_ID,
                        AppConstants.PHOTO_BOX_CATEGORY_ID-> {
                            val selectedIdsMap = HashMap<Int, ReshootImage>()
                            if(selectedList[0].overlayId.toInt()>10){
                                selectedList.forEachIndexed { _, data ->
                                    selectedIdsMap[data.overlayId.toInt()] =
                                        ReshootImage(data.name, data.sequence)
                                }
                                SelectedImagesHelper.selectedOverlayIds = selectedIdsMap
                            }else {
                                selectedList.forEach {
                                    it.isSelected = false
                                }
                                SelectedImagesHelper.selectedImages = selectedList as ArrayList<Image>
                            }

                            reshootIntent =
                                Intent(requireActivity(), ReshootPortraitActivity::class.java)

                        }


                        AppConstants.FOOD_AND_BEV_CATEGORY_ID->{
                            val selectedIdsMap = HashMap<Int, ReshootImage>()

                            selectedList.forEachIndexed { _, data ->
                                selectedIdsMap[data.overlayId.toInt()] = ReshootImage(data.name,data.sequence)
                            }

                            SelectedImagesHelper.selectedOverlayIds = selectedIdsMap
                            reshootIntent = Intent(requireActivity(),ReshootPortraitActivity::class.java)

                        }
                        else -> {
                            val selectedIdsMap = HashMap<Int, ReshootImage>()

                            selectedList.forEachIndexed { _, data ->
                                selectedIdsMap[data.overlayId.toInt()] = ReshootImage(data.name,data.sequence)
                            }

                            SelectedImagesHelper.selectedOverlayIds = selectedIdsMap
                            reshootIntent = Intent(requireActivity(),ReshootPortraitActivity::class.java)
                        }
                    }

                    reshootIntent.apply {
                        putExtra(AppConstants.PROJECT_ID,sku.projectId)
                        putExtra(AppConstants.PROJECT_UUIID,sku.projectUuid)
                        putExtra(AppConstants.SKU_UUID,sku.uuid)
                        putExtra(AppConstants.SKU_ID,sku.skuId)
                        putExtra(AppConstants.SKU_NAME,sku.skuName)
                        putExtra(AppConstants.CATEGORY_ID,categoryId)
                        putExtra(AppConstants.CATEGORY_NAME,sku.categoryName)
                        putExtra(AppConstants.SUB_CAT_ID,sku.subcategoryId)
                        putExtra(AppConstants.EXTERIOR_ANGLES,sku.initialFrames)
                        startActivity(this)
                    }
                }
            }
        }
    }

    private fun getReshootSkus() {
        Utilities.showProgressDialog(requireContext())
        viewModel.getReshootSkus()
    }

    override fun getViewModel() = MyOrdersViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReshootRequestBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when(data){
            is ReshootSkuRes.Data -> {
                if (!data.images.isNullOrEmpty()){
                    skuId = data.skuId
                    viewModel._reshootProjectRes.value = null
                    Utilities.showProgressDialog(requireContext())
                    viewModel.getReshootProjectData(data.projectId)
                    observeProjectRes(data.projectId)
                }else {
                    Toast.makeText(requireContext(),"No image to reshoot",Toast.LENGTH_LONG).show()
                }
            }

            is ReshootSkuRes.Data.Image -> {
                val bundle = Bundle()
                bundle.putString("image",data.outputImageHresUrl)

//                val dialog = DialogViewReshootImage()
//                dialog.arguments = bundle
//
//              dialog.show(requireActivity().supportFragmentManager, "DialogViewReshootImage")
            }
        }
    }

    private fun observeProjectRes(projectId: String) {
        viewModel.reshootProjectRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    it.value.data.projectAppData.categoryId?.let {
                        categoryId = it
                    }
                    viewModel.syncData(it.value.data)
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { viewModel.getReshootProjectData(projectId) }
                }
                Resource.Loading -> Utilities.showProgressDialog(requireContext())
            }
        }
    }

    private fun setCategoryData(){
//        Utilities.showProgressDialog(requireContext())
        viewModel.getCategoryDataV2(categoryId)
    }

    private fun observeCategoryData() {
        viewModel.categoryResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    GlobalScope.launch(Dispatchers.IO) {
                        selectedSubCatId =
                            viewModel.getSubCatIDBySku(skuId)

                        GlobalScope.launch(Dispatchers.Main) {
                            if (it.value.data.isNotEmpty()) {
                                val subCategoryV2s = it.value.data[0].subCategoryV2s?.find {
                                    it.prodSubCatId == selectedSubCatId
                                }
                                overlayListSize = subCategoryV2s?.overlayApps?.size ?: 0

                                startReshoot()
                            }
                        }
                    }
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it){
                        setCategoryData()
                    }
                }

                else -> {}
            }
        }
    }

}