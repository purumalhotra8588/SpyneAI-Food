package com.spyneai.base.network


import InvitationEmailBody
import com.spyneai.app.BaseApplication
import com.spyneai.draft.data.PagedSkuRes
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.data.MessageRes
import com.spyneai.orders.data.paging.ProjectPagedRes
import com.spyneai.reshoot.data.ReshootProjectRes
import com.spyneai.shootapp.repository.model.payment.SubmitPaidProjectUpdatedBody
import com.spyneai.shootapp.repository.model.project.CreateProjectAndSkuRes
import com.spyneai.shootapp.repository.model.project.ProjectBody
import com.spyneai.shootapp.response.InvitationEmailIdRes
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ProjectApi {

    @POST("api/nv1/projects/offline-create-project-skus")
    suspend fun createProject(@Body projectBody: ProjectBody): CreateProjectAndSkuRes

    @GET("api/nv1/app/get-project")
    suspend fun getPagedProjects(
        @Query("pageNo") pageNo: Int,
        @Query("count") count: Int = 10,
        @Query("shootType") shootType: String,
        @Query("sortBy") sortBy: String = "DESC",
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString(),
        @Query("status") status: String = "draft",
        @Query("qcData") qcData: Boolean = Utilities.getBool(
            BaseApplication.getContext(),
            AppConstants.CHECK_QC,
            false
        )
    ): ProjectPagedRes


    @GET("api/nv1/app/get-project-sku")
    suspend fun getPagedSku(
        @Query("pageNo") pageNo: Int,
        @Query("projectId") projectId: String,
        @Query("count") count: Int = 50,
        @Query("sortBy") sortBy: String = "DESC",
        @Query("videoData") videoData: Int,
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString(),
        @Query("qcData") qcData: Boolean = Utilities.getBool(
            BaseApplication.getContext(),
            AppConstants.CHECK_QC,
            false
        )
    ): PagedSkuRes


    @POST("api/nv1/app/project-status-update")
    suspend fun submitPaidProject(
        @Body projectSubmitBody: SubmitPaidProjectUpdatedBody
    ): MessageRes


    @GET("api/nv1/reshoot/get-project-data")
    suspend fun getReshootProjectData(
        @Query("projectId") projectId: String,
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): ReshootProjectRes

    @POST("api/nv1/deeplink/mail-invite")
    suspend fun sendInvitaionEmailId(
        @Body body: InvitationEmailBody
    ): InvitationEmailIdRes

}