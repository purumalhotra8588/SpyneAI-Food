package com.spyneai.shootapp.repository.model.image

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.spyneai.needs.AppConstants
import org.json.JSONObject
import java.io.Serializable

@Keep
@Entity
data class Image(
    @SerializedName("local_id")
    @PrimaryKey(autoGenerate = false)
    var uuid: String = "",
    val enterprise_id: String? = null,
    var input_image_hres_url: String = "",
    @SerializedName("input_image_lres_url")
    var input_image_lres_url: String = "",
    var output_image_hres_url: String = "",
    var output_image_lres_url: String = "",
    var output_image_lres_wm_url: String = "",
    var isSelected: Boolean = false,
    var imageClicked: Boolean = false,
    var isExtraImage: Boolean = false,
    var isHidden: Boolean = false,
    var imagePath: String? = null,
    var skuMeta: JSONObject? = null,
    @SerializedName("project_uuid") var projectUuid: String? = null,
    @SerializedName("project_id") var projectId: String? = null,
    @SerializedName("sku_name") var skuName: String?,
    @SerializedName("sku_uuid") var skuUuid: String?,
    @SerializedName("sku_id") var skuId: String? = null,
    @SerializedName("image_name") var name: String,
    @SerializedName("image_category") var image_category: String,
    @SerializedName("frame_seq_no") val sequence: Int,
    @SerializedName("angle") val angle: Int,
    @SerializedName("ai_angle") var aiAngle: Int = 0,
    @SerializedName("overlay_id") var overlayId: String,
    @SerializedName("is_reclick") var isReclick: Boolean,
    @SerializedName("is_reshoot") var isReshoot: Boolean,
    @SerializedName("path") var path: String,
    @SerializedName("pre_signed_url") var preSignedUrl: String = AppConstants.DEFAULT_PRESIGNED_URL,
    @SerializedName("image_id") var imageId: String? = null,
    @SerializedName("tags") var tags: String? = null,
    var debugData: String? = null,
    var imageState: ImageState = ImageState.QUEUED,
    @SerializedName("to_process_at") var toProcessAT: Long = System.currentTimeMillis(),
    @SerializedName("retry_count") val retryCount: Int = 1,
    @SerializedName("is_uploaded") var isUploaded: Boolean = false,
    @SerializedName("is_marked_done") var isMarkedDone: Boolean = false,
    @SerializedName("status") val status: String = "draft",
    @SerializedName("qc_status") var qcStatus: String = "in_progress",
    @SerializedName("reshoot_comment") var reshootComment: String? = null,
    @SerializedName("created_at") var createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updated_at") var updatedAt: Long = System.currentTimeMillis(),
    @SerializedName("classification_status") var classificationStatus: String? = null,
    @SerializedName("classification_data") var classificationResult: String? = null,
    var classificationPath: String? = null,
    var priority: Int = 1
) : Serializable