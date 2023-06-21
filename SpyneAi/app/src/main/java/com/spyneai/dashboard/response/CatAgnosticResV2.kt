package com.spyneai.dashboard.response
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.spyneai.shootapp.data.model.CarsBackgroundRes
import com.spyneai.shootapp.data.model.MarketplaceRes

public data class CatAgnosticResV2(
    @SerializedName("data")
    val `data`: List<CategoryAgnos>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
) {
    @Entity
    data class CategoryAgnos(
        @PrimaryKey
        @SerializedName("prod_cat_id")
        val categoryId: String,
        @SerializedName("crousel") val crousel : List<Crousel>,
        @SerializedName("tutorials") val tutorials : List<Tutorials>,
        @SerializedName("case_studies") val case_studies : List<Case_studies>,
        @SerializedName("is_active")
        val isActive: Boolean,
        @SerializedName("category_name")
        val name: String,
        @SerializedName("enterprise_id")
        val enterpriseId: String,
        @SerializedName("display_thumbnail")
        val displayThumbnail: String,
        @SerializedName("color_code")
        val colorCode: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("is_threeSixty")
        val isThreeSixty: Boolean,
        @SerializedName("video_shoot")
        val videoShoot: VideoShoot?,
        @SerializedName("orientation")
        val orientation: String,
        @SerializedName("image_categories")
        val imageCategories: List<String>,
        @SerializedName("shoot_experience")
        val shootExperience: ShootExperience?,
        @SerializedName("camera_settings")
        val cameraSettingsV2: CameraSettings?,
        @SerializedName("interior")
        val interior: List<Interior>?,
        @SerializedName("Misc")
        var miscellaneous: List<Miscellaneou>?,
        @SerializedName("subcat_label")
        val subcategoryLabel: String?,
        @SerializedName("sub_categories")
        val subCategoryV2s: List<SubCategoryV2>?,
        @SerializedName("backgrounds")
        val backgroundApps: List<CarsBackgroundRes.BackgroundApp>,
        @SerializedName("marketPlace")
        val marketPlace: List<MarketplaceRes.Marketplace>? = null,
        @SerializedName("process_params")
        val processParams: List<ProcessParams>?,
        @SerializedName("fetch_backgrounds_by")
        val fetchBackgroundsBy: String? = "prodCatId",
        @SerializedName("fetch_marketplace_by")
        val fetchMarketplaceBy: String? = "prodCatId",
        @SerializedName("fetch_marketplace")
        val fetchMarketplace: Boolean=false,
        @SerializedName("fetch_background")
        val fetchBackground: Boolean=false,
        @SerializedName("show_marketplaces")
        val show_marketplaces: Boolean = false,
        val windowCorrection: WindowCorrection? = null,
        val numberPlateList: List<NoPlate>? = null
    ) {
        data class Interior(
            @SerializedName("display_name")
            val displayName: String,
            @SerializedName("display_thumbnail")
            val displayThumbnail: String,
            @SerializedName("id")
            val overlayId: Int,
            @SerializedName("prod_cat_id")
            val prodCatId: String,
            @SerializedName("prod_sub_cat_id")
            val prodSubCatId: String,
            var isSelected: Boolean = false,
            var imageClicked: Boolean = false,
            var imagePath: String? = null,
            var sequenceNumber: Int = 0
        )

        data class NoPlate(
            val active: Int = 0,
            val created_on: String,
            val enterprise_id: String,
            val id: Int,
            val is_default: Int,
            var isSelected: Boolean = false,
            val number_plate_logo_id: String,
            val number_plate_logo_name: String,
            val number_plate_logo_url: String,
            val number_plate_value: Any,
            val updated_on: String
        )

        data class Miscellaneou(
            @SerializedName("display_name")
            val displayName: String,
            @SerializedName("display_thumbnail")
            val displayThumbnail: String,
            @SerializedName("id")
            val overlayId: Int,
            @SerializedName("prod_cat_id")
            val prodCatId: String,
            @SerializedName("prod_sub_cat_id")
            val prodSubCatId: String,
            var isSelected: Boolean = false,
            var imageClicked: Boolean = false,
            var imagePath: String? = null,
            var sequenceNumber: Int = 0
        )
        data class ShootExperience(
            @SerializedName("show_shoot_instructions")
            val showShootInstructions: Boolean,
            @SerializedName("shoot_instructions")
            val shootInstructions: ShootInstructions,
            @SerializedName("has_subcategories")
            val hasSubcategories: Boolean,
            @SerializedName("frame_selection")
            val frameSelection: Boolean,
            @SerializedName("frames")
            val frames: List<Int>,
            @SerializedName("perspective_cropping")
            val perspectiveCropping: Boolean,
            @SerializedName("add_more_angles")
            val addMoreAngles: Boolean = false
        ) {
            data class ShootInstructions(
                @SerializedName("cta")
                val cta: String,
                @SerializedName("title")
                val title: String,
                @SerializedName("url")
                val url: String
            )
        }
        @Entity
        data class SubCategoryV2(
            @SerializedName("camera_settings")
            val cameraSettings: CameraSettings?,
            @SerializedName("display_thumbnail")
            val displayThumbnail: String,
            @SerializedName("overlays")
            val overlayApps: List<OverlayApp>?,
            @SerializedName("prod_cat_id")
            val prodCatId: String,
            @PrimaryKey
            @SerializedName("prod_sub_cat_id")
            val prodSubCatId: String,
            @SerializedName("sub_cat_name")
            val subCatName: String,
            var isSelected: Boolean = false
        ) {
            data class OverlayApp(
                @SerializedName("active")
                val active: Int,
                @SerializedName("angle_name")
                val angleName: String,
                @SerializedName("angles")
                val angles: Int,
                @SerializedName("created_at")
                val createdAt: String,
                @SerializedName("display_name")
                val displayName: String,
                @SerializedName("display_thumbnail")
                val displayThumbnail: String,
                @SerializedName("enterprise_id")
                val enterpriseId: String,
                @SerializedName("frame_angle")
                val frameAngle: String,
                @SerializedName("id")
                val id: Int,
                @SerializedName("overlay_id")
                val overlayId: String,
                @SerializedName("priority")
                val priority: Int,
                @SerializedName("prod_cat_id")
                val prodCatId: String,
                @SerializedName("prod_sub_cat_id")
                val prodSubCatId: String,
                @SerializedName("type")
                val type: String,
                @SerializedName("updated_at")
                val updatedAt: String,
                var isSelected : Boolean = false,
                var imageClicked : Boolean = false,
                var imagePath : String? = null,
                var sequenceNumber : Int = 0
            )
        }

        data class Crousel (

            @SerializedName("before_image") val before_image : String,
            @SerializedName("after_image") val after_image : String
        )
        data class Tutorials (

            @SerializedName("link") val link : String,
            @SerializedName("display_thumbnail") val display_thumbnail : String,
            @SerializedName("title") val title : String,
            @SerializedName("action") val action : String
        )

        data class Case_studies (

            @SerializedName("link") val link : String,
            @SerializedName("display_thumbnail") val display_thumbnail : String,
            @SerializedName("title") val title : String,
            @SerializedName("action") val action : String
        )

        data class WindowCorrection(
            val default: Default,
            @SerializedName("is_user_configrable")
            val is_user_configurable: Boolean,
            val tintColor: List<TintColor>,
            val transparency: List<String>
        ) {
            data class Default(
                val tintColor: String,
                val transparency: String
            )

            data class TintColor(
                val image_url: String,
                @SerializedName("color")
                val color: String,
                val name: String,
                var isSelected: Boolean = false
            )
        }
    }
}