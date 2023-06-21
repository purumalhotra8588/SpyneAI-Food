package com.spyneai.base.network

import androidx.lifecycle.LiveData
import com.spyneai.FilesDataRes
import com.spyneai.app.BaseApplication
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.credits.model.*
import com.spyneai.dashboard.data.model.RidResponse
import com.spyneai.dashboard.data.model.UserDetailsResponse
import com.spyneai.dashboard.network.Resource
import com.spyneai.dashboard.repository.model.*
import com.spyneai.dashboard.response.*
import com.spyneai.dashboardV2.data.model.*
import com.spyneai.food.ImageBody
import com.spyneai.food.MarkDoneBody
import com.spyneai.food.StableDiffusionMarkDoneResponse
import com.spyneai.food.StableDiffusionResponse
import com.spyneai.dashboardV2.data.model.LogoutBody
import com.spyneai.dashboardV2.data.model.LogoutResponse
import com.spyneai.loginsignup.data.LoginBodyV2
import com.spyneai.loginsignup.models.ForgotPasswordResponse
import com.spyneai.model.FetchCategoryRes
import com.spyneai.model.credit.AvailableCreditResponse
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.model.credit.PricingPlanResponse
import com.spyneai.model.credit.SubscriptionPlanResponse
import com.spyneai.model.login.LoginResponse
import com.spyneai.model.otp.LoginResponseV2
import com.spyneai.model.otp.OtpResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.data.MessageRes
import com.spyneai.onboardingv2.data.SampleImagesRes
import com.spyneai.onboardingv2.data.UpdateUserDetailBody
import com.spyneai.onboardingv2.data.UpdateUserDetailRes
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.data.response.GetOngoingSkusResponse
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.registration.models.ChangeAdminDTO
import com.spyneai.registration.models.CreateEnterPriseDTO
import com.spyneai.reshoot.data.ReshootOverlaysRes
import com.spyneai.reshoot.data.ReshootSkuRes
import com.spyneai.shootapp.data.model.*
import com.spyneai.shootapp.repository.model.payment.GenerateOrderBody
import com.spyneai.shootapp.repository.model.payment.GenerateOrderRes
import com.spyneai.shootapp.repository.model.payment.PaymentIdBody
import com.spyneai.shootapp.repository.model.payment.PaymentIdRes
import com.spyneai.shootapp.response.SkuProcessStateResponse
import com.spyneai.shootapp.response.UpdateVideoSkuRes
import com.spyneai.singleimageprocessing.data.SingleImageProcessRes
import com.spyneai.threesixty.data.model.VideoPreSignedRes
import com.spyneai.threesixty.data.response.ProcessThreeSixtyRes
import com.spyneai.threesixty.data.response.VideoUploadedRes
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface ClipperApi {

    @FormUrlEncoded
    @POST("api/v4/image/upload")
    suspend fun getPreSignedUrl(
        @Field("local_id") localId: String,
        @Field("project_id") project_id: String?,
        @Field("sku_id") sku_id: String?,
        @Field("image_category") image_category: String?,
        @Field("image_name") image_name: String?,
        @Field("overlay_id") overlay_id: Int?,
        @Field("upload_type") upload_type: String?,
        @Field("frame_seq_no") frame_seq_no: Int?,
        @Field("is_reclick") is_reclick: Boolean?,
        @Field("is_reshoot") isReshoot: Boolean?,
        @Field("tags") tags: String?,
        @Field("debug_data") debugData: String?,
        @Field("angle") angle: Int,
        @Field("is_extra_image") isExtraImage: Boolean,
        @Field("source") source: String = "App_android",
        @Field("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): ImagePreSignedRes

    @FormUrlEncoded
    @POST("api/v4/image/mark-done")
    suspend fun markUploaded(
        @Field("image_id") imageId: String,
        @Field("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): ImagePreSignedRes


    @GET("api/v2/product/fetch")
    suspend fun getCategories(
        @Query(
            "auth_key"
        ) authKey: String
    ): NewCategoriesResponse

    @GET("api/v2/prod/sub/fetch/v2")
    suspend fun getSubCategories(
        @Query("auth_key") authKey: String,
        @Query("prod_id") prodId: String
    ): NewSubCatResponse

    @GET("api/v2/overlays/fetch")
    suspend fun getOverlays(
        @Query("auth_key") authKey: String,
        @Query("prod_id") prodId: String,
        @Query("prod_sub_cat_id") prodSubcatId: String,
        @Query("no_of_frames") frames: String,
    ): OverlaysResponse

    @FormUrlEncoded
    @POST("api/v2/project/create/v2")
    suspend fun createProject(
        @Field("auth_key") authKey: String,
        @Field("project_name") projectName: String,
        @Field("prod_cat_id") prodCatId: String,
        @Field("dynamic_layout") dynamic_layout: JSONObject? = null,
        @Field("location_data") location_data: JSONObject? = null,
        @Field("source") source: String = "App_android"
    ): CreateProjectRes

    @FormUrlEncoded
    @POST("api/v2/sku/create/v2")
    suspend fun createSku(
        @Field("auth_key") authKey: String,
        @Field("project_id") projectId: String,
        @Field("prod_cat_id") prodCatId: String,
        @Field("prod_sub_cat_id") prodSubCatId: String,
        @Field("sku_name") skuName: String,
        @Field("total_frames") totalFrames: Int,
        @Field("images") images: Int,
        @Field("videos") videos: Int,
        @Field("source") source: String = "App_android"
    ): CreateSkuRes

    @FormUrlEncoded
    @POST("api/v3/video/videoimages-update")
    suspend fun updateVideoSku(
        @Field("sku_id") skuId: String,
        @Field("prod_sub_cat_id") prodSubCatId: String,
        @Field("initial_image_count") initialImageCount: Int,
        @Field("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString(),
        @Field("images") images: Int = 1
    ): UpdateVideoSkuRes

    @GET("api/nv1/background/fetch-enterprise-bgs")
    suspend fun getBackgrounds(
        @QueryMap map: MutableMap<String, Any>
    ): CarsBackgroundRes

    @FormUrlEncoded
    @POST("api/nv1/category-agnostic/create-image-process-environment")
    suspend fun processSku(
        @FieldMap map: MutableMap<String, Any>
    ): ProcessSkuResV2

    @GET("api/v2/sku/getOngoingSKU")
    suspend fun getOngoingSKUs(
        @Query("auth_key") authKey: String
    ): GetOngoingSkusResponse

    @GET("api/v2/sku/getCompSKU")
    suspend fun getCompletedSkus(
        @Query("auth_key") authKey: String
    ): CompletedSKUsResponse

    @GET("api/nv1/app/get-images-by-sku-id")
    suspend fun getImagesOfSku(
        @Query("skuId") skuId: String,
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): ImagesOfSkuRes

    @GET("api/v2/overlays/fetch-ids")
    suspend fun getOverlayIds(
        @Query("overlay_ids") overlayId: JSONArray,
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): ReshootOverlaysRes

    @GET("api/v2/project/getSkuPerProject")
    suspend fun getProjectDetail(
        @Query("auth_key") authKey: String,
        @Query("project_id") projectId: String
    ): ProjectDetailResponse

    @GET("api/v2/sku/updateTotalFrames")
    suspend fun updateTotalFrames(
        @Query("auth_key") authKey: String,
        @Query("sku_id") skuId: String,
        @Query("total_frames") totalFrames: String
    ): UpdateTotalFramesRes

    @GET("api/v3/project/getDetailsProject")
    suspend fun getProjects(
        @Query("auth_key") authKey: String,
        @Query("status")
        zstatus: String
    ): GetProjectsResponse


    @GET("api/v3/project/getDetailsProject")
    suspend fun getDrafts(
        @Query("auth_key") authKey: String,
        @Query("status") status: String
    ): GetProjectsResponse


    @Multipart
    @POST("api/v2/video/upload_two")
    suspend fun process360(
        @Part("auth_key") authKey: RequestBody,
        @Part("type") type: RequestBody,
        @Part("project_id") projectId: RequestBody,
        @Part("sku_name") skuName: RequestBody,
        @Part("sku_id") skuId: RequestBody,
        @Part("category") category: RequestBody,
        @Part("sub_category") subCategory: RequestBody,
        @Part("frames") frames: RequestBody,
        @Part("background_id") backgroundId: RequestBody,
        @Part videoFile: MultipartBody.Part,
        @Part("video_url") videoUrl: RequestBody? = null,
    ): ProcessThreeSixtyRes

    @FormUrlEncoded
    @POST("api/v3/video/video-upload")
    suspend fun getVideoPreSignedUrl(
        @FieldMap map: HashMap<String, Any>
    ): VideoPreSignedRes


    @FormUrlEncoded
    @PUT("api/v3/video/video-mark")
    suspend fun setStatusUploaded(
        @Field("video_id") videoId: String,
        @Field("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): VideoUploadedRes


    @GET("api/v2/credit/fetch")
    suspend fun userCreditsDetails(
        @Query("auth_key") userId: String
    ): CreditDetailsResponse

    @GET("credit/api/v1/users/available-credits")
    suspend fun availableCredits(): AvailableCreditResponse

    @GET("api/nv1/entity/fetch-entity-credit")
    suspend fun userCreditsDetails(
        @Query("auth_key") authKey: String?,
        @Query("entity_id") entityId: String?,
    ): CreditDetailsResponse

    @FormUrlEncoded
    @PUT("api/v2/credit/reduce-user-credit")
    suspend fun reduceCredit(
        @Field("auth_key") authKey: String,
        @Field("credit_reduce") creditReduce: String,
        @Field("sku_id") skuId: String,
        @Field("source") source: String = "App_android",
        @Field("image_id") imageId: String = ""
    ): ReduceCreditResponse

    @FormUrlEncoded
    @POST("api/v4/update-download-status")
    suspend fun updateDownloadStatus(
        @Field("user_id") userId: String,
        @Field("sku_id") skuId: String,
        @Field("enterprise_id") enterpriseId: String,
        @Field("download_hd") downloadHd: Boolean
    ): DownloadHDRes

    @FormUrlEncoded
    @POST("api/v2/sku/skuProcessStatus")
    suspend fun skuProcessState(
        @Field("auth_key") auth_key: String?,
        @Field("project_id") project_id: String?,
        @Field("shoot_done") shootDone: Boolean = true
    ): SkuProcessStateResponse

    @POST("api/nv1/photographer/project-mark-done")
    @FormUrlEncoded
    suspend fun submitProject(
        @Field("projectId") projectId: String,
        @Field("status") status: String = "Yet to Start",
        @Field("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): MessageRes

    @FormUrlEncoded
    @POST("api/v2/sku/skuProcessStatus")
    suspend fun skuProcessStateWithBackgroundId(
        @Field("auth_key") auth_key: String?,
        @Field("project_id") project_id: String?,
        @Field("background_id") background_id: Int?,
    ): SkuProcessStateResponse

    @FormUrlEncoded
    @POST("api/v2/sku/skuProcessStatus")
    suspend fun skuProcessStateWithShadowOption(
        @Field("auth_key") auth_key: String?,
        @Field("project_id") project_id: String?,
        @Field("background_id") background_id: Int?,
        @Field("shadow") shadow: String?,
    ): SkuProcessStateResponse


    @FormUrlEncoded
    @PATCH("api/v2/sku/update-iim")
    suspend fun updateFootwearSubcategory(
        @Field("auth_key") authKey: String,
        @Field("sku_id") skuId: String,
        @Field("initial_image_count") initialImageCount: Int,
        @Field("sub_cat_id") subCatId: String,
    ): UpdateFootwearSubcatRes





    @GET("api/v2/user/project-sku-data")
    suspend fun getProjectName(
        @Query("auth_key") authKey: String,
    ): GetProjectNameResponse


    @GET("api/algo/save_to_gcp_presigned/presigned-url")
    suspend fun getGCPUrl(
        @Query("img_name") imageName: String,
    ): GetGCPUrlRes

    @FormUrlEncoded
    @PUT("api/fv1/upload/image")
    suspend fun getPreSignedUrl(
        @FieldMap map: MutableMap<String, Any?>
    ): GetGCPUrlRes

    @FormUrlEncoded
    @POST("api/algo/attendence/check-in-out")
    suspend fun captureCheckInOut(
        @Field("location") location: JSONObject,
        @Field("location_id") locationId: String,
        @Field("img_url") imageUrl: String = "",
        @Field("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): CheckInOutRes

    @FormUrlEncoded
    @POST("api/v2/image/get")
    suspend fun getImageData(
        @Field("image_id") imageId: String,
        @Field("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): GetImageRes

    @GET("api/algo/manual_location_data/get-data")
    suspend fun getLocations(
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): LocationsRes


    //login(Email and password)
    @FormUrlEncoded
    @POST("api/v2/user/login")
    suspend fun loginWithPassword(
        @FieldMap map: MutableMap<String, Any?>
    ): com.spyneai.onboarding.data.models.LoginWithPasswordRes

    //signup(create user using email/phone)
    @FormUrlEncoded
    @POST("api/v2/user/signup")
    suspend fun signUp(
        @FieldMap map: MutableMap<String, String>
    ): com.spyneai.onboarding.data.models.SignupResponse

    //requestOtp(email)
    @FormUrlEncoded
    @POST("api/v2/user/request-otp")
    suspend fun loginWithOTPEmail(
        @Field("email_id") email_id: String,
        @Field("api_key") apiKey: String
    ): OtpResponse

    //requestOtp(phone)
    @FormUrlEncoded
    @POST("api/v2/user/request-otp")
    suspend fun loginWithOTPPhone(
        @Field("contact_no") contact_no: Int,
        @Field("api_key") apiKey: String
    ): OtpResponse


    @GET("api/v2/user/countries")
    suspend fun getCountries(): com.spyneai.onboarding.data.models.GetCountriesResponse


    @GET("api/v2/product/fetch")
    fun getCategoriesData(
        @Query(
            "auth_key"
        ) authKey: String
    ): LiveData<Resource<CategoryAgnosticResponse>>

    @GET("api/nv1/category-agnostic/get-category-data")
    fun getLivaDataCategoryById(
        @Query("categoryId") categoryId: String,
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString(),
    ): LiveData<Resource<CatAgnosticResV2>>

    @GET("api/nv1/category-agnostic/get-category-data")
    suspend fun getCategoryById(
        @Query("categoryId") categoryId: String,
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString(),
    ): CatAgnosticResV2


    @FormUrlEncoded
    @POST("api/v2/image/user-data")
    suspend fun sendFilesData(
        @Field("auth_key") authKey: String,
        @Field("data") skuId: String
    ): FilesDataRes



    @GET("api/nv1/projects/entity-android-home-page-data")
    suspend fun userDetails(
        @Query("auth_key") authKey: String?,
        @Query("entity_id") entityId: String?,
    ): UserDetailsResponse

    @GET("api/v2/credit/fetch")
    suspend fun fetchCredits(
        @Query("auth_key") userId: String?,
    ): CreditDetailsResponse

    @GET("api/nv1/entity/fetch-user-data")
    suspend fun getRestaurantList(
        @Query("auth_key") authKey: String?,
    ): RidResponse

    @FormUrlEncoded
    @POST("api/nv1/entity/request-credit")
    suspend fun requestCredits(
        @Field("auth_key") authKey: String
    ): RequestCreditResponse

    @FormUrlEncoded
    @POST("api/v2/user/request-otp")
    suspend fun reqOtp(
        @FieldMap map: MutableMap<String, Any>
    ): LoginResponse


    @POST("user-management/v1/auth/login")
    suspend fun reqOtpV2(
        @Body body: LoginBodyV2
    ): LoginResponseV2

    @FormUrlEncoded
    @POST("api/v2/user/validate-otp")
    suspend fun postOtp(
        @FieldMap map: MutableMap<String, String>
    ): OtpResponse

//
//    @FormUrlEncoded
//    @POST("user-management/v1/auth/login")
//    suspend fun postOtpV2(
//        @Body body: LoginBodyV2
//    ): LoginResponseV2


    @GET("api/nv1/sample-images/get-sample-images")
    suspend fun getSampleImages(
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString(),
        @Query("categoryId") categoryId: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.SELECTED_CATEGORY_ID
        ).toString()
    ): SampleImagesRes


    @FormUrlEncoded
    @POST("api/fv1/singleprocess/process")
    suspend fun processSingleImage(
        @FieldMap map: MutableMap<String, Any>
    ): SingleImageProcessRes

    @FormUrlEncoded
    @PUT("api/fv1/users/update-details")
    suspend fun updateCountry(
        @FieldMap map: MutableMap<String, String>
    ): MessageRes


    @PUT("api/nv1/user/profile")
    suspend fun updateUserDetail(
        @Body body: UpdateUserDetailBody
    ): UpdateUserDetailRes

    @POST("api/tv1/pricing/generate-order")
    suspend fun generateOrder(
        @Body body: GenerateOrderBody
    ): GenerateOrderRes


    @POST("api/tv1/pricing/insert-payment")
    suspend fun sendPaymentId(
        @Body body: PaymentIdBody
    ): PaymentIdRes


    @Multipart
    @POST("api/fv1/image/angle-classifier")
    suspend fun angleClassifier(
        @Part image_file: MultipartBody.Part,
        @Part("required_angle") required_angle: Int,
        @Part("crop_check") crop_check: Boolean,
        @Part("is_subCategory_classifier_applied") isSubCategoryClassifierApplied: Boolean = true,
        @Part("is_general_classifier_applied") isGeneralClassifierApplied: Boolean = true,
        @Part("distance") distance: Boolean = true,
        @Part("reflection") reflection: Boolean = true,
        @Part("exposure") exposure: Boolean = true,
        @Header("bearer-token") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): AngleClassifierRes

    @Multipart
    @POST("api/fv2/image/angle-classifier")
    suspend fun angleClassifierV2(
        @Part image_file: MultipartBody.Part,
        @Part("prod_cat_id") prod_cat_id: RequestBody,
        @Part("overlay_id") overlay_id: RequestBody,
        @Header("bearer-token") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString(),
    ): AngleClassifierResponseV2


    @PUT
    fun uploadVideo(
        @Header("content-type") contentType: String,
        @Url uploadUrl: String,
        @Body file: RequestBody
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("api/nv1/token/generate")
    suspend fun forgetPwd(
        @FieldMap map: MutableMap<String, String>,
        @Header("Lead-Gen") LeadGen: Boolean = true
    ): ForgotPasswordResponse


    @FormUrlEncoded
    @POST("api/v2/enterprise/create")
    suspend fun createEnterPrise(
        @FieldMap map: MutableMap<String, String>
    ): CreateEnterPriseDTO

    @GET("api/v2/enterprise/insert-data-for-enterprise")
    suspend fun createAdmin(
        @QueryMap map: MutableMap<String, Any>
    ): ChangeAdminDTO

    // car-inspection







    @GET("api/nv2/enterprise/categories")
    suspend fun fetchCategory(
        @Query("authKey") authKey: String =
            Utilities.getPreference(
                BaseApplication.getContext(),
                AppConstants.AUTH_KEY
            ).toString(),
        @Query("appType") appType : String = "non_auto"
    ): FetchCategoryRes


    @GET("api/dv1/account-executive/get-invoices")
    suspend fun getSubscriptionPlan(
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): SubscriptionPlanResponse

//    @PATCH("console/v1/user/update-profile")
//    suspend fun updateUser(
//        @Body userResourceBody: UpdateUserBody,
//        @Header("content-type") contentType: String = "application/json"
//    ): UpdateUserResponse

    @GET("api/nv1/pricing-plan/usage")
    suspend fun getPricingPlan(
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): PricingPlanResponse


    @GET("api/nv1/reshoot/get-sku-data")
    suspend fun getReshootSkus(
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): ReshootSkuRes


    @POST("api/nv1/app/project-status-update")
    suspend fun projectStatusUpdate(@Body projectStatusBody: ProjectStatusBody): MessageRes

    @PATCH("credit/api/v1/users/deduct-credits")
    suspend fun deductCredits(
        @Body creditResourceBody: CreditResourceBody,
        @Header("content-type") contentType: String = "application/json"
    ): DeductCreditResponse


    @POST("credit/api/v1/users/calculate-credits")
    suspend fun calculateCredits(
        @Body creditResourceBody: CreditResourceBody,
        @Header("Content-Type") contentType: String = "application/json"
    ): CalculateCreditResponse




    @GET("api/nv1/inspection/get-presigned-url")
    suspend fun getGCPUrl(
        @Query("registrationNumber") RegistrationNumber: String,
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): GetGCPUrlRes




    @GET("credit/api/v1/generic/transaction-history")
    suspend fun getTransactionHistory(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int = 20,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String,
        @Query("actionType") actionType: String
    ): TransactionHistoryResponse




    @GET("api/nv1/app/get-images-by-sku-id")
    suspend fun updateProcessedImageState(
        @Query("skuId") skuId: String,
        @Query("imageId") imageId: String,
        @Query("auth_key") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): ImagesOfSkuRes

//    @POST("console/v1/user/app/refresh-token")
//    suspend fun refreshToken(
//        @Body refreshTokenBody: RefreshTokenBody,
//    ): RefreshTokenResponse

    @POST("user-management/v1/user/logout")
    suspend fun newUserLogout(
        @Body logoutBody: LogoutBody,
    ): LogoutResponse





    @GET("api/nv1/app/get-guidelines")
    suspend fun improveShoot(
        @Query("prod_cat_id") prodCatId : String = "cat_Ujt0kuFxY",
        @Header("bearer-token") authKey: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.AUTH_KEY
        ).toString()
    ): ImproveShootResponse


    @POST("api/nv1/app/stable-diffusion")
    suspend fun stableDiffusion(
        @Body imageBody: ImageBody
    ) : StableDiffusionResponse

    @POST("api/nv1/app/stable-diffusion-mark-done")
    suspend fun stableDiffusionMarkDone(
        @Body imageBodyMarkDone: MarkDoneBody
    ) : StableDiffusionMarkDoneResponse
}