package com.spyneai.dashboard.repository.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.dashboard.response.CategoryAgnosticResponse
import com.spyneai.needs.AppConstants

@Dao
interface CategoryDataAppDao {


    @Transaction
    fun saveCatAgnosData(
        list: List<CatAgnosticResV2.CategoryAgnos>,
        subCatList: List<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>
    ){
       val catInsert = insertCategories(list)
        Log.d(AppConstants.CATAGNOSTAG, "saveCatAgnosData: $catInsert")

        val subCatInsert = insertSubCategories(subCatList)
        Log.d(AppConstants.CATAGNOSTAG, "saveCatAgnosData: $subCatInsert")
    }

    @Transaction
    suspend fun insertCatAgnosticData(
        list: List<CatAgnosticResV2.CategoryAgnos>,
        subCatList: List<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>
    ){
        val catInsert = insertCategories(list)
        Log.d(AppConstants.CATAGNOSTAG, "saveCatAgnosData: $catInsert")

        val subCatInsert = insertSubCategories(subCatList)
        Log.d(AppConstants.CATAGNOSTAG, "saveCatAgnosData: $subCatInsert")
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategories(list: List<CatAgnosticResV2.CategoryAgnos>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubCategories(list: List<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>): List<Long>

    @Transaction
    fun saveCategoryData(
        categoryDataList: List<CategoryAgnosticResponse.CategoryListData>,
        subCategories: List<CategoryAgnosticResponse.SubCategories>,
        overlays: List<CategoryAgnosticResponse.OverlaysListData>,
        cameraSettings: List<CategoryAgnosticResponse.CameraSettings>,
        interiorDataList: List<CategoryAgnosticResponse.InteriorListData>,
        miscellaneousListData: List<CategoryAgnosticResponse.MiscellaneousListData>
    ) {
        insertCategoryData(categoryDataList)
        insertCameraSetting(cameraSettings)
        insertOverlays(overlays)
        insertSubcategory(subCategories)
        insertInterior(interiorDataList)
        insertMiscellaneous(miscellaneousListData)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategoryData(list: List<CategoryAgnosticResponse.CategoryListData>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCameraSetting(list: List<CategoryAgnosticResponse.CameraSettings>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOverlays(list: List<CategoryAgnosticResponse.OverlaysListData>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubcategory(list: List<CategoryAgnosticResponse.SubCategories>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInterior(list: List<CategoryAgnosticResponse.InteriorListData>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMiscellaneous(list: List<CategoryAgnosticResponse.MiscellaneousListData>)

    @Transaction
    fun deleteCatData() {
        deleteCategoryData()
        deleteCameraSetting()
        deleteOverlays()
        deleteSubcategory()
        deleteInterior()
        deleteSubCategoryV2()
        deleteMiscellaneous()
        deleteCategoryAgnosData()
    }

    @Query("DELETE FROM categorylistdata")
    abstract fun deleteCategoryData()

    @Query("DELETE FROM categoryagnos")
    abstract fun deleteCategoryAgnosData()

    @Query("DELETE FROM SubCategoryV2")
    abstract fun deleteSubCategoryV2()

    @Query("DELETE FROM camerasettings")
    abstract fun deleteCameraSetting()

    @Query("DELETE FROM overlayslistdata")
    abstract fun deleteOverlays()

    @Query("DELETE FROM subcategories")
    abstract fun deleteSubcategory()

    @Query("DELETE FROM interiorlistdata")
    abstract fun deleteInterior()

    @Query("DELETE FROM miscellaneouslistdata")
    abstract fun deleteMiscellaneous()


//    @Query("SELECT * FROM SubCategories where subCategoryId = :subCategoryId")
//    fun getSubCategories(subCategoryId: String) : List<CategoryAgnosticResponse.SubCategories>

//        @Query("SELECT * FROM Overlays where prod_sub_cat_id = :subCategoryId")
//    fun getOverlays(subCategoryId: String) : List<CatAgnosticResV2.CategoryAgnos.SubCategoryV2.Overlay>
//
//    @Query("SELECT * FROM CameraSettings")
//    fun getCameraSetting() : List<CategoryAgnosticResponse.CameraSettings>
//
    @Query("SELECT * FROM CategoryListData")
    fun getCategory(): LiveData<List<CategoryAgnosticResponse.CategoryListData>>

    @Query("SELECT * FROM CameraSettings")
    fun getCameraSettings(): List<CategoryAgnosticResponse.CameraSettings>

    @Query("SELECT * FROM InteriorListData")
    fun getInterior(): LiveData<List<CategoryAgnosticResponse.InteriorListData>>

    @Query("select * from CategoryAgnos ")
    fun getCatAgnosData(): LiveData<List<CatAgnosticResV2.CategoryAgnos>>

     @Query("select * from CategoryAgnos where categoryId = :categoryId ")
    fun getLiveDataCategoryById(categoryId: String) : LiveData<CatAgnosticResV2.CategoryAgnos>

    @Query("select * from CategoryAgnos where categoryId = :categoryId ")
    fun getCategoryById(categoryId: String) : CatAgnosticResV2.CategoryAgnos

    @Query("select * from CategoryAgnos where categoryId = :categoryId ")
    suspend fun getCategoryAgnosticById(categoryId: String) : CatAgnosticResV2.CategoryAgnos

    @Query("select * from SubCategoryV2 where prodCatId = :categoryId ")
    fun getSubcategories(categoryId: String): LiveData<List<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>>

    @Query("select shootExperience from categoryagnos where categoryId = :categoryId ")
    fun getShootExperience(categoryId: String): LiveData<CatAgnosticResV2.CategoryAgnos.ShootExperience>

    @Query("select * from SubCategoryV2 where prodSubCatId = :prodSubCatId ")
    fun getSubcategory(prodSubCatId: String): CatAgnosticResV2.CategoryAgnos.SubCategoryV2

    @Query("select orientation from categoryagnos where categoryId = :categoryId")
    fun getCategoryOrientation(categoryId: String): LiveData<String>

}