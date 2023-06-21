package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CategoryAgnosticResponse
import java.lang.reflect.Type

class MiscellaneousListDataConverter {
    @TypeConverter
    fun fromCameraSettingList(miscellaneousApps: List<CategoryAgnosticResponse.MiscellaneousApp?>?): String? {
        if (miscellaneousApps == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CategoryAgnosticResponse.MiscellaneousApp?>?>() {}.type
        return gson.toJson(miscellaneousApps, type)
    }

    @TypeConverter
    fun toCameraSettingList(miscellaneousString: String?): List<CategoryAgnosticResponse.MiscellaneousApp>? {
        if (miscellaneousString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CategoryAgnosticResponse.MiscellaneousApp?>?>() {}.type
        return gson.fromJson<List<CategoryAgnosticResponse.MiscellaneousApp>>(miscellaneousString, type)
    }
}