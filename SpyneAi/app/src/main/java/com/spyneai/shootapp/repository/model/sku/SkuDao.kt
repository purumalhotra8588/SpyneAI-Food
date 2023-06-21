package com.spyneai.shootapp.repository.model.sku

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.google.gson.Gson
import com.spyneai.app.BaseApplication
import com.spyneai.getTimeStamp
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants
import com.spyneai.posthog.captureEvent
import com.spyneai.shootapp.repository.model.project.Project
import com.spyneai.shootapp.utils.objectToString

@Dao
interface SkuDao {

    @Transaction
    fun saveSku(sku: Sku, project: Project) {
        val skuid = insertSku(sku)
        Log.d(AppConstants.SHOOT_DAO_TAG, "saveSku: $skuid")
        Log.d(AppConstants.SHOOT_DAO_TAG, "saveSku: " + sku.projectUuid)
        project.skuCount = project.skuCount.plus(1)
        val projectudpate = updateProject(project)

        BaseApplication.getContext().captureEvent(
            "Sku count updated",
            HashMap<String, Any?>().apply {
                put("sku_name", sku.skuName)
                put("sku_count", project.skuCount)
                put("project", Gson().toJson(project))
                put("sku", Gson().toJson(sku))
                put("updated", projectudpate)
            }
        )
    }

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateProject(project: Project): Int

    @Query("UPDATE project SET skuCount = skuCount + 1, isCreated = :isCreated WHERE uuid =:uuid ")
    fun updateProjectSkuCount(uuid: String, isCreated: Boolean = false): Int

    @Insert
    fun insertSku(obj: Sku): Long

    @Update
    fun updateSku(sku: Sku): Int

    @Query("update sku set totalFramesUpdated =:isTotalFramesUpdated, isProcessed = :isProcessed where uuid = :uuid ")
    fun updateSkuProcessed(
        uuid: String,
        isTotalFramesUpdated: Boolean = true,
        isProcessed: Boolean = true
    )

    @Query("select COUNT(*) from sku where isProcessed = :isProcessed and isCreated = :isCreated and backgroundId != 'DEFAULT_BG_ID'")
    fun getPendingSku(isProcessed: Boolean = false, isCreated: Boolean = true): Int

    @Query("select * from sku where isProcessed = :isProcessed and isCreated = :isCreated LIMIT :limit")
    fun getOldestSku(isProcessed: Boolean = false, isCreated: Boolean = true, limit: Int = 1): Sku

    @Query("select * from sku")
    fun getAllSKus(): List<Sku>



    @Query("SELECT * FROM sku where backgroundId = :backgroundId and projectUuid = :projectUuid")
    fun getDraftSkusByProjectId(
        projectUuid: String,
        backgroundId: String = AppConstants.DEFAULT_BG_ID
    ): List<Sku>


    @Query("select * from sku where projectUuid = :uuid")
    fun getSkuWithProjectUuid(uuid: String): Sku

    @Query("select * from sku where projectUuid = :uuid")
    fun getSkuListWithProjectUuid(uuid: String): List<Sku>

    @Query("SELECT * FROM sku where projectUuid = :projectUuid order by createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getSkusWithLimitAndSkip(
        offset: Int,
        projectUuid: String,
        limit: Int = 50
    ): List<Sku>

    @Transaction
    suspend fun insertSkuWithCheck(
        response: ArrayList<Sku>,
        projectUuid: String,
        projectId: String? = null
    ) {
        val list = ArrayList<Sku>()

        response.forEach {
            if (it.uuid.isNullOrEmpty())
                it.uuid = getUuid()

            it.projectUuid = projectUuid
            it.projectId = projectId

            if (it.status == "Done") {
                it.status = "completed"
                it.isProcessed = true
            }

            if (it.status == "In Progress") {
                it.status = "ongoing"
                it.isProcessed = true
            }

            if (it.qcStatus == null)
                it.qcStatus = "in_progress"

            it.status?.let { status ->
                it.status = status.lowercase()
            }

            val dbItem = getSku(it.uuid)

            if (dbItem == null) {
                val sku = getSkuBySkuId(it.skuId)

                if (sku == null) {
                    if (it.backgroundId == null)
                        it.backgroundId = AppConstants.DEFAULT_BG_ID

                    it.createdAt = getTimeStamp(it.createdOn)

                    list.add(it)
                }

            } else {

                if (dbItem.backgroundId == null) {
                    dbItem.backgroundId = it.backgroundId
                    dbItem.backgroundName = it.backgroundName
                }

                if (dbItem.projectId == null)
                    dbItem.projectId = it.projectId

                if (dbItem.skuId == null)
                    dbItem.skuId = it.skuId

                if (it.processedImages > dbItem.processedImages)
                    dbItem.processedImages = it.processedImages

                if (it.imagesCount > dbItem.imagesCount)
                    dbItem.imagesCount = it.imagesCount

                dbItem.isThreeSixty = it.isThreeSixty
                dbItem.isPaid=it.isPaid
                dbItem.toProcessAt = System.currentTimeMillis()
                dbItem.images = if (it.images != dbItem.images) it.images else dbItem.images
                try {
                    dbItem.createdAt = getTimeStamp(it.createdOn)
                }catch (e: java.lang.Exception){}

                dbItem.qcStatus = if (it.qcStatus != dbItem.qcStatus) it.qcStatus else dbItem.qcStatus

                if (dbItem.thumbnail == null && it.thumbnail != null)
                    dbItem.thumbnail = it.thumbnail

                if (it.status != dbItem.status){
                    when(it.status){
                        "Done","completed" -> {
                            dbItem.status = "completed"
                            dbItem.isProcessed = true
                        }
                        "In Progress","Yet to Start", "ongoing" -> {
                            dbItem.status = "ongoing"
                            dbItem.isProcessed = true
                        }
                    }
                }

                BaseApplication.getContext()
                    .captureEvent("Update Sku In Sync", HashMap<String, Any?>().apply {
                        put("enterpriseSkuId", dbItem.skuName)
                        put("enterprise_sku_id", dbItem.skuName)
                        put("sku", dbItem.objectToString())
                    })

                updateSku(sku = dbItem)
            }
        }

        insertAll(list)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(skuList: List<Sku>): List<Long>


    @Query("select * from sku where uuid = :uuid")
    fun getSku(uuid: String): Sku

    @Query("select * from sku where skuId = :skuId")
    fun getSkuBySkuId(skuId: String?): Sku

    @Query("Select * from sku where isProcessed = :isProcessed and isCreated = :isCreated and backgroundId != 'DEFAULT_BG_ID' and toProcessAt <= :currentTime LIMIT :limit")
    fun getProcessAbleSku(
        isProcessed: Boolean = false,
        isCreated: Boolean = true,
        currentTime: Long = System.currentTimeMillis(),
        limit: Int = 1
    ): Sku


    @Query("update sku set toProcessAt = :toProcessAt, retryCount = retryCount + 1 where uuid = :uuid ")
    fun skipSku(uuid: String, toProcessAt: Long): Int

    @Query("select * from sku where skuName = :skuName")
    fun getSkuIdByName(skuName: String): Sku?

    @Query("select * from sku where uuid = :skuUuid")
    fun getThreeSixtyList(skuUuid: String): Sku

    @Query("update sku set threeSixtyList = :threeSixtyList where uuid = :skuUuid")
    fun updateThreeSixtyList(skuUuid: String, threeSixtyList: List<String>)

    @Query("UPDATE sku SET totalFrames = (totalFrames - :deletedImageListSize), imagesCount = (imagesCount - :deletedImageListSize) WHERE skuId =:skuId ")
    fun updateSkuTotalFrameAfterDelete(skuId: String, deletedImageListSize: Int): Int

    @Query("select subcategoryId from sku where skuId = :skuId")
    fun getSubCatIdOfSku(skuId: String): String

    @Query("UPDATE sku SET processSku = :processSku WHERE uuid =:skuUuid ")
    fun makeSkuProcessAble(skuUuid: String, processSku: Boolean = true): Int

    @Query("UPDATE sku SET videoId = :videoId  WHERE skuId =:skuId and categoryName = :categoryName and subcategoryName = :subCatName")
    fun updateSkuVideoId(
        skuId: String,
        videoId: String,
        categoryName: String,
        subCatName: String
    ): Int

    @Query("SELECT * FROM sku where projectUuid = :projectUuid")
    fun getSkusByProjectId(projectUuid: String) : List<Sku>

    @Query("update sku set subcategoryId = :subcategoryId, subcategoryName = :subcategoryname where skuId = :skuId ")
    fun updateSubCategory(skuId: String, subcategoryname : String, subcategoryId : String) : Int

    @Query("select * from sku where uuid = :uuid")
    fun getSkuIdByUuid(uuid: String): Sku?

}