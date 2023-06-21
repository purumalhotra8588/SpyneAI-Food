package com.spyneai.onboarding.data.models

import com.google.gson.annotations.SerializedName

data class LoginWithPasswordResponse(

    @SerializedName("auth_token") val auth_token: String,
    @SerializedName("crm_role") val crm_role: String,
    @SerializedName("discount") val discount: Int,
    @SerializedName("email_id") val email_id: String,
    @SerializedName("enterprise_id") val enterpriseId: String,
    @SerializedName("message") val message: String,
    @SerializedName("price_per_credit") val price_per_credit: Int,
    @SerializedName("restaurant_location") val restaurant_location: String,
    @SerializedName("restaurant_name") val restaurant_name: String,
    @SerializedName("status") val status: Int,
    @SerializedName("user_id") val user_id: String,
    @SerializedName("user_name") val user_name: String,
    @SerializedName("new_user") val newUser: Int,
    @SerializedName("contact_no") val contact_no: Int,
    )