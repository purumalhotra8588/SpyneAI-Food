package com.spyneai.shootapp.repository.model.payment


import com.google.gson.annotations.SerializedName

data class PaymentIdBody(
    @SerializedName("auth_key")
    val authKey: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("paid_amount")
    val paidAmount: Int,
    @SerializedName("payment_id")
    val paymentId: String,
    @SerializedName("purchased_credits")
    val purchasedCredits: Int,
    @SerializedName("signature")
    val signature: String
)