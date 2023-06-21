package com.spyneai.orders.data.response

import com.google.gson.annotations.SerializedName
import com.spyneai.shootapp.repository.model.image.Image


data class ImagesOfSkuRes(
    val data: List<Image>,
    val message: String,
    val paid: String,
    val sku_status: String,
    val status: Int,
    var fromLocal: Boolean = true,
    @SerializedName("360_frames")
    val threeSixtyFrame: ThreeSixtyFrameData? = null
)
class ThreeSixtyFrameData(
    val nerf_360: String? = null,
    val non_nerf_360: String? = null,
    val original_video_url: String? = null,
    val input_360: String? = null,
    val interior_360: String? = null
)
