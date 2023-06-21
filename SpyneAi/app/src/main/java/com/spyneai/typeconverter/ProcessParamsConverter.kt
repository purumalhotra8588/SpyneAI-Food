package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CameraSettings
import com.spyneai.dashboard.response.ProcessParams
import java.lang.reflect.Type

class ProcessParamsConverter {
    @TypeConverter
    fun fromCameraSettingList(processParams: List<ProcessParams>?): String? {
        if (processParams == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<ProcessParams>?>() {}.type
        return gson.toJson(processParams, type)
    }

    @TypeConverter
    fun toCameraSettingList(processParams: String?): List<ProcessParams>? {
        if (processParams == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<ProcessParams>?>() {}.type
        return gson.fromJson<List<ProcessParams>?>(processParams, type)
    }
}