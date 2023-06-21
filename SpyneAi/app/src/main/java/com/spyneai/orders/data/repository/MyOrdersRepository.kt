package com.spyneai.orders.data.repository

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.ProjectApiClient
import com.spyneai.orders.data.response.ProjectCountResponse

class MyOrdersRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()

    private var projectApi = ProjectApiClient().getClient()

    suspend fun getProjectCount(
        tokenId: String
    ) = ProjectCountResponse(12, 32)

    suspend fun getOngoingSKUs(
        tokenId: String
    ) = safeApiCall {
        clipperApi.getOngoingSKUs(tokenId)
    }

    suspend fun getCompletedProjects() = safeApiCall {
        projectApi.getPagedProjects(
            pageNo = 0,
            status = "completed",
            count = 20,
            shootType=""
        )
    }

    suspend fun getProjects(
        tokenId: String,
        status: String
    ) = safeApiCall {
        clipperApi.getProjects(tokenId, status)
    }

    suspend fun getDrafts(
        tokenId: String,
        status: String
    ) = safeApiCall {
        clipperApi.getDrafts(tokenId, status)
    }

    suspend fun getReshootSkus() = safeApiCall {
        clipperApi.getReshootSkus()
    }

    suspend fun getReshootProjectData(projectId: String) = safeApiCall {
        projectApi.getReshootProjectData(projectId)
    }


}