package com.spyneai.onboardingv2.data

data class UpdateUserDetailBody(
    val auth_key: String ="",
    val countryName: String="",
    val name: String="",
    val referral_code: String=""
)