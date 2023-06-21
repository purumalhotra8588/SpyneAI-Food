package com.spyneai.shootapp.data


import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.ClipperApiStagingClient
import com.spyneai.dashboardV2.data.model.LogoutBody
import com.spyneai.food.ImageBody
import com.spyneai.food.MarkDoneBody

class ProcessRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()
    private var clipperStagingApi = ClipperApiStagingClient().getClient()



    suspend fun getBackgroundGifCars(
        map: HashMap<String, Any>
    ) =  safeApiCall {
        clipperApi.getBackgrounds(map)
    }

    suspend fun processSku(
        map: MutableMap<String, Any>
    ) = safeApiCall {
        clipperApi.processSku(map)
    }

    suspend fun updateTotalFrames(
        authKey: String,
        skuId : String,
        totalFrames: String
    ) = safeApiCall {
        clipperApi.updateTotalFrames(authKey,skuId,totalFrames)
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

    suspend fun skuProcessStateWithBackgroundId(
        auth_key: String,
        project_id:  String,
        background_id:  Int
    ) = safeApiCall{
        clipperApi.skuProcessStateWithBackgroundId(auth_key, project_id, background_id)
    }

    suspend fun stableDiffusion(
        imageBody: ImageBody
    ) = safeApiCall {
        clipperApi.stableDiffusion(imageBody)
    }

    suspend fun stableDiffusionMarkDone(
        imageBodyMarkDone: MarkDoneBody
    ) = safeApiCall {
        clipperApi.stableDiffusionMarkDone(imageBodyMarkDone)
    }

}