package com.spyneai.onboardingv2.data

data class UpdateUserDetailRes(
    val `data`: Data,
    val message: String,
    val status: Int
)
data class Data(
    val discount: Int,
    val price_per_credit: Int,
    val sso_token: String
)