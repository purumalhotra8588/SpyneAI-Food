package com.spyneai.onboarding.data.models

data class LoginWithPasswordRes(
    val auth_token: String,
    val check_qc: Boolean,
    val contact_no: Any,
    val crm_role: Any,
    val discount: Int,
    val email_id: String,
    val enterprise_id: String,
    val isd_code: String,
    val message: String,
    val new_user: Int,
    val price_per_credit: Int,
    val restaurant_location: Any,
    val restaurant_name: Any,
    val role: String,
    val secret_key: String,
    val status: Int,
    val user_id: String,
    val user_name: String
)