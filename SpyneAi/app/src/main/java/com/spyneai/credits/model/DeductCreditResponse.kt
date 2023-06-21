package com.spyneai.credits.model

data class DeductCreditResponse(
    val code: String,
    val `data`: DeductCreditData,
    val message: String
)
data class DeductCreditData(
    val available_credits: Int,
    val credits_spent: Int
)