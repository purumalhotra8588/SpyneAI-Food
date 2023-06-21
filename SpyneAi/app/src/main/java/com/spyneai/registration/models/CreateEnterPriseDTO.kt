package com.spyneai.registration.models


import com.google.gson.annotations.SerializedName

data class CreateEnterPriseDTO(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Int?
) {
    data class Data(
        @SerializedName("api_key")
        val apiKey: String?,
        @SerializedName("category")
        val category: String?,
        @SerializedName("color")
        val color: Any?,
        @SerializedName("enterprise_id")
        val enterpriseId: String?,
        @SerializedName("logo_url")
        val logoUrl: Any?,
        @SerializedName("name")
        val name: String?
    )
}