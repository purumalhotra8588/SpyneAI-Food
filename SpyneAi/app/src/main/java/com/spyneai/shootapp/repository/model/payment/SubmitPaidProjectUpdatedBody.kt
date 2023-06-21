package com.spyneai.shootapp.repository.model.payment


import com.google.gson.annotations.SerializedName

data class SubmitPaidProjectUpdatedBody(
    @SerializedName("auth_key")
    val authKey: String,
    @SerializedName("credit_details")
    val creditDetails: List<CreditDetail>,
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("projectIdList")
    val projectIdList: List<String?>
) {
    data class CreditDetail(
        @SerializedName("exterior")
        val exterior: Exterior?,
        @SerializedName("interior")
        val interior: Interior?,
        @SerializedName("miscellanous")
        val miscellanous: Miscellanous?,
        @SerializedName("project_id")
        val projectId: String,
        @SerializedName("sku_list")
        val skuList: List<Sku>,
        @SerializedName("total")
        val total: Total
    ) {
        data class Exterior(
            @SerializedName("credit_count")
            val creditCount: Int?,
            @SerializedName("image_count")
            val imageCount: Int?
        )

        data class Interior(
            @SerializedName("credit_count")
            val creditCount: Int?,
            @SerializedName("image_count")
            val imageCount: Int?
        )

        data class Miscellanous(
            @SerializedName("credit_count")
            val creditCount: Int?,
            @SerializedName("image_count")
            val imageCount: Int?
        )

        data class Sku(
            @SerializedName("exterior")
            val exterior: Exterior?,
            @SerializedName("interior")
            val interior: Interior?,
            @SerializedName("miscellanous")
            val miscellanous: Miscellanous?,
            @SerializedName("sku_id")
            val skuId: String,
            @SerializedName("total")
            val total: Total
        ) {
            data class Exterior(
                @SerializedName("credit_count")
                val creditCount: Int?,
                @SerializedName("image_count")
                val imageCount: Int?
            )

            data class Interior(
                @SerializedName("credit_count")
                val creditCount: Int?,
                @SerializedName("image_count")
                val imageCount: Int?
            )

            data class Miscellanous(
                @SerializedName("credit_count")
                val creditCount: Int?,
                @SerializedName("image_count")
                val imageCount: Int?
            )

            data class Total(
                @SerializedName("credit_count")
                val creditCount: Int,
                @SerializedName("image_count")
                val imageCount: Int
            )
        }

        data class Total(
            @SerializedName("credit_count")
            val creditCount: Int,
            @SerializedName("image_count")
            val imageCount: Int
        )
    }
}