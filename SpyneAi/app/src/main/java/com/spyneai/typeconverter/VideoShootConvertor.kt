package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CameraSettings
import com.spyneai.dashboard.response.VideoShoot
import java.lang.reflect.Type

class VideoShootConvertor {
    @TypeConverter
    fun fromCameraSettingList(cameraSettingV2: VideoShoot?): String? {
        if (cameraSettingV2 == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<VideoShoot?>() {}.type
        return gson.toJson(cameraSettingV2, type)
    }

    @TypeConverter
    fun toCameraSettingList(videoShoot: String?): VideoShoot? {
        if (videoShoot == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<VideoShoot?>() {}.type
        return gson.fromJson<VideoShoot?>(videoShoot, type)
    }
}