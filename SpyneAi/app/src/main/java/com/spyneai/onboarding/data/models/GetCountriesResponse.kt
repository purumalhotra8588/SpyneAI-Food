package com.spyneai.onboarding.data.models

import com.google.gson.annotations.SerializedName

data class GetCountriesResponse (
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("status")
    val status: Int
        )
{
    data class Data (
        @SerializedName("id")
        val id: Int,
        @SerializedName("isd_code")
        val isd_code: Any,
        @SerializedName("name")
        val name: String,
        @SerializedName("num_length")
        val num_length: Any
    )
}