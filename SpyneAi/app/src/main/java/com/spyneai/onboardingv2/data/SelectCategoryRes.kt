package com.spyneai.onboardingv2.data


import com.google.gson.annotations.SerializedName

data class SelectCategoryRes(
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("angles")
        val angles: Int,
        @SerializedName("border_color")
        val borderColor: String,
        @SerializedName("category_id")
        val categoryId: String,
        @SerializedName("category_name")
        val categoryName: String,
        @SerializedName("selected_fill_color")
        val selectedFillColor: String,
        @SerializedName("priority")
        val priority: Int,
        @SerializedName("thumbnail")
        val thumbnail: String,
        @SerializedName("unselected_fill_color")
        val unselectedFillColor: String,
        var isSelected: Boolean = false
    )
}