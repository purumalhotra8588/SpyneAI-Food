package com.spyneai.shootapp.repository.model.payment


import com.google.gson.annotations.SerializedName

data class PaymentIdRes(
    @SerializedName("message")
    val message: String,
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("payment_id")
    val paymentId: String
)