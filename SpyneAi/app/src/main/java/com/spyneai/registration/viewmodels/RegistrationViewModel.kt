package com.spyneai.registration.viewmodels

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.loginsignup.data.LoginBodyV2
import com.spyneai.loginsignup.models.ForgotPasswordResponse
import com.spyneai.model.login.LoginResponse
import com.spyneai.model.otp.LoginResponseV2
import com.spyneai.model.otp.OtpResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboarding.data.models.LoginWithPasswordRes
import com.spyneai.onboarding.data.models.SignupResponse
import com.spyneai.onboardingv2.data.MessageRes
import com.spyneai.onboardingv2.data.UpdateUserDetailBody
import com.spyneai.onboardingv2.data.UpdateUserDetailRes
import com.spyneai.registration.datalayer.LoginSystemRepo
import com.spyneai.registration.models.CreateEnterPriseDTO
import com.spyneai.registration.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import kotlin.collections.set

class RegistrationViewModel : ViewModel() {
    private val repository = LoginSystemRepo()
    val reqOtpSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var sentTo = ""
    var loginBody : LoginBodyV2? = null
    private val _reqOtpResponse: MutableLiveData<Resource<LoginResponse>> =
        MutableLiveData()

    val reqOtpResponse: LiveData<Resource<LoginResponse>>
        get() = _reqOtpResponse

    private val _reqOtpResponseV2: MutableLiveData<Resource<LoginResponseV2>> =
        MutableLiveData()

    val reqOtpResponseV2: LiveData<Resource<LoginResponseV2>>
        get() = _reqOtpResponseV2
    var _verifyOtpResponse: SingleLiveEvent<Resource<OtpResponse>> = SingleLiveEvent()

    val verifyOtpResponse: SingleLiveEvent<Resource<OtpResponse>>
        get() = _verifyOtpResponse

    var _verifyOtpResponseV2: SingleLiveEvent<Resource<LoginResponseV2>> = SingleLiveEvent()

    val verifyOtpResponseV2: SingleLiveEvent<Resource<LoginResponseV2>>
        get() = _verifyOtpResponseV2

    private val _signupResponse: MutableLiveData<Resource<SignupResponse>> =
        MutableLiveData()
    val signupResponse: LiveData<Resource<SignupResponse>> get() = _signupResponse
    private val _forgetPwdResp: MutableLiveData<Resource<ForgotPasswordResponse>> =
        MutableLiveData()
    val forgetPwdResp: LiveData<Resource<ForgotPasswordResponse>> get() = _forgetPwdResp

    private var _message: MutableLiveData<Resource<MessageRes>> = MutableLiveData()
    val message: LiveData<Resource<MessageRes>>
        get() = _message

    private var _updateUserDetail: MutableLiveData<Resource<UpdateUserDetailRes>> = MutableLiveData()
    val updateUserDetail: LiveData<Resource<UpdateUserDetailRes>>
        get() = _updateUserDetail

    private var _loginWithPasswordResp: MutableLiveData<Resource<LoginWithPasswordRes>?> = MutableLiveData()
    val loginWithPasswordResp: LiveData<Resource<LoginWithPasswordRes>?>
        get() = _loginWithPasswordResp

    private var _createEnterPrise: MutableLiveData<Resource<CreateEnterPriseDTO>> =
        MutableLiveData()
    val createEnterPrise: LiveData<Resource<CreateEnterPriseDTO>>
        get() = _createEnterPrise


    fun reqOtp(type: String) {
        val id = Utilities.getPreference(BaseApplication.getContext(), AppConstants.ID).toString()
        val resend_otp = Utilities.getBool(BaseApplication.getContext(), AppConstants.RESEND_OTP)
        val verification = Utilities.getBool(BaseApplication.getContext(), AppConstants.VERIFICATION)
        val user_id = Utilities.getPreference(BaseApplication.getContext(), AppConstants.USER_ID).toString()
        sentTo = id
        val data: MutableMap<String, Any> = HashMap()
        if(type == "login_flow"){
            if (TextUtils.isDigitsOnly(id)) {
                data.apply {
                    put("contact_no", id)
                    put("resend_otp", resend_otp)
                    put("verification", verification)
                    put("user_id", user_id)
                    put("api_key", WhiteLabelConstants.API_KEY)
                }
            } else {
                data.apply {
                    put("email_id", id)
                    put("resend_otp", resend_otp)
                    put("verification", verification)
                    put("user_id", user_id)
                    put("api_key", WhiteLabelConstants.API_KEY)
                }
            }
        }else{
            if (TextUtils.isDigitsOnly(id)) {
                data.apply {
                    put("contact_no", id)
                    put("resend_otp", resend_otp)
                    put("verification", verification)
                    put("user_id", user_id)
                    put("api_key", WhiteLabelConstants.API_KEY)
                }
            } else {
                data.apply {
                    put("email_id", id)
                    put("resend_otp", resend_otp)
                    put("verification", verification)
                    put("user_id", user_id)
                    put("api_key", WhiteLabelConstants.API_KEY)
                }
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

    fun reqOtpV2(
        body:LoginBodyV2
    ) = viewModelScope.launch {
        _reqOtpResponseV2.value = Resource.Loading
        _reqOtpResponseV2.value = repository.reqOtpV2(body)
    }

    fun signUp(
        map: MutableMap<String, String>
    ) = viewModelScope.launch {
        _signupResponse.value = Resource.Loading
        _signupResponse.value = repository.signUp(map)
    }

    fun forgetPwd(
        map: MutableMap<String, String>
    ) = viewModelScope.launch {
        _forgetPwdResp.value = Resource.Loading
        _forgetPwdResp.value = repository.forgetPwd(map)
    }

    fun updateCountry(
        map: java.util.HashMap<String, String>
    ) = viewModelScope.launch {
        _message.value = Resource.Loading
        _message.value = repository.updateCountry(map)
    }

    fun updateUserDetail(
        body:UpdateUserDetailBody
    ) = viewModelScope.launch {
        _updateUserDetail.value = Resource.Loading
        _updateUserDetail.value = repository.updateUserDetail(body)
    }

    suspend fun createEnterPrise(map : MutableMap<String,String>) = viewModelScope.launch {
        _createEnterPrise.value = Resource.Loading
        _createEnterPrise.value = repository.createEnterPrise(map)
    }

    fun loginWithPassword(map: HashMap<String, Any?>) = viewModelScope.launch {
        _loginWithPasswordResp.value = Resource.Loading
        _loginWithPasswordResp.value = repository.login(map)
    }

}