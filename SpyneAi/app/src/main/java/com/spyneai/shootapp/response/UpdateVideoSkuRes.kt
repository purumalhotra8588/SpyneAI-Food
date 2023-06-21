package com.spyneai.shootapp.response


import com.google.gson.annotations.SerializedName

data class UpdateVideoSkuRes(
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
)