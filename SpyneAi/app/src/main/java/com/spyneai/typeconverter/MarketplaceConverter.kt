package com.spyneai.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.shootapp.data.model.MarketplaceRes
import java.lang.reflect.Type

class MarketplaceConverter{
@TypeConverter
fun fromCameraSettingList(interior: List<MarketplaceRes.Marketplace?>?): String? {
    if (interior == null) {
        return null
    }
    val gson = Gson()
    val type: Type = object : TypeToken<List<MarketplaceRes.Marketplace?>?>() {}.type
    return gson.toJson(interior, type)
}

@TypeConverter
fun toCameraSettingList(interiorString: String?): List<MarketplaceRes.Marketplace>? {
    if (interiorString == null) {
        return null
    }
    val gson = Gson()
    val type: Type = object : TypeToken<List<MarketplaceRes.Marketplace?>?>() {}.type
    return gson.fromJson<List<MarketplaceRes.Marketplace>>(interiorString, type)
}
}