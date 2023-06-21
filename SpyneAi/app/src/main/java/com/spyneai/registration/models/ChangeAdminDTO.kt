package com.spyneai.registration.models


import com.google.gson.annotations.SerializedName

data class ChangeAdminDTO(
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Int?
)