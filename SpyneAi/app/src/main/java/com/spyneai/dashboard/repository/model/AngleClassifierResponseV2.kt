package com.spyneai.dashboard.repository.model

import com.google.gson.annotations.SerializedName

data class AngleClassifierResponseV2(
    val `data`: Data,
    val message: String
)

data class Data(
    @SerializedName("classification_result")
    val classificationResult: ClassificationResult,

    val result: List<Result>,
    @SerializedName("classification_status")
    val classificationStatus: String

    //val response: Response,


)

data class ClassificationResult(
    val category: String,

    @SerializedName("overlay_id")
    val overlayID: Long
)

data class Response(
    @SerializedName("Exposure")
    val exposure: String,

    @SerializedName("Blur")
    val blur: String,

    @SerializedName("Crop_detection")
    val cropDetection: CropDetection

)

data class CropDetection(
    val left: Boolean,
    val top: Boolean,
    val right: Boolean,
    val bottom: Boolean
)

data class Result(
    val title: String,
    val description: String,
    val status: String
)

