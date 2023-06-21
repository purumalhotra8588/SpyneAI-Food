package com.spyneai.model.login

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("auth_token")
    val authToken : String,
    @SerializedName("discount") val discount: Int,
    @SerializedName("check_qc")
    val checkQc : Boolean,
    @SerializedName("email_id")
    val emailId : String,
    @SerializedName("enterprise_id") val enterprise_id: String,
    @SerializedName("message")
    val message : String,
    @SerializedName("price_per_credit") val price_per_credit: Int,
    @SerializedName("status")
    val status : Int,
    @SerializedName("user_id")
    val userId : String,
    @SerializedName("user_name")
    val userName : String,
    @SerializedName("is_new_user") val newUser: Int,
    @SerializedName("display_message") val displayMessage : String
)