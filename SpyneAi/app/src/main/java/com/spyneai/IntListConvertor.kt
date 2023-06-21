package com.spyneai

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class IntListConvertor {

//        @TypeConverter
//        fun fromCountryLangList(countryLang: List<Int?>?): Int? {
//            if (countryLang == null) {
//                return null
//            }
//            val gson = Gson()
//            val type: Type = object : TypeToken<List<Int?>?>() {}.type
//            return gson.toJson(countryLang, type)
//        }
//
//        @TypeConverter
//        fun toCountryLangList(countryLangInt: Int?): List<Int>? {
//            if (countryLangInt == null) {
//                return null
//            }
//            val gson = Gson()
//            val type: Type = object : TypeToken<List<Int?>?>() {}.type
//            return gson.fromJson<List<String>>(countryLangInt, type)
//        }

}