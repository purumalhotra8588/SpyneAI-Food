package com.spyneai.onboardingv2.data


import com.google.gson.annotations.SerializedName

data class CategoryModalClass(
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("category_id")
        val categoryId: String,
        @SerializedName("category_name")
        val categoryName: String,
        @SerializedName("category_logo")
        val categoryLogo: String,
        @SerializedName("border_color")
        val borderColor: String,
        @SerializedName("unselected_fill_color")
        val unselectedFillColor: String,
        @SerializedName("thumbnail")
        val thumbnail: String,
        @SerializedName("selected_fill_color")
        val selectedFillColor: String,
        @SerializedName("priority")
        val priority: Int,


    )
}