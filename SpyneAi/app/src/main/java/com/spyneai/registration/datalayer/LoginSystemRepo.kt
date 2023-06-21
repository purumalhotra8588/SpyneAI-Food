package com.spyneai.registration.datalayer

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.loginsignup.data.LoginBodyV2
import com.spyneai.onboardingv2.data.UpdateUserDetailBody

class LoginSystemRepo : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()

    suspend fun reqOtp(
        map: MutableMap<String, Any>
    ) = safeApiCall {
        clipperApi.reqOtp(map)
    }

    suspend fun verityOtp(
        map: MutableMap<String, String>
    ) = safeApiCall {
        clipperApi.postOtp(map)
    }

    suspend fun signUp(
        map: MutableMap<String, String>
    ) = safeApiCall {
        clipperApi.signUp(map)
    }

    suspend fun reqOtpV2(
        body:LoginBodyV2
    ) = safeApiCall {
        clipperApi.reqOtpV2(body)
    }

//    suspend fun verityOtpV2(
//        map: MutableMap<String, String>
//    ) = safeApiCall {
//        clipperApi.postOtpV2(map)
//    }


    suspend fun forgetPwd(map: MutableMap<String, String>) = safeApiCall {
        clipperApi.forgetPwd(map)
    }

    suspend fun updateCountry(
        map: HashMap<String, String>
    ) = safeApiCall {
        clipperApi.updateCountry(map)
    }
    suspend fun updateUserDetail(
        body:UpdateUserDetailBody
    ) = safeApiCall {
        clipperApi.updateUserDetail(body)
    }

    suspend fun createEnterPrise(map: MutableMap<String, String>) = safeApiCall {
        clipperApi.createEnterPrise(map)
    }

    suspend fun login(map: HashMap<String, Any?>) = safeApiCall {
        clipperApi.loginWithPassword(map)
    }



}