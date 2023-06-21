package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.dashboard.response.CategoryAgnosticResponse
import java.lang.reflect.Type

class SubCategoriesConverter {
    @TypeConverter
    fun fromCameraSettingList(CameraSetting: List<CategoryAgnosticResponse.SubCategoryData?>?): String? {
        if (CameraSetting == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CategoryAgnosticResponse.SubCategoryData?>?>() {}.type
        return gson.toJson(CameraSetting, type)
    }

    @TypeConverter
    fun toCameraSettingList(CameraSettingString: String?): List<CategoryAgnosticResponse.SubCategoryData>? {
        if (CameraSettingString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CategoryAgnosticResponse.SubCategoryData?>?>() {}.type
        return gson.fromJson<List<CategoryAgnosticResponse.SubCategoryData>>(CameraSettingString, type)
    }
}