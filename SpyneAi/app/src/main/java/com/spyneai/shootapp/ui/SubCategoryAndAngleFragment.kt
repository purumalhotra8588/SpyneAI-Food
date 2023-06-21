package com.spyneai.shootapp.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.posthog.captureEvent
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.databinding.FragmentSelectSubcategoryAndAngleBinding
import com.spyneai.posthog.Events
import com.spyneai.shootapp.adapters.SubcatAndAngleAdapter
import com.spyneai.shootapp.data.ShootViewModelApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.spyneai.needs.AppConstants
import kotlinx.coroutines.DelicateCoroutinesApi


class SubCategoryAndAngleFragment :
    BaseFragment<ShootViewModelApp, FragmentSelectSubcategoryAndAngleBinding>(),
    OnItemClickListener {


    var subcatAndAngleAdapter: SubcatAndAngleAdapter? = null
    var subCatId : String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (arguments?.getString(AppConstants.SUB_CAT_ID) != null ) {
            if(arguments?.getString(AppConstants.SUB_CAT_ID) != "subcategoryId"){
                subCatId = arguments?.getString(AppConstants.SUB_CAT_ID)
                isSubCatSlected()
            }else{
                binding.tvDescription.text = "Select your ${viewModel.category?.name}'s Sub-categories"

                viewModel.getSubCategories.observe(viewLifecycleOwner) {
                    binding.clRoot.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.subcat_selection
                        )
                    )
                    binding.clRoot.isClickable = true
                    binding.clRoot.isFocusable = true
                    binding.clRoot.isFocusableInTouchMode = true

                    binding.apply {
                        rv.isVisible = true
                        ivArrow?.isVisible = true
                        tvDescription.isVisible = true
                    }

                    getSubcategories()
                }

                observeSubcategories()

            }

        } else {

            binding.tvDescription.text = "Select your ${viewModel.category?.name}'s Sub-categories"

            viewModel.getSubCategories.observe(viewLifecycleOwner) {
                binding.clRoot.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.subcat_selection
                    )
                )
                binding.apply {
                    rv.isVisible = true
                    ivArrow?.isVisible = true
                    tvDescription.isVisible = true
                }

                getSubcategories()
            }

            observeSubcategories()

        }

//        if (viewModel.isSkuCreated.value == null || viewModel.category?.orientation == "landscape"
//        ) {
//            viewModel.getSubCategories.observe(viewLifecycleOwner, {
//                binding.clRoot.setBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.subcat_selection
//                    )
//                )
//                getSubcategories()
//            })
//
//            observeSubcategories()
//        } else {
//            hideViews()
//        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun isSubCatSlected(){
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.setSubcategoryData(subCatId.toString())

            GlobalScope.launch(Dispatchers.Main) {
                hideViews()

                onSubCategorySelection()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.isSubcategoriesSelectionShown = true
    }

    private fun getSubcategories() {
        binding.shimmer.startShimmer()

    }

    fun observeSubcategories() {
        viewModel.getSubcategoriesV2()?.observe(viewLifecycleOwner){

            requireContext().captureEvent(
                Events.GET_SUBCATEGORIES,
                HashMap<String, Any?>()
            )

            binding.apply {
                shimmer.stopShimmer()
                shimmer.visibility = View.INVISIBLE
            }

            subcatAndAngleAdapter = SubcatAndAngleAdapter(it, this)

            val lyManager =
                if (requireActivity().requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                else
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.VERTICAL,
                        false
                    )

            binding.rv.apply {
                layoutManager = lyManager
                adapter = subcatAndAngleAdapter
            }
        }
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectSubcategoryAndAngleBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when (data) {
            is CatAgnosticResV2.CategoryAgnos.SubCategoryV2 -> {
                hideViews()

                viewModel.subcategoryV2 = data

               onSubCategorySelection()
            }
        }
    }

    private fun onSubCategorySelection() {
        viewModel.category?.shootExperience?.let {
            if (it.frames.isNullOrEmpty()){
                viewModel.exterirorAngles.value = 0
                updateSku()
            }else{
                if (it.frames.size > 1)
                    selectAngles()
                else{
                    viewModel.exterirorAngles.value = it.frames[0]
                    updateSku()
                }
            }
        }
    }

    private fun updateSku() {
        setSubcategoryData()

        var selectedSubCatId= viewModel.skuApp?.subcategoryId

        viewModel.category?.subCategoryV2s?.let { subcategories ->
            subcategories.forEach { subcategory ->
                if (subcategory.prodSubCatId==selectedSubCatId){
                    if(!subcategory.overlayApps.isNullOrEmpty())
                        viewModel.replaceFragment.value=true
                }
            }
        }

        viewModel.isSubCategoryConfirmed.value = true
        viewModel.isSkuCreated.value = true
        viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
        viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
        viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

        //add sku to local database
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.updateSubcategory()

        }
    }

    private fun hideViews() {
        binding.clRoot.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.transparent
            )
        )

        binding.clRoot.isClickable = false
        binding.clRoot.isFocusable = false
        binding.clRoot.isFocusableInTouchMode = false

        binding.apply {
            shimmer.stopShimmer()
            shimmer.visibility = View.INVISIBLE
            ivArrow?.visibility = View.GONE
            tvDescription.visibility = View.INVISIBLE
            rv.visibility = View.INVISIBLE
        }
    }

    private fun selectAngles() {
        setSubcategoryData()
        
    }

    private fun setSubcategoryData(){
        viewModel.skuApp?.apply {
            subcategoryName = viewModel.subcategoryV2?.subCatName
            subcategoryId = viewModel.subcategoryV2?.prodSubCatId
            initialFrames = viewModel.exterirorAngles.value
            totalFrames = viewModel.exterirorAngles.value
        }
    }
}