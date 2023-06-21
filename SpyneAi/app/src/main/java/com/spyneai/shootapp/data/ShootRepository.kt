package com.spyneai.shootapp.data

import InvitationEmailBody
import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.GCPSpyneApiClient
import com.spyneai.base.network.ProjectApiClient
import com.spyneai.shootapp.repository.model.image.Image

import com.spyneai.shootapp.repository.model.payment.GenerateOrderBody
import com.spyneai.shootapp.repository.model.payment.PaymentIdBody
import com.spyneai.shootapp.repository.model.payment.SubmitPaidProjectUpdatedBody
import com.spyneai.shootapp.repository.model.project.ProjectBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject


class ShootRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()
    private var projectApi = ProjectApiClient().getClient()


    suspend fun getSampleImages(
    ) = safeApiCall {
        clipperApi.getSampleImages()
    }

    suspend fun getPreSignedUrl(
        map: HashMap<String,Any?>
    ) = safeApiCall {
        clipperApi.
        getPreSignedUrl(map)
    }


    suspend fun getBackgroundGifCars(
        map: HashMap<String, Any>
    ) =  safeApiCall {
        clipperApi.getBackgrounds(map)
    }

    suspend fun getSubCategories(
        authKey : String,prodId : String
    ) = safeApiCall {
        clipperApi.getSubCategories(authKey, prodId)
    }

    suspend fun getOverlays(authKey: String, prodId: String,
                            prodSubcategoryId : String, frames : String) = safeApiCall {
        clipperApi.getOverlays(authKey, prodId, prodSubcategoryId, frames)
    }

    suspend fun processSingleImage(
        map: HashMap<String,Any>
    ) = safeApiCall {
        clipperApi.processSingleImage(map)
    }

    suspend fun getPreSignedUrl(
        uploadType : String,
        imageApp : Image
    ) = safeApiCall {
        clipperApi.getPreSignedUrl(
           imageApp.uuid,
            imageApp.projectId,
            imageApp.skuId,
            imageApp.image_category,
            imageApp.name,
            imageApp.overlayId?.toInt(),
            uploadType,
            imageApp.sequence!!,
            imageApp.isReclick,
            imageApp.isReshoot,
            imageApp.tags,
            imageApp.debugData,
            imageApp.angle!!,
            imageApp.isExtraImage
        )
    }


    suspend fun markUploaded(
        imageId : String
    ) = safeApiCall {
        clipperApi.markUploaded(imageId)
    }


    suspend fun createProject(authKey: String,
                              projectName : String,
                              prodCatId : String,
                              dynamicLayout : JSONObject? = null,
                              location_data : JSONObject? = null
    ) = safeApiCall {
        clipperApi.createProject(authKey, projectName, prodCatId,dynamicLayout,location_data)
    }

    suspend fun createSku(authKey: String,projectId : String
                          ,prodCatId : String,prodSubCatId : String,
                          skuName : String,total_frames : Int,
                          images : Int, videos : Int,

    ) = safeApiCall {
        clipperApi.createSku(authKey, projectId, prodCatId, prodSubCatId, skuName.uppercase(),
            total_frames,
        images,
        videos)
    }

    suspend fun updateVideoSku(
        skuId: String,
        prodSubCatId : String,
        initialImageCount: Int
    )= safeApiCall {
        clipperApi.updateVideoSku(
            skuId,
            prodSubCatId,
            initialImageCount
        )
    }

    suspend fun sendInvitaionEmailId(
        body : InvitationEmailBody
    ) = safeApiCall {
        projectApi.sendInvitaionEmailId(body)
    }


    suspend fun getGCPUrl(
        imageName: String
    )= safeApiCall {
        clipperApi.getGCPUrl(imageName)
    }

    suspend fun getProjectDetail(
        tokenId: String,
        projectId:  String
    ) = safeApiCall{
        clipperApi.getProjectDetail(tokenId, projectId)
    }

    suspend fun updateTotalFrames(
        skuId: String,
        totalFrames:  String,
        authKey:  String
    ) = safeApiCall{
        clipperApi.updateTotalFrames(authKey = authKey,skuId = skuId,totalFrames = totalFrames)
    }
    suspend fun skuProcessState(
        auth_key: String,
        project_id:  String,
    ) = safeApiCall{
        clipperApi.skuProcessState(auth_key, project_id)
    }

    suspend fun submitProject(
        projectId:  String
    ) = safeApiCall{
        clipperApi.submitProject(projectId = projectId)
    }

    suspend fun submitPaidProject(
        submitPaidProjectBody: SubmitPaidProjectUpdatedBody
    ) = safeApiCall{
        projectApi.submitPaidProject(submitPaidProjectBody)
    }

    suspend fun updateFootwearSubcategory(
        authKey: String,
        skuId:  String,
        initialImageCount:  Int,
        subCatId:  String
    ) = safeApiCall{
        clipperApi.updateFootwearSubcategory(authKey, skuId, initialImageCount, subCatId)
    }

    suspend fun skuProcessStateWithBackgroundId(
        auth_key: String,
        project_id:  String,
        background_id:  Int
    ) = safeApiCall{
        clipperApi.skuProcessStateWithBackgroundId(auth_key, project_id, background_id)
    }

    suspend fun skuProcessStateWithShadowOption(
        auth_key: String,
        project_id:  String,
        background_id:  Int,
        shadow:  String
    ) = safeApiCall{
        clipperApi.skuProcessStateWithShadowOption(auth_key, project_id, background_id, shadow)
    }



    suspend fun getProjectName(
        authKey : String
    ) = safeApiCall {
        clipperApi.getProjectName(authKey)
    }

    suspend fun getOverlayIds(ids: JSONArray
    ) = safeApiCall {
        clipperApi.getOverlayIds(ids)
    }

    suspend fun getImageData(
        imageId : String
    ) = safeApiCall {
        clipperApi.getImageData(imageId)
    }

    suspend fun uploadImageToGcp(
        fileUrl : String,
        file : RequestBody,
        contentType: String
    ) = safeApiCall {
        GCPSpyneApiClient().getClient().uploadAppFileToGcp(fileUrl,file,contentType = contentType)
    }

    suspend fun createProject(projectBody: ProjectBody
    )= safeApiCall {
        projectApi.createProject(projectBody)
    }

    suspend fun sendFilesData(
        authKey: String,
        data : String
    ) = safeApiCall {
        clipperApi.sendFilesData(authKey,data)
    }

    suspend fun generateOrder(
        body: GenerateOrderBody
    ) = safeApiCall {
        clipperApi.generateOrder(body)
    }



    suspend fun sendPaymentId(
        body: PaymentIdBody
    ) = safeApiCall {
        clipperApi.sendPaymentId(body)
    }
    suspend fun getUserCredits(
        userId : String
    )= safeApiCall {
        clipperApi.userCreditsDetails(userId)
    }

    suspend fun angleClassifierV2(
        imageFile: MultipartBody.Part,
        prod_cat_id:  RequestBody,
        overlay_id:  RequestBody,
    ) = safeApiCall {
        clipperApi.angleClassifierV2(image_file = imageFile,prod_cat_id =prod_cat_id, overlay_id = overlay_id)
    }

    suspend fun angleClassifier(
        imageFile : MultipartBody.Part,
        requiredAngle:Int,
        cropCheck: Boolean
    ) = safeApiCall {
        clipperApi.angleClassifier(imageFile,requiredAngle,cropCheck)
    }

    suspend fun improveShoot(
    ) = safeApiCall {
        clipperApi.improveShoot()
    }
}