package com.spyneai.dashboard.data


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.gson.Gson
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.ProjectApiClient
import com.spyneai.base.network.Resource
import com.spyneai.credits.model.TransactionHistory
import com.spyneai.credits.paging.TransactionPagedRepository
import com.spyneai.dashboard.data.model.RidResponse
import com.spyneai.dashboard.data.model.UserDetailsResponse
import com.spyneai.dashboard.data.repository.DashboardRepository
import com.spyneai.dashboard.repository.model.CheckInOutRes
import com.spyneai.dashboard.repository.model.GetGCPUrlRes
import com.spyneai.dashboard.repository.model.LocationsRes
import com.spyneai.dashboard.repository.model.VersionStatusRes
import com.spyneai.dashboard.repository.model.category.DynamicLayout
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.dashboard.response.RequestCreditResponse
import com.spyneai.dashboard.ui.base.BaseViewModel
import com.spyneai.isInternetActive
import com.spyneai.model.FetchCategoryRes
import com.spyneai.model.credit.AvailableCreditResponse
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.model.credit.PricingPlanResponse
import com.spyneai.model.credit.SubscriptionPlanResponse
import com.spyneai.needs.AppConstants
import com.spyneai.onboarding.data.repository.OnBoardingRepository
import com.spyneai.onboardingv2.data.MessageRes
import com.spyneai.orders.data.paging.PagedRepository
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.data.response.GetOngoingSkusResponse
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.dashboardV2.data.model.LogoutBody
import com.spyneai.dashboardV2.data.model.LogoutResponse
import com.spyneai.needs.Utilities
import com.spyneai.shootapp.repository.model.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DashboardViewModel @ViewModelInject constructor(
    private val repository: DashboardRepository
) : BaseViewModel() {

    private val spyneAppDatabase = SpyneAppDatabase.getInstance(BaseApplication.getContext())

    private val onBoardingRepository = OnBoardingRepository(spyneAppDatabase.categoriesDao())

    var isRIDSelected: MutableLiveData<Boolean> = MutableLiveData()


    private val _logoutResponse: MutableLiveData<Resource<LogoutResponse>> =
        MutableLiveData()
    val logoutResponse: LiveData<Resource<LogoutResponse>>
        get() = _logoutResponse

    fun newUserLogout(body: LogoutBody) = viewModelScope.launch {
        _logoutResponse.value = Resource.Loading
        _logoutResponse.value = onBoardingRepository.newUserLogout(body)
    }


    private val _categoriesResponse: MutableLiveData<Resource<NewCategoriesResponse>> =
        MutableLiveData()
    val categoriesResponse: LiveData<Resource<NewCategoriesResponse>>
        get() = _categoriesResponse

    private val _userCreditsRes: MutableLiveData<Resource<CreditDetailsResponse>> =
        MutableLiveData()
    val userCreditsRes: LiveData<Resource<CreditDetailsResponse>>
        get() = _userCreditsRes


    private val _availableCreditRes: MutableLiveData<Resource<AvailableCreditResponse>> =
        MutableLiveData()
    val availableCreditRes: LiveData<Resource<AvailableCreditResponse>>
        get() = _availableCreditRes

    private val _getOngoingSkusResponse: MutableLiveData<Resource<GetOngoingSkusResponse>> =
        MutableLiveData()
    val getOngoingSkusResponse: LiveData<Resource<GetOngoingSkusResponse>>
        get() = _getOngoingSkusResponse

    private val _completedSkusResponse: MutableLiveData<Resource<CompletedSKUsResponse>> =
        MutableLiveData()
    val completedSkusResponse: LiveData<Resource<CompletedSKUsResponse>>
        get() = _completedSkusResponse

    private val _versionResponse: MutableLiveData<Resource<VersionStatusRes>> = MutableLiveData()
    val versionResponse: LiveData<Resource<VersionStatusRes>>
        get() = _versionResponse


    var _gcpUrlResponse: MutableLiveData<Resource<GetGCPUrlRes>> = MutableLiveData()
    val gcpUrlResponse: LiveData<Resource<GetGCPUrlRes>>
        get() = _gcpUrlResponse

    var _locationResponse: MutableLiveData<Resource<LocationsRes>> = MutableLiveData()
    val locationsResponse: LiveData<Resource<LocationsRes>>
        get() = _locationResponse

    var _checkInOutRes: MutableLiveData<Resource<CheckInOutRes>> = MutableLiveData()
    val checkInOutRes: LiveData<Resource<CheckInOutRes>>
        get() = _checkInOutRes


    val signup: MutableLiveData<Boolean> = MutableLiveData()
    var replaceJoinDetails: MutableLiveData<Boolean> = MutableLiveData()
    var replaceJoinOtp: MutableLiveData<Boolean> = MutableLiveData()
    var replaceJoinOtpPost: MutableLiveData<Boolean> = MutableLiveData()
    var replacemanagepref: MutableLiveData<Boolean> = MutableLiveData()
    var prefrencefrag: MutableLiveData<Boolean> = MutableLiveData()
    var replacemanageAdd: MutableLiveData<Boolean> = MutableLiveData()
    var replacemanage: MutableLiveData<Boolean> = MutableLiveData()
    val fabClickable: MutableLiveData<Boolean> = MutableLiveData()
    val startImageUpload: MutableLiveData<Boolean> = MutableLiveData()

    val showNavigation: MutableLiveData<Boolean> = MutableLiveData()


    var _subscriptionPlanRes: MutableLiveData<Resource<SubscriptionPlanResponse>> =
        MutableLiveData()
    val subscriptionPlanRes: LiveData<Resource<SubscriptionPlanResponse>>
        get() = _subscriptionPlanRes


    var _pricingPlanRes: MutableLiveData<Resource<PricingPlanResponse>> = MutableLiveData()
    val pricingPlanRes: LiveData<Resource<PricingPlanResponse>>
        get() = _pricingPlanRes

    fun getCategoryAgnosData(catId: String): CatAgnosticResV2.CategoryAgnos {
        return spyneAppDatabase.categoryDataDao().getCategoryById(catId)
    }

    fun getSubscriptionPlan(
    ) = viewModelScope.launch {
        _subscriptionPlanRes.value = Resource.Loading
        _subscriptionPlanRes.value = repository.getSubscriptionPlan()
    }

    fun getPricingPlan(
    ) = viewModelScope.launch {
        _pricingPlanRes.value = Resource.Loading
        _pricingPlanRes.value = repository.getPricingPlan()
    }

    var CategoryAgnosData: MutableLiveData<CatAgnosticResV2.CategoryAgnos> = MutableLiveData()

    val isNewUser: MutableLiveData<Boolean> = MutableLiveData()
    val startHereVisible: MutableLiveData<Boolean> = MutableLiveData()
    val isStartAttendance: MutableLiveData<Boolean> = MutableLiveData()
    val gotoMyOrderFragment: MutableLiveData<Boolean> = MutableLiveData()
    val gotoMyOrderFragmentNew: MutableLiveData<Boolean> = MutableLiveData()
    val gotoSearchFragment: MutableLiveData<Boolean> = MutableLiveData()
    val creditsMessage: MutableLiveData<String> = MutableLiveData()
    val refreshProjectData: MutableLiveData<Boolean> = MutableLiveData()
    var type = "checkin"
    var fileUrl = ""
    var preSignedUrl = ""
    var siteImagePath = ""
    var resultCode: Int? = null
    val continueAnyway: MutableLiveData<Boolean> = MutableLiveData()


    var _updateUserName: MutableLiveData<Resource<MessageRes>> = MutableLiveData()
    val updateUserName: LiveData<Resource<MessageRes>>
        get() = _updateUserName

    fun updateCountry(
        map: HashMap<String, String>
    ) = viewModelScope.launch {
        _updateUserName.value = Resource.Loading
        _updateUserName.value = onBoardingRepository.updateCountry(map)
    }

    fun userDetails(
        authKey: String,
        entityId: String
    ) = viewModelScope.launch {
        _userDetailsResponse.value = Resource.Loading
        _userDetailsResponse.value = repository.userDetails(authKey, entityId)
    }


    fun getCategories(
        tokenId: String
    ) = viewModelScope.launch {
        _categoriesResponse.value = Resource.Loading

        GlobalScope.launch(Dispatchers.IO) {
            val catList = repository.getCategories()

            if (!catList.isNullOrEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    _categoriesResponse.value = Resource.Success(
                        NewCategoriesResponse(
                            200,
                            "",
                            catList
                        )
                    )
                }

            } else {
                val response = repository.getCategories(tokenId)

                if (response is Resource.Success) {
                    //save response to local DB
                    GlobalScope.launch(Dispatchers.IO) {
                        val catList = response.value.data
                        val dynamicList = ArrayList<DynamicLayout>()

                        catList.forEach {
                            dynamicList.add(
                                DynamicLayout(it.categoryId, it.dynamic_layout?.project_dialog)
                            )
                        }
                        repository.insertCategories(
                            catList,
                            dynamicList
                        )

                        GlobalScope.launch(Dispatchers.Main) {
                            _categoriesResponse.value = Resource.Success(
                                NewCategoriesResponse(
                                    200,
                                    "",
                                    catList
                                )
                            )
                        }
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        _categoriesResponse.value = response
                    }
                }
            }
        }
    }


    @ExperimentalPagingApi
    fun getAllProjects(status: String): Flow<PagingData<Project>> {
        return PagedRepository(
            ProjectApiClient().getClient(),
            SpyneAppDatabase.getInstance(BaseApplication.getContext()),
            status,
            "Self Serve",
            true
        ).getSearchResultStream()
            .cachedIn(viewModelScope)
    }


    @ExperimentalPagingApi
    fun getTransactionHistory(
        fromDate: String,
        toDate: String,
        actionType: String
    ): Flow<PagingData<TransactionHistory>> {
        return TransactionPagedRepository(
            service = ClipperApiClient().getClient(),
            fromDate = fromDate,
            toDate = toDate,
            actionType = actionType
        ).getSearchResultStream()
            .cachedIn(viewModelScope)
    }






    //ongoing completed
    private val _getProjectsResponse: MutableLiveData<Resource<GetProjectsResponse>> =
        MutableLiveData()
    val getProjectsResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _getProjectsResponse


    // credit details food
    private val _creditDetailsResponse: MutableLiveData<Resource<CreditDetailsResponse>> =
        MutableLiveData()
    val creditDetailsResponse: LiveData<Resource<CreditDetailsResponse>>
        get() = _creditDetailsResponse

    val _userDetailsResponse: MutableLiveData<Resource<UserDetailsResponse>> = MutableLiveData()
    val userDetailsResponse: LiveData<Resource<UserDetailsResponse>>
        get() = _userDetailsResponse

    private val _ridResponse: MutableLiveData<Resource<RidResponse>> = MutableLiveData()
    val ridResponse: LiveData<Resource<RidResponse>>
        get() = _ridResponse

    private val _requestCreditResponse: MutableLiveData<Resource<RequestCreditResponse>> =
        MutableLiveData()
    val requestCreditResponse: LiveData<Resource<RequestCreditResponse>>
        get() = _requestCreditResponse

    private var _fetchCategory: MutableLiveData<Resource<FetchCategoryRes>> = MutableLiveData()
    val fetchCategory: LiveData<Resource<FetchCategoryRes>>
        get() = _fetchCategory


    fun getProjects(
        tokenId: String, status: String
    ) = viewModelScope.launch {
        _getProjectsResponse.value = Resource.Loading
        _getProjectsResponse.value = repository.getProjects(tokenId, status)
    }

    private val _getCompletedProjectsResponse: MutableLiveData<Resource<GetProjectsResponse>> =
        MutableLiveData()
    val getCompletedProjectsResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _getCompletedProjectsResponse

    fun getCompletedProjects(
        tokenId: String, status: String
    ) = viewModelScope.launch {
        _getCompletedProjectsResponse.value = Resource.Loading
        _getCompletedProjectsResponse.value = repository.getProjects(tokenId, status)
    }


    fun getGCPUrl(
        imageName: String,
    ) = viewModelScope.launch {
        _gcpUrlResponse.value = Resource.Loading
        _gcpUrlResponse.value = repository.getGCPUrl(imageName)
    }

    fun getUserCredits(
        userId: String
    ) = viewModelScope.launch {
        _userCreditsRes.value = Resource.Loading
        _userCreditsRes.value = repository.getUserCredits(userId)
    }

    fun getAvailableCredit(
    ) = viewModelScope.launch {
        _availableCreditRes.value = Resource.Loading
        _availableCreditRes.value = repository.availableCredits()
    }


    fun captureCheckInOut(
        location: JSONObject,
        location_id: String,
        imageUrl: String = ""
    ) = viewModelScope.launch {
        _checkInOutRes.value = Resource.Loading
        _checkInOutRes.value = repository.captureCheckInOut(location, location_id, imageUrl)
    }


    fun getLocations(
    ) = viewModelScope.launch {
        _locationResponse.value = Resource.Loading
        _locationResponse.value = repository.getLocations()
    }


    fun categoryData(catId: String): LiveData<com.spyneai.dashboard.network.Resource<CatAgnosticResV2.CategoryAgnos?>> =
        repository.getCategoriesData(catId)


    fun getCanAgnosData() = repository.getCatAgnosData()


    fun getCategoryData(catId: String) = categoryData(catId)


    private val _categoryResponse: MutableLiveData<Resource<CatAgnosticResV2>> =
        MutableLiveData()
    val categoryResponse: LiveData<Resource<CatAgnosticResV2>>
        get() = _categoryResponse

    fun getCategoryDataV2(catId: String) = viewModelScope.launch {
        _categoryResponse.value = Resource.Loading

        withContext(Dispatchers.IO) {
            val data = repository.getLiveDataCategoryById(catId)

            if (data != null ) {
                val list = ArrayList<CatAgnosticResV2.CategoryAgnos>()
                list.add(data)

                withContext(Dispatchers.Main) {
                    _categoryResponse.value = Resource.Success(
                        CatAgnosticResV2(
                            data = list,
                            message = "Fetched Category",
                            status = 200
                        )
                    )
                }
            } else {
                val response = repository.getCategoryData(catId)
                if (response is Resource.Success) {
                    //save data
                    saveData(response.value.data)
                    withContext(Dispatchers.Main) {
                        _categoryResponse.value = response
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _categoryResponse.value = response
                    }
                }

            }
        }
    }

    fun saveData(data: List<CatAgnosticResV2.CategoryAgnos>) {
        val subCatList = ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>()

        fun userCreditsDetails(
            authKey: String,
            entityId: String
        ) = viewModelScope.launch {
            _creditDetailsResponse.value = Resource.Loading
            _creditDetailsResponse.value = repository.userCreditsDetails(authKey, entityId)
        }

        fun userDetails(
            authKey: String,
            entityId: String
        ) = viewModelScope.launch {
            _userDetailsResponse.value = Resource.Loading
            _userDetailsResponse.value = repository.userDetails(authKey, entityId)
        }
        data.forEach {
            it.subCategoryV2s?.let { it ->
                subCatList.addAll(it)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCatAgnosData(data, subCatList)
        }
    }


    fun deleteCategoryData() {
        spyneAppDatabase.categoryDataDao().deleteCatData()
    }


    fun getRestaurantList(
        authKey: String
    ) = viewModelScope.launch {
        _ridResponse.value = Resource.Loading
        _ridResponse.value = repository.getRestaurantList(authKey)
    }


    fun requestCredits(
        authKey: String
    ) = viewModelScope.launch {
        _requestCreditResponse.value = Resource.Loading
        _requestCreditResponse.value = repository.requestCredits(authKey)
    }

    fun fetchCategoryByEnterprise() = viewModelScope.launch {
        // check in local DB
        val data = withContext(Dispatchers.IO){
            onBoardingRepository.getCategory(
                Utilities.getPreference(
                    BaseApplication.getContext(),
                    AppConstants.USER_ID
                ).toString()
            )
        }

        if (data == null) {
            _fetchCategory.value = Resource.Loading
            _fetchCategory.value = repository.fetchCategory()
        } else {
            try {
                //parse data to response
                val response = Gson().fromJson(
                    data.data,
                    FetchCategoryRes::class.java
                )
                if (response != null) {
                    _fetchCategory.value = Resource.Success(response)
                } else {
                    _fetchCategory.value = Resource.Loading
                    _fetchCategory.value = repository.fetchCategory()
                }
            } catch (e: Exception) {
                _fetchCategory.value = Resource.Loading
                _fetchCategory.value = repository.fetchCategory()
            }
        }

    }
}