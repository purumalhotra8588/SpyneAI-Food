package com.spyneai.shootapp.repository.model.image

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants
import com.spyneai.shootapp.utils.objectToString
import org.json.JSONObject
import java.util.ArrayList

@Dao
interface ImageDao {
    @Query("Select path from image where skuUuid = :skuUuid and isHidden = :isHidden")
    fun getImagesPathBySkuId(skuUuid: String, isHidden: Boolean = false): List<String>

    @Query("select * from image where skuUuid = :skuUuid and isHidden = :isHidden and (overlayId NOT NULL and overlayId != '')")
    fun getImagesBySkuUuid(skuUuid: String, isHidden: Boolean = false): List<Image>

    @Query("Select path from image where skuUuid = :skuUuid")
    fun getImagesPathBySkuIdLiveData(skuUuid: String): LiveData<List<String>>

    @Query("select * from image where isHidden = :isHidden and skuId NOT NUll and projectId NOT NULL and (isUploaded = :isUploaded or isMarkedDone = :isMarkedDone) and imageState != :imageState and overlayId != '' and toProcessAt <= :currentTime limit 1")
    fun getOldestImage(
        isUploaded: Boolean = false,
        isMarkedDone: Boolean = false,
        currentTime: Long = System.currentTimeMillis(),
        isHidden: Boolean = false,
        imageState: ImageState = ImageState.CLASSIFICATION_PENDING
    ): Image

    @Query("select * from image where skuId NOT NUll and projectId NOT NULL and (imageState = :imageState) and toProcessAt <= :currentTime limit 1")
    fun getImageForClassification(
        imageState: ImageState = ImageState.CLASSIFICATION_PENDING,
        currentTime: Long = System.currentTimeMillis()
    ): Image

    @Query("select * from image where skuId = :skuId and (imageState = :imageState) limit 1")
    fun getReuploadImageForClassification(
        skuId: String,
        imageState: ImageState = ImageState.CLASSIFICATION_PENDING
    ): Image

    @Query("update image set toProcessAt = :toProcessAt, retryCount = retryCount + 1 where uuid = :uuid")
    fun skipImage(uuid: String, toProcessAt: Long): Int

    @Query("update image set isUploaded = :done,isMarkedDone = :done where uuid = :uuid")
    fun markDone(uuid: String, done: Boolean = true): Int

    @Query("update image set preSignedUrl = :preUrl, imageId = :imageId where uuid = :uuid")
    fun addPreSignedUrl(uuid: String, preUrl: String, imageId: String): Int

    @Query("update image set isUploaded = :isUploaded where uuid = :uuid")
    fun markUploaded(uuid: String, isUploaded: Boolean = true): Int

    @Query("update image set imageState = :imageState where uuid = :uuid")
    fun markUploading(uuid: String, imageState: ImageState = ImageState.UPLOADING): Int

    @Query("update image set isMarkedDone = :isMarkedDone, imageState = :imageState where uuid = :uuid")
    fun markStatusUploaded(
        uuid: String,
        isMarkedDone: Boolean = true,
        imageState: ImageState = ImageState.UPLOADED
    ): Int


    @Query("update image set imageState = :imageState where uuid = :uuid")
    fun markStatusQueued(uuid: String, imageState: ImageState = ImageState.QUEUED): Int

    @Query("update image set imageState = :imageState, classificationStatus = :classificationStatus, classificationResult = :classificationResponse, overlayId = :overlayId, image_category = :category  where uuid = :uuid")
    fun updateClassificationResponse(
        uuid: String,
        classificationStatus: String,
        classificationResponse: String,
        category: String,
        overlayId: String,
        imageState: ImageState = ImageState.QUEUED
    ): Int

    @Query("update image set imageState = :imageState, classificationStatus = :classificationStatus, classificationResult = :classificationResponse, image_category = :category  where uuid = :uuid")
    fun updateInteriorClassificationResponse(
        uuid: String,
        classificationStatus: String,
        classificationResponse: String,
        category: String,
        imageState: ImageState = ImageState.QUEUED
    ): Int

    @Query("update image set isHidden = :isHidden where imageId IN (:listOfImageIds)")
    fun hideImagesInDB(listOfImageIds: List<String>, isHidden: Boolean = true): Int

    @Query("select * from image where uuid = :uuid")
    fun getImage(uuid: String): Image

    @Query("select * from image where imageId = :imageId")
    fun getImageWithImageId(imageId: String?): Image

    @Query("select * from image where skuId = :skuId and overlayId = :overlayId")
    fun getImage(skuId: String, overlayId: String): Image

    @Query("select * from image where skuUuid = :skuUuid and overlayId = :overlayId")
    fun getImageBySkuUuid(skuUuid: String, overlayId: String): Image

    @Update
    fun updateImage(image: Image): Int

    @Query("update image set path = :path and isReclick = :isReclick and isUploaded = :isUploaded and isMarkedDone = :isMarkedDone and toProcessAT = :toProcessAT and preSignedUrl = :preSignedUrl where  uuid = :uuid")
    fun updateImagePathOnReClick(
        path: String,
        uuid: String,
        isReclick: Boolean = true,
        isUploaded: Boolean = false,
        isMarkedDone: Boolean = false,
        toProcessAT: Long = System.currentTimeMillis(),
        preSignedUrl: String = AppConstants.DEFAULT_PRESIGNED_URL
    ): Int

    @Query("update image set path = :path and toProcessAT = :toProcessAT where  uuid = :uuid")
    fun updateImagePathOnReUpload(
        path: String,
        uuid: String,
        toProcessAT: Long = System.currentTimeMillis(),
    ): Int

    @Transaction
    suspend fun insertImagesWithCheck(
        response: ArrayList<Image>,
        projectUuid: String,
        skuUuid: String
    ) {
        val list = ArrayList<Image>()

        response.forEach {
            if (it.qcStatus == null)
                it.qcStatus = "in_progress"

            var dbItem = getImageWithImageId(it.imageId)

            if (dbItem == null && it.uuid != null)
                dbItem = getImage(it.uuid)

            if (dbItem == null) {
                if (it.uuid.isNullOrEmpty()) {
                    it.uuid = getUuid()
                }

                if (it.overlayId == null)
                    it.overlayId = "1234"

                if (it.output_image_hres_url == null)
                    it.output_image_hres_url = ""

                if (it.output_image_lres_url == null)
                    it.output_image_lres_url = ""

                if (it.output_image_lres_wm_url == null)
                    it.output_image_lres_wm_url = ""

                it.projectUuid = projectUuid
                it.skuUuid = skuUuid
                it.path = it.input_image_lres_url

                if (it.status == "Done" || it.status == "done" || it.status == "Yet to Start" || it.status == "Failed") {
                    it.isMarkedDone = true
                    it.imageState = ImageState.UPLOADED
                }

                it.isUploaded = true
                it.createdAt = System.currentTimeMillis()
                it.updatedAt = System.currentTimeMillis()
                it.toProcessAT = System.currentTimeMillis()
                it.preSignedUrl = AppConstants.DEFAULT_PRESIGNED_URL

                if (!it.name.contains("."))
                    it.name = it.name + ".jpg"

                if (!it.output_image_lres_url.isNullOrEmpty())
                    it.imageState = ImageState.PROCESSED

                if (!it.classificationResult.isNullOrEmpty()) {

                    val data = JSONObject(it.classificationResult)

                    Log.d("Sync", "insertImagesWithCheck: $data")

                    it.classificationResult = it.classificationResult

                    Log.d("Sync", "insertImagesWithCheck: ${data.has("classification_status")}")
                    if (data.has("classification_status")) {
                        var classificationStatus =
                            data.getString("classification_status")
                        it.classificationStatus = classificationStatus
                    }
                }

                Log.d("Sync", "it: ${it.objectToString()}")

                list.add(it)
            } else {
                if (it.input_image_lres_url != dbItem.input_image_lres_url && !it.input_image_lres_url.isNullOrEmpty())
                    dbItem.input_image_lres_url = it.input_image_lres_url

                if (it.input_image_hres_url != dbItem.input_image_hres_url && !it.input_image_hres_url.isNullOrEmpty())
                    dbItem.input_image_hres_url = it.input_image_hres_url

                if (it.output_image_lres_url != dbItem.output_image_lres_url && !it.output_image_lres_url.isNullOrEmpty())
                    dbItem.output_image_lres_url = it.output_image_lres_url

                if (it.output_image_hres_url != dbItem.output_image_hres_url && !it.output_image_hres_url.isNullOrEmpty())
                    dbItem.output_image_hres_url = it.output_image_hres_url

                if (it.output_image_lres_wm_url != dbItem.output_image_lres_wm_url && !it.output_image_lres_wm_url.isNullOrEmpty())
                    dbItem.output_image_lres_wm_url = it.output_image_lres_wm_url

                if (dbItem.skuUuid != skuUuid)
                    dbItem.skuUuid = skuUuid

                if (dbItem.image_category != it.image_category)
                    dbItem.image_category = it.image_category

                if ((it.status == "Done" || it.status == "done" || it.status == "Yet to Start" || it.status == "Failed") && dbItem.preSignedUrl != AppConstants.DEFAULT_PRESIGNED_URL) {
                    dbItem.isMarkedDone = true
                    dbItem.isUploaded = true
                }

                if (!it.name.contains("."))
                    it.name = it.name + ".jpg"

                if (it.name != dbItem.name)
                    dbItem.name = it.name

                if (it.qcStatus != dbItem.qcStatus)
                    dbItem.qcStatus = it.qcStatus

                dbItem.createdAt = System.currentTimeMillis()
                dbItem.updatedAt = System.currentTimeMillis()
                dbItem.toProcessAT = System.currentTimeMillis()

                if (!dbItem.output_image_lres_url.isNullOrEmpty())
                    dbItem.imageState = ImageState.PROCESSED

                dbItem.reshootComment = it.reshootComment

                updateImage(dbItem)
            }
        }

        insertAllImages(list)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllImages(imageList: List<Image>): List<Long>

    @Query("select Count(*) from image where isUploaded = :isUploaded or isMarkedDone = :isMarkedDone")
    fun totalRemainingUpload(isUploaded: Boolean = false, isMarkedDone: Boolean = false): Int

    @Query("select Count(*) from image where imageState = :imageState")
    fun getTotalRemainingClassification(imageState: ImageState = ImageState.CLASSIFICATION_PENDING): Int

    @Query("select * from image where skuUuid = :skuUuid and isHidden = :isHidden")
    fun getImagesBySkuId(skuUuid: String, isHidden: Boolean = false): List<Image>

    @Query("select path from image where skuUuid = :skuUuid and image_category = :imageCategory")
    fun getExteriorImages(
        skuUuid: String,
        imageCategory: String = "Exterior"
    ): LiveData<List<String>>

    @Query("select path from image where (isUploaded = :isUploaded or isMarkedDone = :isMarkedDone) and skuId = :skuId")
    fun getImagePath(
        skuId: String,
        isUploaded: Boolean = false,
        isMarkedDone: Boolean = false
    ): List<String>

    @Query("select * from image where isHidden = :isHidden")
    fun getAllImages(isHidden: Boolean = false): List<Image>

    @Query("select Count(*) from image where skuId = :skuId and overlayId != '' and isHidden = :isHidden")
    fun getTotalFrames(skuId: String, isHidden: Boolean = false): Int?

    @Query("select * from image where isMarkedDone = :isMarkedDone and isHidden = :isHidden ORDER BY createdAt DESC")
    fun getUploadedImage(isMarkedDone: Boolean = false, isHidden: Boolean = false): List<Image>

    @Query("select * from image where isMarkedDone = :isMarkedDone and imageState = :imageState and skuId= :skuId LIMIT 1")
    fun getFirstUploadedImage(
        isMarkedDone: Boolean = true,
        imageState: ImageState = ImageState.UPLOADED,
        skuId: String
    ): Image?

    @Query("select Count(*) from image where isMarkedDone = :isMarkDone and skuId = :skuId and isHidden = :isHidden")
    fun getUploadedImageCount(
        skuId: String,
        isMarkDone: Boolean = true,
        isHidden: Boolean = false
    ): Int?

    @Query("select * from image where skuId = :skuId and isHidden = :isHidden")
    fun getUploadingImages(skuId: String, isHidden: Boolean = false): LiveData<List<Image>>


    @Query("select Count(*) from image where (isUploaded = :isUploaded or isMarkedDone = :isMarkedDone) and createdAt <(select createdAt from image where skuId=:skuId limit 1)")
    fun totalRemainingUploadAbove(
        skuId: String,
        isUploaded: Boolean = false,
        isMarkedDone: Boolean = false
    ): Int

    @Query("select Count(*) from image where (isUploaded = :isUploaded or isMarkedDone = :isMarkedDone) and skuId = :skuId")
    fun getSkuUploadedOrQueued(
        skuId: String,
        isUploaded: Boolean = true,
        isMarkedDone: Boolean = true
    ): Int

    @Query("select * from image where skuId = :skuId and isHidden = :isHidden")
    fun getSkuImageList(skuId: String, isHidden: Boolean = false): List<Image>

    @Query("select COUNT(*) from image where isUploaded = :isUploaded and skuUuid = :skuUuid ")
    fun getUploadedCount(skuUuid: String, isUploaded: Boolean = true): Int

    @Query("select COUNT(*) from image where skuUuid = :skuUuid ")
    fun getTotalImageCount(skuUuid: String): Int

    @Query("select * from image where skuId = :skuId and overlayId = :overlayId")
    fun getImageForComment(skuId: String, overlayId: String): Image

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertImages(imageList: List<Image>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReuploadImage(image: Image): Long

    @Query("select Count(*) from image where skuId = :skuId and imageState = :imageState")
    fun totalvalidationCompletedCount(
        skuId: String,
        imageState: ImageState = ImageState.QUEUED
    ): Int

    @Query("select Count(*) from image where skuId = :skuId")
    fun skuImagesCount(skuId: String): Int

    @Query("select * from image where skuId = :skuId and image_category = :imageCategory")
    fun getClassifiedImages(skuId: String, imageCategory : String): List<Image>

    @Query("select * from image where skuId = :skuId and overlayId = '' ")
    fun getOthersImageList(skuId: String): List<Image>

    @Query("select classificationResult from image where uuid = :imageUuid")
    fun getClassificationResult(imageUuid: String): String

    @Query("update image set imageState = :imageState, classificationStatus = :classificationStatus  where uuid = :uuid")
    fun updateFailedClassificationResponse(
        uuid: String,
        classificationStatus: String,
        imageState: ImageState = ImageState.QUEUED
    ): Int

    @Query("update image set overlayId = :overlayId, image_category = :imageCategory  where uuid = :uuid")
    fun updateOverlayId(uuid: String, overlayId: String, imageCategory : String): Int

    @Query("select Count(*) from image where skuId = :skuId and imageState = :imageState")
    fun totalvalidationRemaningCount(
        skuId: String,
        imageState: ImageState = ImageState.CLASSIFICATION_PENDING
    ): Int

    @Update
    fun updateImages(exteriorImageList: List<Image>)


    @Query("SELECT * FROM Image WHERE imageId IN (:imageIds)")
    fun getListOfImages(imageIds: List<String>): List<Image>

    @Query("SELECT * FROM Image WHERE skuId = :skuId and image_category = :imageCategory")
    fun getInteriorImage(skuId: String, imageCategory: String = "360int"): Image?


    @Query("select * from image where skuUuid = :skuUuid and isUploaded = :isUploaded and isMarkedDone = :isMarkedDone and (output_image_lres_url IS NULL or output_image_lres_url = :outputUrl) limit 1")
    fun getUploadedImage(skuUuid: String, isUploaded: Boolean = true, isMarkedDone: Boolean = true,outputUrl: String = ""): Image?

    @Query("select * from image where skuId = :skuId")
    fun getSkuImageList(skuId: String): List<Image>

    @Query("DELETE FROM image WHERE uuid = :imageUuid")
    fun deleteImage(imageUuid: String): Int

    @Query("select * from image where skuUuid = :skuUuid limit 1")
    fun getImageSingleImageByUuid(skuUuid: String) : Image?

}