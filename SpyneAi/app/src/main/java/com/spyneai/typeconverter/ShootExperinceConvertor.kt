package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CatAgnosticResV2
import java.lang.reflect.Type

class ShootExperinceConvertor {
    @TypeConverter
    fun fromCameraSettingList(interior: CatAgnosticResV2.CategoryAgnos.ShootExperience?): String? {
        if (interior == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<CatAgnosticResV2.CategoryAgnos.ShootExperience?>() {}.type
        return gson.toJson(interior, type)
    }

    @TypeConverter
    fun toCameraSettingList(interiorString: String?): CatAgnosticResV2.CategoryAgnos.ShootExperience? {
        if (interiorString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<CatAgnosticResV2.CategoryAgnos.ShootExperience?>() {}.type
        return gson.fromJson<CatAgnosticResV2.CategoryAgnos.ShootExperience?>(interiorString, type)
    }
}