package com.spyneai.food

import com.google.gson.annotations.SerializedName


data class StableDiffusionResponse(

    val `data`: StableDiffusionData,
    val message: String

) {

    data class StableDiffusionData(
        @SerializedName("output_image_url")
        var outputImageUrl: String,
        @SerializedName("image_id")
        var imageId: String,
    )
}
