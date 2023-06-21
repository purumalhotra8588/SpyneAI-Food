package com.spyneai.dashboard.data.model

import com.google.gson.annotations.SerializedName

data class UserDetailsResponse (
    @SerializedName("status") val status : Int,
    @SerializedName("message") val message : String,
    @SerializedName("Error") val Error: String,
    @SerializedName("data") val data : Data
        ){
    data class Data (

        @SerializedName("total_completed_projects") val total_completed_projects : Int,
        @SerializedName("total_ongoing_projects") val total_ongoing_projects : Int,
        @SerializedName("total_draft_projects") val total_draft_projects : Int,
        @SerializedName("user_name") val user_name : String,
        @SerializedName("user_email") val user_email : String,
        @SerializedName("total_credits") val total_credits : Int,
        @SerializedName("reshoot_count") val reshoot_count : Int,
        @SerializedName("qcdone_count") val qcdone_count : Int,
        @SerializedName("yet_to_qc") val yet_to_qc : Int

    )
}