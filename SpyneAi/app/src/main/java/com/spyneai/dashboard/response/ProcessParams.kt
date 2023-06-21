package com.spyneai.dashboard.response


import com.google.gson.annotations.SerializedName

data class ProcessParams(
    @SerializedName("id")
    val id: Int,
    @SerializedName("default_value")
    val defaultValue: Any,
    @SerializedName("field_name")
    val fieldName: String,
    @SerializedName("field_type")
    val fieldType: String,
    @SerializedName("field_id")
    val fieldId: String,
    @SerializedName("is_required")
    val isRequired: Boolean
)
