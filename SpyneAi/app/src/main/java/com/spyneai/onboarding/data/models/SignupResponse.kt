package com.spyneai.onboarding.data.models

import com.google.gson.annotations.SerializedName

data class SignupResponse(

    @SerializedName("auth_token") val auth_token: String,
    @SerializedName("discount") val discount: Int,
    @SerializedName("display_message") val displayMessage: String,
    @SerializedName("email_id") val emailId: String,
    @SerializedName("enterprise_id") val enterpriseId: String,
    @SerializedName("message") val message: String,
    @SerializedName("price_per_credit") val price_per_credit: Int,
    @SerializedName("restaurant_location") val restaurant_location: String,
    @SerializedName("restaurant_name") val restaurant_name: String,
    @SerializedName("secret_key") val secret_secret_key: String,
    @SerializedName("status") val status: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("new_user") val newUser: Int,
    @SerializedName("contact_no") val contact_no: Int,

    )