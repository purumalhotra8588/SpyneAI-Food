package com.spyneai.threesixty.data

import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.SpyneAiApiClient
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ThreeSixtyRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()
    private var spyneApi = SpyneAiApiClient().getClient()





    suspend fun getVideoPreSignedUrl(
        map: HashMap<String, Any>
    )= safeApiCall {
        clipperApi.getVideoPreSignedUrl(map)
    }

//    suspend fun uploadVideo(
//        contentType : String,
//        url : String,
//        file : MultipartBody.Part
//    ) = safeApiCall {
//        clipperApi.uploadVideo(
//            contentType,
//            url,
//            file
//        )
//    }

    suspend fun setStatusUploaded(
        videoId: String
    ) = safeApiCall {
        clipperApi.setStatusUploaded(videoId)
    }

    suspend fun getBackgroundGifCars(
        map: HashMap<String, Any>
    ) = safeApiCall {
        map.apply {
            put("auth_key",
                Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString())
        }
        clipperApi.getBackgrounds(map)
    }

    suspend fun getUserCredits(
        userId : String
    )= safeApiCall {
        clipperApi.userCreditsDetails(userId)
    }

    suspend fun reduceCredit(
        userId : String,
        creditReduce:String,
        skuId: String
    )= safeApiCall {
        clipperApi.reduceCredit(userId, creditReduce,skuId)
    }

    suspend fun updateDownloadStatus(
        userId : String,
       skuId: String,
        enterpriseId: String,
         downloadHd: Boolean
    )= safeApiCall {
        clipperApi.updateDownloadStatus(userId,skuId, enterpriseId, downloadHd)
    }
}