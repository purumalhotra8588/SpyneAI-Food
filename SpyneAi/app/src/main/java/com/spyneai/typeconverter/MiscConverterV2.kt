package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CatAgnosticResV2
import java.lang.reflect.Type

class MiscConverterV2 {
    @TypeConverter
    fun fromCameraSettingList(miscellaneous: List<CatAgnosticResV2.CategoryAgnos.Miscellaneou?>?): String? {
        if (miscellaneous == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CatAgnosticResV2.CategoryAgnos.Miscellaneou?>?>() {}.type
        return gson.toJson(miscellaneous, type)
    }

    @TypeConverter
    fun toCameraSettingList(miscellaneousString: String?): List<CatAgnosticResV2.CategoryAgnos.Miscellaneou>? {
        if (miscellaneousString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CatAgnosticResV2.CategoryAgnos.Miscellaneou?>?>() {}.type
        return gson.fromJson<List<CatAgnosticResV2.CategoryAgnos.Miscellaneou>>(miscellaneousString, type)
    }
}