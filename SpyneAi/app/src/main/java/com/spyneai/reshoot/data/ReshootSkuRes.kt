package com.spyneai.reshoot.data

import com.google.gson.annotations.SerializedName

data class ReshootSkuRes(
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("images")
        val images: List<Image>,
        @SerializedName("project_id")
        val projectId: String,
        @SerializedName("sku_id")
        val skuId: String,
        @SerializedName("sku_name")
        val skuName: String
    ) {
        data class Image(
            @SerializedName("output_image_hres_url")
            val outputImageHresUrl: String,
            @SerializedName("reshoot_comment")
            val reshootComment: String
        )
    }
}