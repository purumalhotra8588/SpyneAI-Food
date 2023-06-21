package com.spyneai.onboardingv2.ui.intro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.dashboard.ui.enable
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentChooseCategoryBinding
import com.spyneai.model.CategoryData
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboarding.data.viewmodels.OnBoardingViewModel
import com.spyneai.onboardingv2.ui.intro.adapter.CategoriesAdapterV2
import kotlinx.coroutines.launch


class ChooseCategoryFragment : BaseFragment<OnBoardingViewModel, FragmentChooseCategoryBinding>(),
    OnItemClickListener {

    private var currentSelectedItem: CategoryData? = null
    private lateinit var catAdapter: CategoriesAdapterV2
    private var selectedCategory: String = ""
    private var companyName : String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
        companyName = bundle?.getString("countryName")?:""

        fetchCategorybyEnterprise()
        observeFetchCategory()

        binding.btnGetStarted.enable(false)

        binding.btnGetStarted.setOnClickListener {
           if (selectedCategory.isNullOrEmpty())
               Toast.makeText(requireContext(), "Please choose a category", Toast.LENGTH_SHORT).show()
            else
                goToHome()
        }
    }

    private fun goToHome() {
        getCategory()

        viewModel.categoryResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    viewModel.saveData(it.value.data)

                    var orientation = ""

                    it.value.data.forEach {
                        if (it.categoryId == selectedCategory)
                            orientation = it.orientation
                        Utilities.saveBool(requireContext(), AppConstants.SHOW_MARKETPLACES, it.show_marketplaces)
                    }

                    //get selected item value
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.SELECTED_CATEGORY_ID,
                        selectedCategory
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.SELECTED_CATEGORY_ORIENTATION,
                        orientation
                    )
//                        if(Utilities.getPreference(requireContext(),AppConstants.EMAIL_ID_REG_).equals("")) {

                        if(Utilities.getBool(requireContext(),AppConstants.NEW_ENTERPRISE_USER,false)){
//                            startActivity(Intent(requireContext(), MainV2Activity::class.java))
                            startActivity(Intent(requireContext(), MainDashboardActivity::class.java))
                        }else{
                            startActivity(Intent(requireContext(), MainDashboardActivity::class.java))
                        }
                    requireActivity().finishAffinity()
//                        }else{
//                            createEnterPrise()
//                        }

                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getCategory() }
                }
                else -> {

                }
            }
        }
    }

    private fun createEnterPrise() {
        val map = HashMap<String,String>()
        map["name"] = Utilities.getPreference(requireContext(),AppConstants.COMPANY_NAME)?:""
        map["category"] = currentSelectedItem?.categoryName?:""
        lifecycleScope.launch {
            viewModel.createEnterPrise(map)
        }
        viewModel.createEnterPrise.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    changeRole(it.value.data?.enterpriseId)
                }
                is Resource.Failure ->{
                    Utilities.hideProgressDialog()
                    handleApiError(it) { createEnterPrise() }
                }
                else -> {

                }
            }
        }

    }

    private fun changeRole(enterpriseId: String?) {
        lifecycleScope.launch {
            val map = HashMap<String,Any>()
            map["email"] = Utilities.getPreference(requireContext(),AppConstants.EMAIL_ID)?:""
            map["enterprise_id"] = enterpriseId?:""
            map["role"] = "admin"
            map["new_user"] = 1
            viewModel.createAdmin(map)
            viewModel.changeAdmin.observe(viewLifecycleOwner){
                when(it){
                    is Resource.Success ->{
                        if(Utilities.getBool(requireContext(),AppConstants.NEW_ENTERPRISE_USER,false)){
                            startActivity(Intent(requireContext(), MainDashboardActivity::class.java))
                        }else {
                            startActivity(
                                Intent(
                                    requireContext(),
                                    MainDashboardActivity::class.java
                                )
                            )
                        }
                    }
                    is Resource.Failure ->{
                        Utilities.hideProgressDialog()
                        handleApiError(it) {createEnterPrise()}
                    }
                    else -> {

                    }
                }
            }
        }
    }

    private fun getCategory() {
        Utilities.showProgressDialog(requireContext())
        viewModel.getCategoryData(selectedCategory)
    }

    private fun fetchCategorybyEnterprise() {
        Utilities.showProgressDialog(requireContext())

        viewModel.fetchCategoryByEnterprise()
    }

    private fun observeFetchCategory(){
        viewModel.fetchCategory.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    val categoryList = it.value.data.categoryData

                    if (categoryList.size > 1){
                        binding.tvType.isVisible = true
                        binding.tvChoose.isVisible = true

                        catAdapter = CategoriesAdapterV2(categoryList, this)

                        binding.rvCategories.apply {
                            layoutManager =
                                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
                            adapter = catAdapter
                        }
                    }else {
                        currentSelectedItem = categoryList[0]
                        selectedCategory = categoryList[0].categoryId
                        goToHome()
                    }
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { fetchCategorybyEnterprise() }
                }
                else -> {

                }
            }
        }
    }

    override fun getViewModel() = OnBoardingViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentChooseCategoryBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when (data) {
            is CategoryData -> {
                //unselect last selected item
                currentSelectedItem?.let {
                    it.isSelected = false
                }

                data.isSelected = true
                currentSelectedItem = data

                selectedCategory = data.categoryId

                catAdapter?.notifyDataSetChanged()

                binding.btnGetStarted.apply {
                    this.setTextColor(Color.WHITE)
                    this.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_gradient_button)
                    this.enable(true)
                }
            }

        }
    }
}