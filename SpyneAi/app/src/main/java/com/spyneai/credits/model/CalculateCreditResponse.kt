package com.spyneai.credits.model

data class CalculateCreditResponse(
    val code: String,
    val `data`: CalculateCreditData,
    val message: String
)
data class CalculateCreditData(
    val paid_credits: Int,
    val total_credits: Int,
    val user_available_credits: Int
)