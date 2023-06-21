package com.spyneai.shootapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class MarketplaceRes(
    val `data`: List<Marketplace>)
{
    @Entity
    data class Marketplace(
        @PrimaryKey
        val id:Int,
        val active: Int,
        val bottom_margin: String,
        val country: String? = null,
        val created_at: String,
        val enterprise_id: String,
        val height: String,
        val left_margin: String,
        val marketPlace_id: String,
        val marketPlace_name: String,
        val market_place_img: String,
        val per_credit: Int,
        var prod_cat_id: String,
        var prod_sub_cat_id: String,
        val right_margin: String,
        val top_margin: String,
        val updated_at: String,
        val width: String,
        var isSelected: Boolean = false
    )

}


