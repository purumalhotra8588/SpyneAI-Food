package com.spyneai.onboarding.data.models

import com.google.gson.annotations.SerializedName

data class OtpResponse (
        @SerializedName("message")
        val message : String,
        @SerializedName("status")
        val status : Int,
        @SerializedName("user_id")
        val userId : String
)