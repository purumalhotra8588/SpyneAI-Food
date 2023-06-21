package com.spyneai.shootapp.repository.model.payment


import com.google.gson.annotations.SerializedName

data class GenerateOrderRes(
    @SerializedName("message")
    val message: String,
    @SerializedName("order_id")
    val orderId: String
)