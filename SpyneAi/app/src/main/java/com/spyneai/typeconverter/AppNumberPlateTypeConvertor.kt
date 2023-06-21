package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CatAgnosticResV2
import java.lang.reflect.Type

class AppNumberPlateTypeConvertor {
    @TypeConverter
    fun fromCameraSettingList(interior: List<CatAgnosticResV2.CategoryAgnos.NoPlate?>?): String? {
        if (interior == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CatAgnosticResV2.CategoryAgnos.NoPlate?>?>() {}.type
        return gson.toJson(interior, type)
    }

    @TypeConverter
    fun toCameraSettingList(interiorString: String?): List<CatAgnosticResV2.CategoryAgnos.NoPlate>? {
        if (interiorString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CatAgnosticResV2.CategoryAgnos.NoPlate?>?>() {}.type
        return gson.fromJson<List<CatAgnosticResV2.CategoryAgnos.NoPlate>>(interiorString, type)
    }
}