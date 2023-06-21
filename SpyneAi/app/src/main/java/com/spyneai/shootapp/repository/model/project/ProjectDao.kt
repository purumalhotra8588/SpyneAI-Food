package com.spyneai.shootapp.repository.model.project

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.spyneai.app.BaseApplication
import com.spyneai.getTimeStamp
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.captureEvent
import com.spyneai.shootapp.data.model.Credits
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.shootapp.utils.objectToString

@Dao
interface ProjectDao {

    @Insert
    fun insertProject(obj: Project): Long

    @Update
    fun updateProject(project: Project): Int

    @Query("update project set projectId = :projectId, isCreated = :isCreated where uuid = :uuid")
    fun updateProjectServerId(uuid: String, projectId: String, isCreated: Boolean = true): Int

    @Query("UPDATE project SET imagesCount = (imagesCount - :deletedImageListSize) WHERE projectId =:projectId")
    fun updateProjectTotalFrameAfterDelete(projectId: String, deletedImageListSize: Int): Int

    @Query("select COUNT(*) from project where isCreated = :isCreated ")
    fun getPendingProjects(isCreated: Boolean = false): Int

    @Query("select * from project where isCreated = :isCreated LIMIT :limit")
    fun getOldestProject(isCreated: Boolean = false, limit: Int = 1): Project

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(doggoModel: List<Project>): List<Long>

    @Transaction
    suspend fun insertWithCheck(response: List<Project>) {
        val list = ArrayList<Project>()

        response.forEach {
            if (it.uuid.isNullOrEmpty())
                it.uuid = getUuid()

            if (it.status == "Done" || it.status == "completed")
                it.status = "completed"

            if (it.status == "In Progress" || it.status == "Yet to Start" || it.status == "ongoing")
                it.status = "ongoing"

            if (it.qcStatus == null)
                it.qcStatus = "in_progress"

            it.userId = Utilities.getPreference(BaseApplication.getContext(), AppConstants.USER_ID)
                .toString()

            it.status = it.status.lowercase()
            it.isCreated = true

            val dbItem = getProject(it.uuid)

            if (dbItem == null) {
                val project = getProjectByProjectId(it.projectId)
                if (project == null) {
                    it.createdAt = getTimeStamp(it.createdOn)
                    list.add(it)
                }

            } else {
                if (it.skuCount > dbItem.skuCount)
                    dbItem.skuCount = it.skuCount

                if (it.processedCount > dbItem.processedCount)
                    dbItem.processedCount = it.processedCount

                if (it.imagesCount > dbItem.imagesCount)
                    dbItem.imagesCount = it.imagesCount

                if (dbItem.projectId == null)
                    dbItem.projectId = it.projectId

                if (dbItem.thumbnail == null && it.thumbnail != null)
                    dbItem.thumbnail = it.thumbnail

                dbItem.qcStatus =
                    if (it.qcStatus != dbItem.qcStatus) it.qcStatus else dbItem.qcStatus

                if (it.status != dbItem.status) {
                    when (it.status) {
                        "Done", "completed" -> dbItem.status = "completed"
                        "In Progress", "Yet to Start", "ongoing" -> dbItem.status = "ongoing"
                    }
                }

                BaseApplication.getContext()
                    .captureEvent("Update Project In Sync", HashMap<String, Any?>().apply {
                        put("enterpriseSkuId", dbItem.projectName)
                        put("enterprise_sku_id", dbItem.projectName)
                        put("project", dbItem.objectToString())
                    })

                updateProject(project = dbItem)
            }
        }

        insertAll(list)
    }


    @Query("Select * from project where projectId = :projectId")
    fun getProjectByProjectId(projectId: String?): Project


    @Query("SELECT * FROM project where uuid = :uuid")
    fun getProject(uuid: String): Project

//    @Query("SELECT * FROM project where status = :status order by createdAt DESC LIMIT :limit OFFSET :offset")
//    suspend fun getProjectsWithLimitAndSkip(offset: Int,status: String = "Draft",limit: Int = 10) : List<Project>

    @Query("select * from project")
    fun getAllProjects(): List<Project>

    @Query("Select * from project where isCreated = :isCreated  and skuCount > 0 and toProcessAt <= :currentTime LIMIT :limit")
    fun getProjectWithSkus(
        isCreated: Boolean = false,
        currentTime: Long = System.currentTimeMillis(),
        limit: Int = 1
    ): ProjectWithSku

    @Query("update project set toProcessAt = :toProcessAt, retryCount = retryCount + 1 where uuid = :uuid ")
    fun skipProject(uuid: String, toProcessAt: Long): Int


    @Query("select * from project where projectName = :projectName ")
    suspend fun getProjectByName(projectName: String): Project

    @Query("SELECT * FROM project where status = 'draft'")
    fun getDraftProjects(): List<Project>

    @Query("select COUNT(*) from project where isSelectable = :isSelectable and isSubmitted = :isSubmitted")
    fun getSubmissionPendingProjects(
        isSelectable: Boolean = true,
        isSubmitted: Boolean = false
    ): Int

    @Query("select * from project where userId =:userId and  uuid IN (:projectUuid)")
    fun getCartProjectsData(
        projectUuid: List<String>, userId: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.USER_ID
        ).toString()
    ): LiveData<List<Project>>

    @Query("SELECT * FROM project where shoot_type = :shootType and userId = :userId  and teamId = :teamId order by createdAt DESC LIMIT :limit OFFSET :offset ")
    suspend fun getInspectionProjectWithOutFilter(
        offset: Int, limit: Int = 10,
        shootType: String = "Inspection",
        userId: String = Utilities.getPreference(BaseApplication.getContext(), AppConstants.USER_ID)
            .toString(),
        teamId: String? = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.TEAM_ID
        )
    ): List<Project>

    @Query("SELECT * FROM project where userId = :userId and teamId = :teamId order by createdAt DESC LIMIT :limit OFFSET :offset ")
    suspend fun getProjectWithOutFilter(
        offset: Int,
        limit: Int = 10,
        userId: String = Utilities.getPreference(BaseApplication.getContext(), AppConstants.USER_ID)
            .toString(),
        teamId: String? = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.TEAM_ID
        )
    ): List<Project>

    @Query("SELECT * FROM project where userId = :userId and (teamId is null or teamId = '') order by createdAt DESC LIMIT :limit OFFSET :offset ")
    suspend fun getProjectWithOutFilterTeamIdNull(
        offset: Int,
        limit: Int = 10,
        userId: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.USER_ID
        ).toString()
    ): List<Project>

    @Query("SELECT * FROM project where status = :status and userId = :userId and teamId = :teamId order by createdAt DESC LIMIT :limit OFFSET :offset ")
    suspend fun getProjectsWithLimitAndSkip(
        offset: Int,
        status: String = "Draft",
        limit: Int = 10,
        userId: String = Utilities.getPreference(BaseApplication.getContext(), AppConstants.USER_ID)
            .toString(),
        teamId: String? = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.TEAM_ID
        )
    ): List<Project>

    @Query("SELECT * FROM project where status = :status and userId = :userId and (teamId is null or teamId = '') order by createdAt DESC LIMIT :limit OFFSET :offset ")
    suspend fun getProjectsWithLimitAndSkipTeamIdNull(
        offset: Int,
        status: String = "Draft",
        limit: Int = 10,
        userId: String = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.USER_ID
        ).toString()
    ): List<Project>

    @Query("SELECT * FROM project where status = :status and shoot_type = :shootType and teamId = :teamId order by createdAt DESC LIMIT :limit OFFSET :offset ")
    suspend fun getInspectionProjectsWithLimitAndSkip(
        offset: Int, status: String = "Draft", limit: Int = 10, shootType: String = "Inspection",
        teamId: String? = Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.TEAM_ID
        )
    ): List<Project>

    @Query("SELECT * FROM project where status = :status order by createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getProjectsWithFilter(
        offset: Int,
        status: String = "Draft",
        limit: Int = 10
    ): List<Project>

    @Query("select * from project where projectId NOT NULL and orderId NOT NULL and credits NOT NULL and isSelectable = :isSelectable and isSubmitted = :isSubmitted and toProcessAt <= :currentTime limit 1")
    fun getPaidProjectIds(
        isSelectable: Boolean = true,
        isSubmitted: Boolean = false,
        currentTime: Long = System.currentTimeMillis()
    ): List<Project>

    @Query("select skuCount from project where uuid = :uuid")
    fun getSkusCountByProjectUuid(uuid: String): Int

    @Query("update project set credits = :finalCredits where uuid = :uuid")
    fun updateProjectCredits(uuid: String, finalCredits: Credits): Int

    @Query("update sku set skuName= :skuName where uuid = :uuid")
    fun updateSkuName(uuid: String, skuName: String): Int

    @Query("update project set projectName= :projectName where uuid = :uuid")
    fun updateProjectName(uuid: String, projectName: String): Int

    @Query("update project set orderId = :orderId  where uuid IN (:projectUuids)")
    fun updateProjectOrderId(orderId: String, projectUuids: List<String>): Int

    @Query("SELECT * FROM sku where projectUuid = :projectUuid")
    fun getSkusByProjectId(projectUuid: String): List<Sku>

    @Query("select * from project where orderId = :orderId")
    fun getProjectsByOrderId(orderId: String): List<Project>

    @Transaction
    fun updateProject(uuid: String) {
        val p = updateProjectStatus(uuid)
        Log.d(AppConstants.SHOOT_DAO_TAG, "updateProject: $p")

        val update = markProjectSelectAble(uuid)
        Log.d(AppConstants.SHOOT_DAO_TAG, "updateProject: $update")
    }

    @Query("UPDATE project SET status = 'ongoing' WHERE uuid =:uuid ")
    fun updateProjectStatus(uuid: String): Int

    @Query("update project set toProcessAt = :toProcessAt, isSelectable = :isSelectable where uuid = :uuid")
    fun markProjectSelectAble(
        uuid: String,
        toProcessAt: Long = System.currentTimeMillis(),
        isSelectable: Boolean = true
    ): Int
}