package com.spyneai.shootapp.data.model

import com.google.gson.annotations.SerializedName

data class ImproveShootResponse(

    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Data

) {
    data class Data(

        @SerializedName("guidelines") val guidelines: List<Guidelines>
    )

    data class Guidelines(

        @SerializedName("heading") val heading: String,
        @SerializedName("description") val description: String,
        @SerializedName("regulations") val regulations: List<Regulations>
    )

    data class Regulations(

        @SerializedName("image_url") val image_url: String,
        @SerializedName("method") val method: String,
        @SerializedName("description") val description: String
    )
}