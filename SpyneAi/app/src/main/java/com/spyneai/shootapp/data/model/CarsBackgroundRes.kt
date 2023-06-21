package com.spyneai.shootapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class CarsBackgroundRes(
    val `data`: List<BackgroundApp>,
    val `marketPlace`: List<MarketplaceRes.Marketplace>
) {
    @Entity
    data class BackgroundApp(
        @PrimaryKey(autoGenerate = true)
        val id : Int,
        var categoryId : String,
        @SerializedName("bgName")
        var bgName: String,
        var gifUrl: String?,
        @SerializedName("backgroundCredit")
        val imageCredit: Int,
        @SerializedName("backgroundId")
        val imageId: String,
        @SerializedName("backgroundImageUrl")
        val imageUrl: String?,
        @SerializedName("prodCatId")
        var prodCatId: String?,
        @SerializedName("prodSubCatId")
        var prodSubCatId: String?,
        var isSelected: Boolean = false,
        @SerializedName("backgroundType")
        val backgroundType : String?


    )
}