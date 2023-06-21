package com.spyneai.onboarding.data.viewmodels

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.Resource
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.dashboard.repository.DashboardLocalDS
import com.spyneai.dashboard.repository.DashboardRepository
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.loginsignup.models.Categories
import com.spyneai.model.FetchCategoryRes
import com.spyneai.model.login.LoginResponse
import com.spyneai.model.otp.OtpResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboarding.data.models.LoginWithPasswordRes
import com.spyneai.onboarding.data.models.SignupResponse
import com.spyneai.onboarding.data.models.ValidateOtpResponse
import com.spyneai.onboarding.data.repository.OnBoardingRepository
import com.spyneai.onboardingv2.data.MessageRes
import com.spyneai.registration.models.ChangeAdminDTO
import com.spyneai.registration.models.CreateEnterPriseDTO
import com.spyneai.shootapp.utils.objectToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnBoardingViewModel : ViewModel() {

    private val spyneAppDatabase = SpyneAppDatabase.getInstance(BaseApplication.getContext())
    private val repository = OnBoardingRepository(spyneAppDatabase.categoriesDao())
    private val dashboardRepository = DashboardRepository(
        categoryDataAppDao = spyneAppDatabase.categoryDataDao(),
        dashboardLocalDS = DashboardLocalDS(spyneAppDatabase.categoryDataDao()),
        apiServices = ClipperApiClient().getClient(),
        context = BaseApplication.getContext()
    )
    val usePassword: MutableLiveData<Boolean> = MutableLiveData()
    val showSignup: MutableLiveData<Boolean> = MutableLiveData()
    val reqOtpSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var sentTo = ""

    private val _loginWithPasswordResponse: MutableLiveData<Resource<LoginWithPasswordRes>> =
        MutableLiveData()
    val loginEmailPasswordResponse: LiveData<Resource<LoginWithPasswordRes>>
        get() = _loginWithPasswordResponse

    private val _reqOtpResponse: MutableLiveData<Resource<LoginResponse>> =
        MutableLiveData()
    val reqOtpResponse: LiveData<Resource<LoginResponse>>
        get() = _reqOtpResponse

    private val _verifyOtpResponse: MutableLiveData<Resource<OtpResponse>> =
        MutableLiveData()
    val verifyOtpResponse: LiveData<Resource<OtpResponse>>
        get() = _verifyOtpResponse

    private var _categoryResponse: MutableLiveData<Resource<CatAgnosticResV2>> =
        MutableLiveData()
    val categoryResponse: LiveData<Resource<CatAgnosticResV2>>
        get() = _categoryResponse

    var _message: MutableLiveData<Resource<MessageRes>> = MutableLiveData()
    val message: LiveData<Resource<MessageRes>>
        get() = _message

    private var _createEnterPrise: MutableLiveData<Resource<CreateEnterPriseDTO>> =
        MutableLiveData()
    val createEnterPrise: LiveData<Resource<CreateEnterPriseDTO>>
        get() = _createEnterPrise

    private var _fetchCategory: MutableLiveData<Resource<FetchCategoryRes>> = MutableLiveData()
    val fetchCategory: LiveData<Resource<FetchCategoryRes>>
        get() = _fetchCategory

    private var _changeAdmin: MutableLiveData<Resource<ChangeAdminDTO>> =
        MutableLiveData()
    val changeAdmin: LiveData<Resource<ChangeAdminDTO>>
        get() = _changeAdmin

    fun updateCountry(
        map: HashMap<String, String>
    ) = viewModelScope.launch {
        _message.value = Resource.Loading
        _message.value = repository.updateCountry(map)
    }

    fun loginWithPassword(
        map: MutableMap<String, Any?>
    ) = viewModelScope.launch {
        _loginWithPasswordResponse.value = Resource.Loading
        _loginWithPasswordResponse.value =
            repository.loginWithPassword(map)
    }

    private val _signupResponse: MutableLiveData<Resource<SignupResponse>> =
        MutableLiveData()
    val signupResponse: LiveData<Resource<SignupResponse>> get() = _signupResponse

    fun signUp(
        map: MutableMap<String, String>
    ) = viewModelScope.launch {
        _signupResponse.value = Resource.Loading
        _signupResponse.value = repository.signUp(map)
    }

    private val _otpResponse: MutableLiveData<Resource<OtpResponse>> = MutableLiveData()
    val otpResponse: LiveData<Resource<OtpResponse>> get() = _otpResponse

    fun loginWithOTPEmail(
        email_id: String,
        apiKey: String
    ) = viewModelScope.launch {
        _otpResponse.value = Resource.Loading
        _otpResponse.value = repository.loginWithOTPEmail(email_id, apiKey)
    }


    private val _validateOtpResponse: MutableLiveData<Resource<ValidateOtpResponse>> =
        MutableLiveData()
    val validateOtpResponse: LiveData<Resource<ValidateOtpResponse>> get() = _validateOtpResponse


    fun reqOtp() {
        val id = Utilities.getPreference(BaseApplication.getContext(), AppConstants.ID).toString()
        sentTo = id
        val data: MutableMap<String, Any> = HashMap()

        if (TextUtils.isDigitsOnly(id)) {
            data.apply {
                put("contact_no", id)
                put("api_key", WhiteLabelConstants.API_KEY)
            }
        } else {
            data.apply {
                put("email_id", id)
                put("api_key", WhiteLabelConstants.API_KEY)
            }
        }

        viewModelScope.launch {
            _reqOtpResponse.value = Resource.Loading
            _reqOtpResponse.value = repository.reqOtp(data)
        }
    }

    fun verifyOtp(otpEntered: String) {
        val id = Utilities.getPreference(BaseApplication.getContext(), AppConstants.ID).toString()
        val map = HashMap<String, String>()

        if (TextUtils.isDigitsOnly(id))
            map["contact_no"] = id
        else
            map["email_id"] = id

        map.apply {
            put("api_key", WhiteLabelConstants.API_KEY)
            put("otp", otpEntered)
            put("source", "App_android")
        }

        viewModelScope.launch {
            _verifyOtpResponse.value = Resource.Loading
            _verifyOtpResponse.value = repository.verityOtp(map)
        }
    }

    fun saveData(data: List<CatAgnosticResV2.CategoryAgnos>) {
        val subCatList = ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>()

        data.forEach {
            it.subCategoryV2s?.let { it ->
                subCatList.addAll(it)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            dashboardRepository.saveCatAgnosData(data, subCatList)
        }
    }

    fun getCategoryData(catId: String) = viewModelScope.launch {
        _categoryResponse.value = Resource.Loading
        _categoryResponse.value = repository.getCategoryData(catId)
    }

    suspend fun createEnterPrise(map: MutableMap<String, String>) = viewModelScope.launch {
        _createEnterPrise.value = Resource.Loading
        _createEnterPrise.value = repository.createEnterPrise(map)
    }


    suspend fun createAdmin(map: MutableMap<String, Any>) = viewModelScope.launch {
        _changeAdmin.value = Resource.Loading
        _changeAdmin.value = repository.createAdmin(map)
    }


    fun fetchCategoryByEnterprise() = viewModelScope.launch {
        _fetchCategory.value = Resource.Loading

        val response = repository.fetchCategory()

        if (response is Resource.Success) {
            withContext(Dispatchers.IO){
                //save to local DB
                repository.saveCategory(
                    Categories(
                        Utilities.getPreference(BaseApplication.getContext(), AppConstants.USER_ID)
                            .toString(),
                        response.value.objectToString()
                    )
                )
            }
            _fetchCategory.value = response
        } else {
            _fetchCategory.value = response
        }
    }

}
































