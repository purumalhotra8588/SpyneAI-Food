package com.spyneai.dashboard.response


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class  CategoryAgnosticResponse(
    @SerializedName("status") val status : Int,
    @SerializedName("message") val message : String,
    @SerializedName("data") val data : List<CategoryData>?
) {


    //CATEGORY

    data class CategoryData(
        @SerializedName("categoryId") val categoryId : String,
        @SerializedName("name") val name : String,
        @SerializedName("enterprise_id") val enterprise_id : String,
        @SerializedName("display_thumbnail") val display_thumbnail : String,
        @SerializedName("color_code") val color_code : String,
        @SerializedName("description") val description : String,
        @SerializedName("multiSku") val multiSku : Boolean,
        @SerializedName("threeSixty") val threeSixty : Boolean,
        @SerializedName("orintation") val orintation : String,
        @SerializedName("shootInstructions") val shootInstructions : Boolean,
        @SerializedName("angles") val angles : List<Int>,
        @SerializedName("CameraSetting") val cameraSetting : List<CameraSetting>,
        @SerializedName("SubCategoryData") val subCategoryData : List<SubCategoryData>
    )

    @Entity
    data class CategoryListData(
        @PrimaryKey
        var categoryId: String = "",
        var name: String? = "",
        var enterprise_id: String? = "",
        var display_thumbnail: String? = "",
        var color_code: String? = "",
        var description: String? = "",
        var multiSku: Boolean? = true,
        var threeSixty: Boolean? = true,
        var orintation: String? = "",
        var shootInstructions: Boolean? = true,
    )

    //CAMERA SETTING

    data class CameraSetting(

        @SerializedName("categoryId") val categoryId : String,
        @SerializedName("focus") val focus : Boolean,
        @SerializedName("exposer") val exposer : Boolean,
        @SerializedName("brightness") val brightness : Int,
        @SerializedName("shootWithGyro") val shootWithGyro : Boolean,
        @SerializedName("pitch") val pitch : Int,
        @SerializedName("roll") val roll : Int
    )

    @Entity
    data class CameraSettings(
        @PrimaryKey
        var categoryId : String,
        var cameraSettings:List<CameraSetting> = arrayListOf()
    )

    //OVERLAYS

    data class Overlay(

        @SerializedName("overlayId") val overlayId : Int,
        @SerializedName("categoryId") val categoryId : String,
        @SerializedName("subCategoryId") val subCategoryId : String,
        @SerializedName("displayName") val displayName : String,
        @SerializedName("angleName") val angleName : String,
        @SerializedName("frameAngle") val frameAngle : Int,
        @SerializedName("type") val type : String,
        @SerializedName("angles") val angles : Int,
        @SerializedName("display_thumbnail") val display_thumbnail : String
    )

    @Entity
    data class OverlaysListData(
        @PrimaryKey
        var subCategoryId: String = "",
        var overlays:List<Overlay> = arrayListOf()
    )

    //SUBCATEGORIES

    data class SubCategoryData(

        @SerializedName("subCategoryId") val subCategoryId : String,
        @SerializedName("categoryId") val categoryId : String,
        @SerializedName("name") val name : String,
        @SerializedName("display_thumbnail") val display_thumbnail : String,
        @SerializedName("Overlay") val overlay : List<Overlay>,
        @SerializedName("interior") val interior : List<Interior>,
        @SerializedName("miscellaneous") val miscellaneousApps : List<MiscellaneousApp>
        )

    @Entity
    data class SubCategories(
        @PrimaryKey
        var categoryId: String = "",
        var subCategories:List<SubCategoryData> = arrayListOf()
    )

    // INTERIOR
    data class Interior (

        @SerializedName("id") val id : Int,
        @SerializedName("display_thumbnail") val display_thumbnail : String,
        @SerializedName("display_name") val display_name : String,
        @SerializedName("categoryId") val categoryId : String,
        @SerializedName("subCategoryId") val subCategoryId : String
    )

    @Entity
    data class InteriorListData(
        @PrimaryKey
        var subCategoryId: String = "",
        var interiorDataList:List<Interior> = arrayListOf()
    )

    //MISCELLANEOUS
    data class MiscellaneousApp (
        @SerializedName("id") val id : Int,
        @SerializedName("display_thumbnail") val display_thumbnail : String,
        @SerializedName("display_name") val display_name : String,
        @SerializedName("categoryId") val categoryId : String,
        @SerializedName("subCategoryId") val subCategoryId : String
    )

    @Entity
    data class MiscellaneousListData(
        @PrimaryKey
        var subCategoryId: String = "",
        var miscellaneousAppListData:List<MiscellaneousApp> = arrayListOf()
    )
}