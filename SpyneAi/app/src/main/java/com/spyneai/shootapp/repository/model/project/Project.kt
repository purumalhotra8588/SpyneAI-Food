package com.spyneai.shootapp.repository.model.project

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.spyneai.shootapp.data.model.Credits
import com.spyneai.shootapp.data.model.ProjectClickType

@Keep
@Entity
data class Project(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("local_id")
    var uuid: String,
    var userId: String,
    @SerializedName("project_name") var projectName: String? = null,
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("entity_id") var entityId: String? = null,
    @SerializedName("category") val categoryName: String? = null,
    @SerializedName("project_id") var projectId: String? = null,
    @SerializedName("dynamic_layout") val dynamicLayout: String? = null,
    @SerializedName("location_data") val locationData: String? = null,
    @SerializedName("status") var status: String = "draft",
    @SerializedName("rating") var rating: String? = null,
    @SerializedName("total_sku") var skuCount: Int = 0,
    @SerializedName("total_images") var imagesCount: Int = 0,
    @SerializedName("processed_images") var processedCount: Int = 0,
    @SerializedName("thumbnail") var thumbnail: String? = null,
    @SerializedName("qc_status") var qcStatus : String = "in_progress",
    var isCreated: Boolean = false,
    val toProcessAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 1,
    @SerializedName("created_on") var createdOn: String = System.currentTimeMillis().toString(),
    @SerializedName("created_at") var createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis(),
    val environment: String? = null,
    @SerializedName("image_upload_type")
    val shootType: String? = null,
    var isSelectable: Boolean = false,
    var isSubmitted: Boolean = true,
    @SerializedName("clickType") var clickType: ProjectClickType? = null,
    var credits: Credits? = null,
    var orderId: String? = null,
    var shoot_type: String? = null,
    @SerializedName("team_id")
    var teamId: String? = null
)