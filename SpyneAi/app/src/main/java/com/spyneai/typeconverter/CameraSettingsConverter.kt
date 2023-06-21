package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CategoryAgnosticResponse
import java.lang.reflect.Type

class CameraSettingsConverter {
    @TypeConverter
    fun fromCameraSettingList(CameraSetting: List<CategoryAgnosticResponse.CameraSetting?>?): String? {
        if (CameraSetting == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CategoryAgnosticResponse.CameraSetting?>?>() {}.type
        return gson.toJson(CameraSetting, type)
    }

    @TypeConverter
    fun toCameraSettingList(CameraSettingString: String?): List<CategoryAgnosticResponse.CameraSetting>? {
        if (CameraSettingString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CategoryAgnosticResponse.CameraSetting?>?>() {}.type
        return gson.fromJson<List<CategoryAgnosticResponse.CameraSetting>>(CameraSettingString, type)
    }
}