package com.spyneai.orders.data.paging


import com.google.gson.annotations.SerializedName
import com.spyneai.shootapp.repository.model.project.Project


class ProjectPagedRes(
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ArrayList<Project>
)
