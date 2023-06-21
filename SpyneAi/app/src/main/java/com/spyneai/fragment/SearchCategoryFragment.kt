package com.spyneai.onboardingv2.ui.intro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.databinding.SearchCategoryLayoutBinding
import com.spyneai.homev12.adapter.SubCategoryAdapter
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.ui.base.ShootPortraitActivity

class SearchCategoryFragment : BaseFragment<ShootViewModelApp, SearchCategoryLayoutBinding>(),
    SubCategoryAdapter.BtnClickListener {

    private lateinit var marketplaceAdapter: SubCategoryAdapter

    private var subcatList: ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2> = ArrayList()

    private var filteredList: ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2> =
        ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and adapter
        marketplaceAdapter = SubCategoryAdapter(
            requireContext(),
            filteredList,
            object : SubCategoryAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    val intent =  Intent(
                        requireContext(), ShootPortraitActivity::class.java
                    )

                    intent.apply {
                        putExtra(AppConstants.CATEGORY_ID, AppConstants.ECOM_CATEGORY_ID)
                        putExtra(AppConstants.ORIENTATION, "portrait")
                        putExtra(AppConstants.CATEGORY_NAME, "Ecom")
                        putExtra(AppConstants.SUB_CAT_ID, subcatList[position].prodSubCatId)
                        startActivity(intent)
                    }
                }
            })

        binding.rvMarketplace.apply {
            layoutManager = GridLayoutManager(
                requireContext(), 4, GridLayoutManager.VERTICAL,
                false
            )
            adapter = marketplaceAdapter
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        viewModel.getSubcategoriesV2(
            Utilities.getPreference(
                BaseApplication.getContext(),
                AppConstants.SELECTED_CATEGORY_ID
            ).toString()
        )?.observe(viewLifecycleOwner) {
            it?.let {
                subcatList = it as ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>
                filteredList.addAll(subcatList)
                marketplaceAdapter.subCatList = filteredList
                marketplaceAdapter.notifyDataSetChanged()
            }
        }


        // Setup search view for filtering list
        binding.searchview.setOnQueryTextListener(object :
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { text ->
                    val newFilteredList = subcatList.filter { it.subCatName.contains(text, true) }

                    if (newFilteredList.isEmpty()) {
                        val other = subcatList.find { it.subCatName.contains("others", true) }

                        other?.let {
                            val otherList =
                                ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>()
                            otherList.add(it)
                            marketplaceAdapter.subCatList = otherList
                            marketplaceAdapter.notifyDataSetChanged()
                        }
                    } else {
                        newFilteredList?.let {
                            marketplaceAdapter.subCatList =
                                it as ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>
                            marketplaceAdapter.notifyDataSetChanged()
                        }
                    }
                }
                return true
            }
        })
    }


    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = SearchCategoryLayoutBinding.inflate(inflater, container, false)

    override fun onBtnClick(position: Int) {
        // Handle marketplace item click event
    }
}
