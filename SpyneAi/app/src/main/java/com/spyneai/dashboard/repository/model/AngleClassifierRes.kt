package com.spyneai.dashboard.repository.model

import com.google.gson.annotations.SerializedName

data class AngleClassifierRes(
    val `data`: Data,
    val message: String
) {
    data class Data(

        @SerializedName("crop_array") val crop_array : CropArray,
        @SerializedName("car_angle") val car_angle : Boolean,
        @SerializedName("valid_angle") val valid_angle : Boolean,
        @SerializedName("exposure") val exposure : String?,
        @SerializedName("valid_category") val validCategory : Boolean,
        @SerializedName("distance") val distance : String?,
        @SerializedName("reflection") val reflection : String?
    ) {
        data class CropArray(
            val bottom: Boolean,
            val left: Boolean,
            val right: Boolean,
            val top: Boolean
        )
    }
}