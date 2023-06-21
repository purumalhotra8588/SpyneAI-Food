package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CategoryAgnosticResponse
import java.lang.reflect.Type

class InteriorListDataConverter {
    @TypeConverter
    fun fromCameraSettingList(interior: List<CategoryAgnosticResponse.Interior?>?): String? {
        if (interior == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CategoryAgnosticResponse.Interior?>?>() {}.type
        return gson.toJson(interior, type)
    }

    @TypeConverter
    fun toCameraSettingList(interiorString: String?): List<CategoryAgnosticResponse.Interior>? {
        if (interiorString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CategoryAgnosticResponse.Interior?>?>() {}.type
        return gson.fromJson<List<CategoryAgnosticResponse.Interior>>(interiorString, type)
    }
}