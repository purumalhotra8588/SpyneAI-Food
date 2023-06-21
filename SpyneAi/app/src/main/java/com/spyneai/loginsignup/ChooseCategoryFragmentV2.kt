package com.spyneai.onboardingv2.ui.intro

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.carinspectionocr.viewmodel.RegistrationDataViewModel
import com.spyneai.dashboard.ui.enable
import com.spyneai.databinding.FragmentChooseCategory2Binding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.data.SelectCategoryRes
import com.spyneai.onboardingv2.ui.intro.adapter.CategoriesAdapterV3


class ChooseCategoryFragmentV2 :
    BaseFragment<RegistrationDataViewModel, FragmentChooseCategory2Binding>(),
    OnItemClickListener {

    private var currentSelectedItem: SelectCategoryRes.Data? = null
    private lateinit var catAdapter: CategoriesAdapterV3
    private var selectedCategory: String = ""
    private var companyName: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        Utilities.saveBool(
            requireContext(), AppConstants.NEW_LOGIN_API,
            false
        )

        val categoryData = resources.openRawResource(R.raw.select_categoires)
            .bufferedReader().use { it.readText() }

        var response = Gson().fromJson(categoryData, SelectCategoryRes::class.java)


        val list = response.data.sortedBy {
            it.priority
        }


        catAdapter = CategoriesAdapterV3(list, this)

        binding.rvCategories.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = catAdapter
        }

        binding.btnGetStarted.enable(false)

        binding.btnGetStarted.setOnClickListener {

            if (currentSelectedItem?.categoryName == "Automobile") {
                findNavController().navigate(
                    R.id.action_ChooseCategory_to_WebViewFragment
                )
            } else {
                findNavController().navigate(
                    R.id.action_ChooseCategory_to_nav_phone_number_fragment
                )
            }
        }


    }

    override fun getViewModel() = RegistrationDataViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentChooseCategory2Binding.inflate(inflater, container, false)

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemClick(view: View, position: Int, data: Any?) {
        when (data) {
            is SelectCategoryRes.Data -> {
                //unselect last selected item
                currentSelectedItem?.let {
                    it.isSelected = false
                }

                data.isSelected = true
                currentSelectedItem = data

                selectedCategory = data.categoryId

                Utilities.saveBool(requireContext(),AppConstants.NEW_LOGIN_API,data.categoryId == AppConstants.CARS_CATEGORY_ID)

                catAdapter.notifyDataSetChanged()

                binding.btnGetStarted.apply {
                    this.setTextColor(Color.WHITE)
                    this.isCheckable = true
                    this.enable(true)
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()

        Utilities.saveBool(
            requireContext(), AppConstants.NEW_LOGIN_API,
            false
        )

    }
}