package com.spyneai.loginsignup.data

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.model.login.LoginResponse
import com.spyneai.model.otp.OtpResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.coroutines.launch

class OnBoardingViewModel : ViewModel() {
    private val repository = OnBoardingRepository()
    val reqOtpSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var sentTo = ""

    private val _reqOtpResponse: MutableLiveData<Resource<LoginResponse>> =
        MutableLiveData()
    val reqOtpResponse: LiveData<Resource<LoginResponse>>
        get() = _reqOtpResponse

    private val _verifyOtpResponse: MutableLiveData<Resource<OtpResponse>> =
        MutableLiveData()
    val verifyOtpResponse: LiveData<Resource<OtpResponse>>
        get() = _verifyOtpResponse


    fun reqOtp(){
        val id = Utilities.getPreference(BaseApplication.getContext(),AppConstants.ID).toString()
        sentTo = id
        val data: MutableMap<String, Any> = HashMap()

        if (TextUtils.isDigitsOnly(id)) {
            data.apply {
                put("contact_no",id)
                put("api_key",WhiteLabelConstants.API_KEY)
            }
        } else {
            data.apply {
                put("to_email_id",id)
                put("api_key",WhiteLabelConstants.API_KEY)
            }
        }

        viewModelScope.launch {
            _reqOtpResponse.value = Resource.Loading
            _reqOtpResponse.value = repository.reqOtp(data)
        }
    }

    fun verifyOtp(otpEntered: String) {
        val id = Utilities.getPreference(BaseApplication.getContext(), AppConstants.ID).toString()
        val map = HashMap<String,String>()

        if (TextUtils.isDigitsOnly(id))
            map["contact_no"] = id
        else
            map["email_id"] = id

        map.apply {
            put("api_key", WhiteLabelConstants.API_KEY)
            put("otp",otpEntered)
            put("source","App_android")
        }

        viewModelScope.launch {
            _verifyOtpResponse.value = Resource.Loading
            _verifyOtpResponse.value = repository.verityOtp(map)
        }
    }


}