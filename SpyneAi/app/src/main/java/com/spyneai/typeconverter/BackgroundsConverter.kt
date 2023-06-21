package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.shootapp.data.model.CarsBackgroundRes
import java.lang.reflect.Type

class BackgroundsConverter {
    @TypeConverter
    fun fromCameraSettingList(interior: List<CarsBackgroundRes.BackgroundApp?>?): String? {
        if (interior == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CarsBackgroundRes.BackgroundApp?>?>() {}.type
        return gson.toJson(interior, type)
    }

    @TypeConverter
    fun toCameraSettingList(interiorString: String?): List<CarsBackgroundRes.BackgroundApp>? {
        if (interiorString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CarsBackgroundRes.BackgroundApp?>?>() {}.type
        return gson.fromJson<List<CarsBackgroundRes.BackgroundApp>>(interiorString, type)
    }
}