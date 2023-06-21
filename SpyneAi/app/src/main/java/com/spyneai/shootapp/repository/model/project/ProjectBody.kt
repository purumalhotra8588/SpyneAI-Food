package com.spyneai.shootapp.repository.model.project


import com.google.gson.annotations.SerializedName
import com.spyneai.app.BaseApplication
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

data class ProjectBody(
    @SerializedName("auth_key")
    val authKey: String = Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY)!!,
    @SerializedName("project_data")
    val projectData: ProjectData,
    @SerializedName("sku_data")
    val skuData: List<SkuData>,
    @SerializedName("vehicle_data")
    val vehicleData: VehicleData?
) {
    data class ProjectData(
        @SerializedName("category_id")
        val categoryId: String,
        @SerializedName("dynamic_layout")
        var dynamicLayout: DynamicLayout? = null,
        @SerializedName("local_id")
        val localId: String,
        @SerializedName("project_id")
        val projectId: String?,
        @SerializedName("location_data")
        var locationData: LocationData? = null,
        @SerializedName("project_name")
        val projectName: String,
        @SerializedName("entity_id")
        val entityId: String? = null,
        @SerializedName("source")
        val source: String = "App_android",
        val is_old_app: Boolean = false,
        val shoot_type: String? = null,
    ) {
        data class DynamicLayout(
            @SerializedName("dynamic")
            val `dynamic`: String? = null
        )

        data class LocationData(
            @SerializedName("location")
            val location: String? = null
        )
    }

    data class SkuData(
        @SerializedName("sku_id")
        val skuId: String?,
        @SerializedName("image_present")
        val imagePresent: Int,
        @SerializedName("initial_no")
        val initialNo: Int,
        @SerializedName("local_id")
        val localId: String,
        @SerializedName("prod_cat_id")
        val prodCatId: String,
        @SerializedName("prod_sub_cat_id")
        val prodSubCatId: String? = null,
        @SerializedName("sku_name")
        val skuName: String,
        @SerializedName("source")
        val source: String = "App_android",
        @SerializedName("total_frames_no")
        val totalFramesNo: Int,
        @SerializedName("video_present")
        val videoPresent: Int
    )

    data class VehicleData(
        val color: String?,
        val rcImage: String,
        val registrationNumber: String,
        val vehicleId: String,
        val model: String,
        val vinScanner: String,
        val chassis_number: String,
        val engine_number: String,
        val odometer_reading: String,
        val name_of_owner: String,
    )
}