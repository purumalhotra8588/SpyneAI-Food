package com.spyneai.reshoot.data

import com.google.gson.annotations.SerializedName
import com.spyneai.shootapp.repository.model.project.Project
import com.spyneai.shootapp.repository.model.sku.Sku


data class ReshootProjectRes(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("projectData")
        val projectAppData: Project,
        @SerializedName("skuData")
        val skuAppData: List<Sku>
    )
}