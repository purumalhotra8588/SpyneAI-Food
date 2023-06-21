package com.spyneai.dashboard.ui

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.activity.SearchCategoryActivity
import com.spyneai.activity.SubCategorySearchAdapter
import com.spyneai.adapter.BannerAdapter
import com.spyneai.adapter.CategoryAdapter
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.databinding.FragmentHomeV12Binding
import com.spyneai.handleFirstPageError
import com.spyneai.homev12.adapter.HomeProjectPagedAdapter
import com.spyneai.homev12.adapter.NewBackgroundAdapter
import com.spyneai.homev12.adapter.SubCategoryAdapter
import com.spyneai.model.CategoryData
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.paging.LoaderStateAdapter
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.shootapp.ui.base.ShootPortraitActivity
import com.spyneai.shootapp.utils.objectToString
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@ExperimentalPagingApi
class HomeV2Fragment : BaseFragment<DashboardViewModel, FragmentHomeV12Binding>() {

    val TAG = HomeV2Fragment::class.java.simpleName
    lateinit var btnlistener: NewBackgroundAdapter.BtnClickListener

    var subCategoryAdapter: SubCategoryAdapter? = null
    lateinit var preferenceFragment: PreferenceFragment

    lateinit var homeProjectPagedAdapter: HomeProjectPagedAdapter
    lateinit var categoryAdapter: CategoryAdapter
    lateinit var categoryList: List<CategoryData>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel?.fabClickable?.value = false

        categoryList = ArrayList()


        if (Utilities.getPreference(BaseApplication.getContext(), AppConstants.SELECTED_CATEGORY_ID)
                .isNullOrEmpty()
        ) {
            Utilities.savePrefrence(
                BaseApplication.getContext(),
                AppConstants.SELECTED_CATEGORY_ID,
                AppConstants.CARS_CATEGORY_ID
            )
        }

        fetchCategorybyEnterprise()
        observeFetchCategory()
        //Set Banner Of Selected Cat
        setBannerOfSelectedCat()

        //Change Banner When Category Change
        // When cat Change

        // Get Selected Category Data
        getCategporyData(
            Utilities.getPreference(
                BaseApplication.getContext(),
                AppConstants.SELECTED_CATEGORY_ID
            ).toString(),
            "onViewCreated"
        )

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //set category data
        setCategoryList()

        val bannerList = ArrayList<String>()
        bannerList.clear()


        when (Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.SELECTED_CATEGORY_ID
        )) {
            AppConstants.FOOD_AND_BEV_CATEGORY_ID -> {
                binding.tvSubcatProjectsViewAll.visibility = View.GONE
                bannerList.add("https://spyne-static.s3.amazonaws.com/Banner/Group+58940228.png")
            }

            AppConstants.ECOM_CATEGORY_ID -> {
                binding.tvSubcatProjectsViewAll.visibility = View.VISIBLE
                bannerList.add("https://spyne-static.s3.amazonaws.com/Banner/Banner1.png")
            }

            AppConstants.FOOTWEAR_CATEGORY_ID -> {
                binding.tvSubcatProjectsViewAll.visibility = View.GONE
                bannerList.add("https://spyne-static.s3.amazonaws.com/Banner/Banner1.png")
            }

            else -> {
                binding.tvSubcatProjectsViewAll.visibility = View.GONE
                bannerList.add("https://spyne-static.s3.amazonaws.com/Banner/carbanner.png")
            }
        }
        binding.bannerViewPager.adapter = BannerAdapter(bannerList, requireActivity(),
            object : BannerAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    if (binding.llCategoryList.visibility == View.VISIBLE) {
                        binding.llCategoryList.visibility = View.INVISIBLE
                        binding.ivCategoryDropdown.rotation = 0f
                    }
                }
            })

        preferenceFragment = PreferenceFragment()

        //For All Home Project
        homeProjectPagedAdapter = HomeProjectPagedAdapter(
            requireContext()
        )
        homeProjectPagedAdapter.addLoadStateListener { loadState ->
            when {
                homeProjectPagedAdapter.itemCount == 0 -> {
                    val error = handleFirstPageError(loadState) { homeProjectPagedAdapter.retry() }
                    if (error || loadState.append.endOfPaginationReached) {
                        stopLoader()
                    }
                }

                homeProjectPagedAdapter.itemCount > 0 -> {
                    binding.cvNoProjectYet.isVisible = false
                    stopLoader()
                }

                loadState.append.endOfPaginationReached -> stopLoader()

                else -> stopLoader()
            }
        }
        binding.rvYourProjects.layoutManager =
            LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL,
                false
            )
        val loaderStateAdapter = LoaderStateAdapter { homeProjectPagedAdapter.retry() }
        binding.rvYourProjects.adapter =
            homeProjectPagedAdapter.withLoadStateFooter(loaderStateAdapter)

        getProjectData()
        listners()
    }

    private fun stopLoader() {
        binding.shimmerYourProjects.stopShimmer()
        binding.shimmerYourProjects.visibility = View.GONE
        binding.rvYourProjects.visibility = View.VISIBLE
    }


    private fun setCategoryList() {
        categoryList.forEachIndexed { index, s ->
            if (s.categoryId == Utilities.getPreference(
                    requireContext(),
                    AppConstants.SELECTED_CATEGORY_ID
                )
            ) {
                binding.tvSelectedCategory.text = categoryList[index].categoryName

                categoryList[index].isSelected = true
            }

        }
        categoryAdapter = CategoryAdapter(requireActivity(),
            categoryList,
            object : CategoryAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {

                    switchCategory(categoryList[position].categoryId)

                    val selectedElement = categoryList.firstOrNull {
                        it.isSelected
                    }
                    selectedElement?.isSelected = false

                    categoryList[position].isSelected = true
                    categoryAdapter?.notifyDataSetChanged()

                    try {
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.SELECTED_CATEGORY_ID,
                            categoryList[position].categoryId
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    binding.llCategoryList.visibility = View.GONE
                    binding.ivCategoryDropdown.rotation = 0f
                    binding.tvSelectedCategory.text = categoryList[position].categoryName

                    val properties = HashMap<String, Any?>().apply {
                        put(
                            "auth_key",
                            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY)
                        )
                        put("categoryData", categoryList[position].objectToString())
                    }
                    requireContext().captureEvent(
                        Events.CATEGORY_DROPDOWN_SELECTED,
                        properties
                    )

                }
            })

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvChooseCategory.layoutManager = layoutManager
        binding.rvChooseCategory.adapter = categoryAdapter

    }

    private fun switchCategory(categoryId: String) {
        Utilities.showProgressDialog(requireContext())
        getCategporyData(categoryId,"switch category")
        getProjectData()
        viewModel.refreshProjectData.value = true
    }

    private fun listners() {

        binding.clMain.setOnClickListener {
            if (binding.llCategoryList.visibility == View.VISIBLE) {
                binding.llCategoryList.visibility = View.INVISIBLE
                binding.ivCategoryDropdown.rotation = 0f
            }
        }


        binding.tvSubcatProjectsViewAll.setOnClickListener {
            viewModel.gotoSearchFragment.value = true
        }

        binding.llChooseCategory.setOnClickListener {
            if (binding.llCategoryList.visibility != View.VISIBLE) {
                binding.llCategoryList.visibility = View.VISIBLE
                binding.ivCategoryDropdown.rotation = 180f
                setCategoryList()
            } else {
                binding.llCategoryList.visibility = View.GONE
                binding.ivCategoryDropdown.rotation = 0f

            }

            val properties = HashMap<String, Any?>().apply {
                put("auth_key", Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY))
            }
            requireContext().captureEvent(
                Events.CATEGORY_DROPDOWN_CLICKED,
                properties
            )

        }

        binding.ivProfile.setOnClickListener {
            if (binding.llCategoryList.visibility == View.VISIBLE) {
                binding.llCategoryList.visibility = View.INVISIBLE
                binding.ivCategoryDropdown.rotation = 0f
            } else
                viewModel.showNavigation.value = true
        }

        binding.tvYourProjectsViewAll.setOnClickListener {
            viewModel.gotoMyOrderFragmentNew.value = true

        }


    }


    private fun getProjectData() {
        lifecycleScope.launch {
            viewModel.getAllProjects("all")
                .distinctUntilChanged().collectLatest {
                    homeProjectPagedAdapter.submitData(it)
                }
        }
    }


    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeV12Binding.inflate(inflater, container, false)


    private fun getCategporyData(categoryId: String,from: String) {
        Log.d(TAG, "getCategporyData: $from")
        viewModel.getCategoryDataV2(categoryId)
        observeCategoryData(categoryId)
    }

    private fun observeCategoryData(categoryId: String) {
        viewModel.categoryResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    viewModel.fabClickable.value = true
                    it.value.data.forEach {
                        if (it.categoryId == categoryId)
                            Utilities.saveBool(
                                requireContext(),
                                AppConstants.SHOW_MARKETPLACES,
                                it.show_marketplaces
                            )
                    }

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.SELECTED_CATEGORY_ID,
                        categoryId
                    )

                    val data = it.value.data[0]

                    viewModel.CategoryAgnosData.value = data

                    if (!data.subCategoryV2s.isNullOrEmpty()) {
                        binding.tvSubCatType.visibility = View.VISIBLE
                        binding.tvSubCatType.text = data?.subcategoryLabel
                        binding.tvSubCatType.typeface =
                            ResourcesCompat.getFont(requireContext(), R.font.poppins_semibold)

                        if (data.categoryId == AppConstants.ECOM_CATEGORY_ID) {
                            val list = ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2?>()
                            data?.subCategoryV2s.forEachIndexed { index, subCategoryV2 ->
                                if (index <= 4)
                                    list.add(subCategoryV2)
                            }
                            list.add(null)
                            initSearchSubCategories(list, data)
                        } else {
                            initSubCategories(data?.subCategoryV2s, data)
                        }

                    } else {
                        binding.tvSubCatType.visibility = View.GONE
                        binding.flVehicleType.visibility = View.GONE
                    }

                }

                is Resource.Failure -> {
                    viewModel.fabClickable.value = false
                    handleApiError(it) {
                        Utilities.hideProgressDialog()
                        viewModel.getCategoryDataV2(categoryId)
                    }
                }

                else -> {

                }
            }
        }
    }

    private fun initSearchSubCategories(
        list: java.util.ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2?>,
        data: CatAgnosticResV2.CategoryAgnos
    ) {
        binding.shimmerVehicleType.stopShimmer()
        binding.shimmerVehicleType.visibility = View.GONE
        binding.rvVehicleType.visibility = View.VISIBLE
        val subCategoryAdapter = SubCategorySearchAdapter(requireContext(),
            list,
            object : SubCategorySearchAdapter.BtnClickListener {
                override fun onBtnClick(
                    position: Int,
                    data: CatAgnosticResV2.CategoryAgnos.SubCategoryV2?
                ) {
                    if (binding.llCategoryList.visibility == View.VISIBLE) {
                        binding.llCategoryList.visibility = View.INVISIBLE
                        binding.ivCategoryDropdown.rotation = 0f
                    } else {
                        if (data == null) {
                            startActivity(
                                Intent(
                                    requireContext(),
                                    SearchCategoryActivity::class.java
                                )
                            )
                        } else {
                            val intent = Intent(
                                requireContext(), ShootPortraitActivity::class.java
                            )

                            intent.apply {
                                putExtra(AppConstants.CATEGORY_ID, AppConstants.ECOM_CATEGORY_ID)
                                putExtra(AppConstants.ORIENTATION, "portrait")
                                putExtra(AppConstants.CATEGORY_NAME, "Ecom")
                                putExtra(AppConstants.SUB_CAT_ID, data.prodSubCatId)
                                startActivity(intent)
                            }
                        }

                    }
                }
            }
        )
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvVehicleType.layoutManager = layoutManager
        binding.rvVehicleType.adapter = subCategoryAdapter

    }


    private fun initSubCategories(
        subCatList: List<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>?,
        data: CatAgnosticResV2.CategoryAgnos
    ) {


        binding.shimmerVehicleType.stopShimmer()
        binding.shimmerVehicleType.visibility = View.GONE
        binding.rvVehicleType.visibility = View.VISIBLE
        subCategoryAdapter = SubCategoryAdapter(requireContext(),
            subCatList as ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>,
            object : SubCategoryAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    if (binding.llCategoryList.visibility == View.VISIBLE) {
                        binding.llCategoryList.visibility = View.INVISIBLE
                        binding.ivCategoryDropdown.rotation = 0f
                    } else {
                        val item = data
                        if (item.isActive) {
                            val intent = Intent(
                                requireContext(), ShootPortraitActivity::class.java
                            )

                            intent.apply {
                                putExtra(AppConstants.CATEGORY_ID, item.categoryId)
                                putExtra(AppConstants.ORIENTATION, item.orientation)
                                putExtra(AppConstants.CATEGORY_NAME, item.name)
                                putExtra(AppConstants.IMAGE_URL, item.displayThumbnail)
                                putExtra(AppConstants.DESCRIPTION, item.description)
                                putExtra(AppConstants.COLOR, item.colorCode)
                                putExtra(AppConstants.SUB_CAT_ID, subCatList[position].prodSubCatId)
                                startActivity(intent)
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Coming Soon !",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        )

        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.rvVehicleType.layoutManager = layoutManager
        binding.rvVehicleType.adapter = subCategoryAdapter
    }

    private fun fetchCategorybyEnterprise() {
        Utilities.showProgressDialog(requireContext())
        viewModel.fetchCategoryByEnterprise()
    }

    private fun observeFetchCategory() {
        viewModel.fetchCategory.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {

                }
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    categoryList = it.value.data.categoryData.sortedBy {
                        it.priority
                    }

                    if (Utilities.getPreference(
                            BaseApplication.getContext(),
                            AppConstants.SELECTED_CATEGORY_ID
                        ).toString().isNullOrEmpty()
                    ) {
                        Utilities.savePrefrence(
                            BaseApplication.getContext(),
                            AppConstants.SELECTED_CATEGORY_ID,
                            categoryList[0].categoryId
                        )
                    }

                    categoryList.forEach {
                        if (it.categoryId == Utilities.getPreference(
                                BaseApplication.getContext(),
                                AppConstants.SELECTED_CATEGORY_ID
                            ).toString()
                        ) {

                            binding.tvSelectedCategory.text = it.categoryName

                        }
                    }
                    getCategporyData(
                        Utilities.getPreference(
                            BaseApplication.getContext(),
                            AppConstants.SELECTED_CATEGORY_ID
                        ).toString(),
                        "Observe Fetch categories"
                    )

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

    private fun setBannerOfSelectedCat() {
        val bannerList = ArrayList<String>()
        bannerList.clear()
        when (Utilities.getPreference(requireContext(), AppConstants.SELECTED_CATEGORY_ID)) {
            AppConstants.FOOD_AND_BEV_CATEGORY_ID -> {
                bannerList.add("https://spyne-static.s3.amazonaws.com/ezgif.com-gif-maker+(3).webp")
            }
            AppConstants.ECOM_CATEGORY_ID -> {
                bannerList.add("https://spyne-static.s3.amazonaws.com/Banner/Banner1.png")
            }
            AppConstants.FOOTWEAR_CATEGORY_ID -> {
                bannerList.add("https://spyne-static.s3.amazonaws.com/Banner/Banner1.png")
            }
            else -> {
                bannerList.add("https://spyne-static.s3.amazonaws.com/Banner/carbanner.png")
            }
        }

        binding.bannerViewPager.adapter = BannerAdapter(bannerList, requireActivity(),
            object : BannerAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    if (binding.llCategoryList.visibility == View.VISIBLE) {
                        binding.llCategoryList.visibility = View.INVISIBLE
                        binding.ivCategoryDropdown.rotation = 0f
                    }
                }
            })
    }

}