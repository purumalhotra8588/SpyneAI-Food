package com.spyneai.dashboard.response

import com.google.gson.annotations.SerializedName

data class RequestCreditResponse(
    @SerializedName("status") val status : Int,
    @SerializedName("Error") val Error: String,
    @SerializedName("message") val message : String
)