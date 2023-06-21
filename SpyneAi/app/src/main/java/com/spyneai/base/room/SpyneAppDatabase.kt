package com.spyneai.base.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.repository.DashboardDao
import com.spyneai.dashboard.repository.model.CategoryDataAppDao
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.dashboard.response.CategoryAgnosticResponse
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.loginsignup.models.Categories
import com.spyneai.loginsignup.models.CategoriesDao
import com.spyneai.shootapp.data.model.CarsBackgroundRes
import com.spyneai.shootapp.data.model.MarketplaceRes
import com.spyneai.shootapp.repository.db.RecentBackgroundDao
import com.spyneai.shootapp.repository.db.ShootDaoApp
import com.spyneai.shootapp.repository.model.RecentBackground
import com.spyneai.shootapp.repository.model.image.Image
import com.spyneai.shootapp.repository.model.image.ImageDao
import com.spyneai.shootapp.repository.model.project.Project
import com.spyneai.shootapp.repository.model.project.ProjectDao
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.shootapp.repository.model.sku.SkuDao
import com.spyneai.typeconverter.*

@Database(
    entities = [NewCategoriesResponse.Category::class,
        NewSubCatResponse.Subcategory::class,
        NewSubCatResponse.Interior::class,
        NewSubCatResponse.Miscellaneous::class,
        NewSubCatResponse.Tags.ExteriorTags::class,
        NewSubCatResponse.Tags.InteriorTags::class,
        NewSubCatResponse.Tags.FocusShoot::class,
        OverlaysResponse.Overlays::class,
        CarsBackgroundRes.BackgroundApp::class,
        MarketplaceRes.Marketplace::class,
        RecentBackground::class,
        CategoryAgnosticResponse.CategoryListData::class,
        CategoryAgnosticResponse.CameraSettings::class,
        CategoryAgnosticResponse.OverlaysListData::class,
        CategoryAgnosticResponse.SubCategories::class,
        CategoryAgnosticResponse.InteriorListData::class,
        CategoryAgnosticResponse.MiscellaneousListData::class,
        CatAgnosticResV2.CategoryAgnos::class,
        CatAgnosticResV2.CategoryAgnos.SubCategoryV2::class,
        Project::class,
        Sku::class,
        Image::class,
        Categories::class],
    version = 42
)
@TypeConverters(
    StringListConvertor::class,
    CameraConverterV2::class,
    InrteriorConverterV2::class,
    VideoShootConvertor::class,
    MiscConverterV2::class,
    SubcatConvertor::class,
    OverlaysConvertor::class,
    ShootExperinceConvertor::class,
    CameraSettingsConverter::class,
    SubCategoriesConverter::class,
    OverlaysListDataConverter::class,
    InteriorListDataConverter::class,
    MiscellaneousListDataConverter::class,
    ProcessParamsConverter::class,
    VideoShootConvertor::class,
    BackgroundsConverter::class,
    MarketplaceConverter::class,
    CrouselConverter::class,
    CaseStudiesConverter::class,
    TutorialsConverter::class,
    AppNumberPlateTypeConvertor::class,
    WindowCorrectionConvertor::class,
    HashMapConverter::class,
    CreditsConverter::class,
    ImageListConverter::class,
    JsonConvertor::class
)

abstract class SpyneAppDatabase : RoomDatabase() {
    abstract fun categoryDataDao(): CategoryDataAppDao

    abstract fun dashboardDao(): DashboardDao

    abstract fun shootDao(): ShootDaoApp

    abstract fun projectDao(): ProjectDao

    abstract fun skuDao(): SkuDao

    abstract fun imageDao(): ImageDao


    abstract fun recentBackgroundDao(): RecentBackgroundDao
    abstract fun categoriesDao(): CategoriesDao

    companion object {

        @Volatile
        private var INSTANCE: SpyneAppDatabase? = null
        fun getInstance(context: Context): SpyneAppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SpyneAppDatabase::class.java,
                        "spyne-db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

val MIGRATION_35_41 = object : Migration(35, 41) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add the new columns to the CategoryAgnos table
        database.execSQL("ALTER TABLE CategoryAgnos RENAME COLUMN backgrounds TO backgroundApps")
        database.execSQL("ALTER TABLE CategoryAgnos ADD COLUMN windowCorrection TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE CategoryAgnos ADD COLUMN numberPlateList TEXT DEFAULT NULL")

        database.execSQL("ALTER TABLE SubCategoryV2 RENAME COLUMN overlays TO overlayApps")
        database.execSQL("ALTER TABLE SubCategoryV2 ADD COLUMN isSelected INTEGER NOT NULL DEFAULT 0")

        // Delete the Project, Sku, Image, and VideoDetails tables
//        database.execSQL("DROP TABLE IF EXISTS Project")
//        database.execSQL("DROP TABLE IF EXISTS Sku")
//        database.execSQL("DROP TABLE IF EXISTS Image")
//        database.execSQL("DROP TABLE IF EXISTS VideoDetails")
    }
}

