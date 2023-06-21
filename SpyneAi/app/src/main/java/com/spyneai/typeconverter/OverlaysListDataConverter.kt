package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CategoryAgnosticResponse
import java.lang.reflect.Type

class OverlaysListDataConverter {
    @TypeConverter
    fun fromCameraSettingList(CameraSetting: List<CategoryAgnosticResponse.Overlay?>?): String? {
        if (CameraSetting == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CategoryAgnosticResponse.Overlay?>?>() {}.type
        return gson.toJson(CameraSetting, type)
    }

    @TypeConverter
    fun toCameraSettingList(CameraSettingString: String?): List<CategoryAgnosticResponse.Overlay>? {
        if (CameraSettingString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CategoryAgnosticResponse.Overlay?>?>() {}.type
        return gson.fromJson<List<CategoryAgnosticResponse.Overlay>>(CameraSettingString, type)
    }
}