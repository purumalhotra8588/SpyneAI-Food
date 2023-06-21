package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CatAgnosticResV2
import java.lang.reflect.Type

class InrteriorConverterV2 {
    @TypeConverter
    fun fromCameraSettingList(interior: List<CatAgnosticResV2.CategoryAgnos.Interior?>?): String? {
        if (interior == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CatAgnosticResV2.CategoryAgnos.Interior?>?>() {}.type
        return gson.toJson(interior, type)
    }

    @TypeConverter
    fun toCameraSettingList(interiorString: String?): List<CatAgnosticResV2.CategoryAgnos.Interior>? {
        if (interiorString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CatAgnosticResV2.CategoryAgnos.Interior?>?>() {}.type
        return gson.fromJson<List<CatAgnosticResV2.CategoryAgnos.Interior>>(interiorString, type)
    }
}