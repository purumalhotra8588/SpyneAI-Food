package com.spyneai.loginsignup.data

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient

class OnBoardingRepository: BaseRepository() {

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

    suspend fun forgetPwd(map: MutableMap<String, String>) = safeApiCall {
        clipperApi.forgetPwd(map)
    }


}