package com.spyneai.model.credit

data class AvailableCreditResponse(
    val code: String,
    val `data`: CreditData,
    val message: String
)
data class CreditData(
    val available_credits: Int,
    val total_credits_spent: Int
)