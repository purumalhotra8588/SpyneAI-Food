package com.spyneai.shootapp.repository.model.payment


import com.google.gson.annotations.SerializedName

data class GenerateOrderBody(
    @SerializedName("auth_key")
    val authKey: String,
    @SerializedName("discount")
    val discount: Int,
    @SerializedName("source")
    val source: String,
    @SerializedName("total_amount")
    val totalAmount: Double,
    @SerializedName("final_amount")
    val finalAmount: Double
)