package com.spyneai.onboardingv2.data


import com.google.gson.annotations.SerializedName

data class SampleImagesRes(
    @SerializedName("data")
    val `data`: Data
) {
    data class Data(
        @SerializedName("sample_images")
        val sampleImages: List<SampleImage>
    ) {
        data class SampleImage(
            @SerializedName("input_image")
            val inputImage: String,
            @SerializedName("output_image")
            val outputImage: String,
            @SerializedName("prod_cat_id")
            val prodCatId: String,
            @SerializedName("prod_sub_cat_id")
            val prodSubCatId: String
        )
    }
}