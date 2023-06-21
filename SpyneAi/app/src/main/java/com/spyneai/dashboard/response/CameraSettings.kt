package com.spyneai.dashboard.response

import com.google.gson.annotations.SerializedName

data class CameraSettings(
    @SerializedName("pitch_var")
    val pitchVar: Int,
    @SerializedName("parent_id")
    val parentId: String,
    @SerializedName("pitch")
    val pitch: List<Int>,
    @SerializedName("roll_var")
    val rollVar: Int,
    @SerializedName("roll")
    val roll: List<Int>,
    @SerializedName("show_grid")
    val showGrid: Boolean,
    @SerializedName("show_gyro")
    val showGyro: Boolean,
    @SerializedName("show_overlays")
    val showOverlays: Boolean
)