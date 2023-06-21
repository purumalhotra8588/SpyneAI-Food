package com.spyneai.onboarding.data.models

import com.google.gson.annotations.SerializedName

data class ValidateOtpResponse (
        @SerializedName("auth_token")
        val authToken : String,
        @SerializedName("email_id")
        val emailId : String,
        @SerializedName("message")
        val message : String,
        @SerializedName("status")
        val status : Int,
        @SerializedName("user_id")
        val userId : String,
        @SerializedName("user_name")
        val userName : String,
        @SerializedName("display_message") val displayMessage : String
)