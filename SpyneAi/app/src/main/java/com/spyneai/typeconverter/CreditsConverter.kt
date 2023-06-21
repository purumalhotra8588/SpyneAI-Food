package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.shootapp.data.model.Credits
import java.lang.reflect.Type

class CreditsConverter {
    @TypeConverter
    fun fromCameraSettingList(processParams: Credits?): String? {
        if (processParams == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<Credits?>() {}.type
        return gson.toJson(processParams, type)
    }

    @TypeConverter
    fun toCameraSettingList(processParams: String?): Credits? {
        if (processParams == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<Credits?>() {}.type
        return gson.fromJson<Credits?>(processParams, type)
    }
}