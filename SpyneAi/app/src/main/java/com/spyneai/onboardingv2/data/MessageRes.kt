package com.spyneai.onboardingv2.data


import com.google.gson.annotations.SerializedName
import com.spyneai.dashboard.repository.model.AngleClassifierRes

data class MessageRes(
    @SerializedName("message") val message : String,
    @SerializedName("data") val data : Data

){
    data class Data (

        @SerializedName("sso_token") val sso_token : String,
        @SerializedName("price_per_credit") val price_per_credit : Int,
        @SerializedName("discount") val discount : Int
    )
}