package com.spyneai.dashboard.data.model

import com.google.gson.annotations.SerializedName

data class RidResponse(

    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("Error") val Error: String,
    @SerializedName("data") val data: List<Data>
) {
    data class Data(

        @SerializedName("entity_id") val entity_id: String,
        @SerializedName("entity_name") val entity_name: String,
        var isSelected: Boolean = false
    )
}