package com.spyneai.shootapp.data.model

import com.google.gson.annotations.SerializedName

data class SubmitPaidProjectBody(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("auth_key")
    val authKey: String,
    @SerializedName("projectIdList")
    val projectIdList: List<String?>
)
