package com.spyneai.dashboard.response

import com.google.gson.annotations.SerializedName

data class VideoShoot(
    @SerializedName("video_frame_selection")
    val videoFrameSelection: Boolean,
    @SerializedName("minimum_duration")
    val minimumDuration: Int,
    @SerializedName("video_frames")
    val videoFrames: List<Int>,
    @SerializedName("default_video_frames")
    val defaultVideoFrame: Int? = null
)