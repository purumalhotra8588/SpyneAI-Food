

package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class HashMapConverter {
    @TypeConverter
    fun fromCountryLangList(countryLang: HashMap<String,Any>?): String? {
        if (countryLang == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<HashMap<String,Any>?>() {}.type
        return gson.toJson(countryLang, type)
    }

    @TypeConverter
    fun toCountryLangList(countryLangString: String?): HashMap<String,Any>? {
        if (countryLangString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<HashMap<String,Any>?>() {}.type
        return gson.fromJson<HashMap<String,Any>>(countryLangString, type)
    }

}