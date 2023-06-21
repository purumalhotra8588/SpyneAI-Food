package com.spyneai.singleimageprocessing.data


import com.google.gson.annotations.SerializedName

data class SingleImageProcessRes(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("output_image")
        val outputImage: String,
        @SerializedName("output_watermark")
        val outputWatermarkImage: String
    )
}