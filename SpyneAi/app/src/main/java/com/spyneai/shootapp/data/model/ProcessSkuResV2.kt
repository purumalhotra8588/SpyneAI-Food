package com.spyneai.shootapp.data.model


import com.google.gson.annotations.SerializedName

data class ProcessSkuResV2(
    @SerializedName("description")
    val data: Data,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("sku_id")
        val skuId: String,
    )
}