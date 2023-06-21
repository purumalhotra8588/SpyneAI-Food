package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.shootapp.repository.model.image.Image
import java.lang.reflect.Type

class ImageListConverter {
    @TypeConverter
    fun fromCountryLangList(countryLang: List<Image>?): String? {
        if (countryLang == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Image>?>() {}.type
        return gson.toJson(countryLang, type)
    }

    @TypeConverter
    fun toCountryLangList(countryLangString: String?): List<Image>? {
        if (countryLangString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Image>?>() {}.type
        return gson.fromJson<List<Image>>(countryLangString, type)
    }
}