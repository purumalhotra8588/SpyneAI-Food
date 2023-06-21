package com.spyneai.shootapp.data.model

import com.google.gson.annotations.SerializedName

data class CreditItem(
    @SerializedName("credit_count")
    val credit: Int = 0,
    @SerializedName("image_count")
    val images: Int = 0
)