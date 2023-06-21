package com.spyneai.model

import com.google.gson.annotations.SerializedName

data class FetchCategoryRes(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String
)
data class Data(
    @SerializedName("category-data")
    val categoryData: List<CategoryData>
)

data class CategoryData(
    @SerializedName("border_color")
    var borderColor: String?,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("priority")
    val priority: Int?,
    @SerializedName("selected_fill_color")
    var selectedFillColor: String?,
    @SerializedName("thumbnail")
    val thumbnail: String,
    @SerializedName("unselected_fill_color")
    var unselectedFillColor: String?,
    var isSelected: Boolean = false
)