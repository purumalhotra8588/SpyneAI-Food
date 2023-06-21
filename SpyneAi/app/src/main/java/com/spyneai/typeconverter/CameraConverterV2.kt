package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CameraSettings
import java.lang.reflect.Type

class CameraConverterV2 {
    @TypeConverter
    fun fromCameraSettingList(cameraSettingV2: CameraSettings?): String? {
        if (cameraSettingV2 == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<CameraSettings?>() {}.type
        return gson.toJson(cameraSettingV2, type)
    }

    @TypeConverter
    fun toCameraSettingList(CameraSettingString: String?): CameraSettings? {
        if (CameraSettingString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<CameraSettings?>() {}.type
        return gson.fromJson<CameraSettings?>(CameraSettingString, type)
    }
}