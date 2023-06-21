package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CatAgnosticResV2
import java.lang.reflect.Type

class WindowCorrectionConvertor{
    @TypeConverter
    fun fromHotspotCoordinatesData(hotspot: CatAgnosticResV2.CategoryAgnos.WindowCorrection?): String? {
        if (hotspot == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<CatAgnosticResV2.CategoryAgnos.WindowCorrection?>() {}.type
        return gson.toJson(hotspot, type)
    }

    @TypeConverter
    fun toHotspotCoordinatesData(hotspotString: String?): CatAgnosticResV2.CategoryAgnos.WindowCorrection? {
        if (hotspotString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<CatAgnosticResV2.CategoryAgnos.WindowCorrection?>() {}.type
        return gson.fromJson<CatAgnosticResV2.CategoryAgnos.WindowCorrection>(hotspotString, type)
    }
}