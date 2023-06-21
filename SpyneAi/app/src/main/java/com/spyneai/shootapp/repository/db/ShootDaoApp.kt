package com.spyneai.shootapp.repository.db

import android.util.Log
import androidx.room.*
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.needs.AppConstants
import com.spyneai.shootapp.data.model.CarsBackgroundRes
import com.spyneai.shootapp.data.model.MarketplaceRes
import com.spyneai.shootapp.repository.model.image.Image
import com.spyneai.shootapp.repository.model.sku.Sku

@Dao
interface ShootDaoApp {
    //subcategories  queries
    @Transaction
    fun saveSubcategoriesData(
        subCategories: List<NewSubCatResponse.Subcategory>,
        interior: List<NewSubCatResponse.Interior>,
        misc: List<NewSubCatResponse.Miscellaneous>,
        exteriorTagsTags: List<NewSubCatResponse.Tags.ExteriorTags>,
        interiorTags: List<NewSubCatResponse.Tags.InteriorTags>,
        focusTags: List<NewSubCatResponse.Tags.FocusShoot>){

        subcategories(subCategories)
        insertInterior(interior)
        insertMisc(misc)

        insertExteriorTags(exteriorTagsTags)
        insertInteriorTags(interiorTags)
        insertFocusTags(focusTags)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun subcategories(list: List<NewSubCatResponse.Subcategory>)

    @Insert
    fun insertInterior(list: List<NewSubCatResponse.Interior>) : List<Long>

    @Insert
    fun insertMisc(list: List<NewSubCatResponse.Miscellaneous>)

    @Query("SELECT * FROM subcategory")
    fun getSubcategories(): List<NewSubCatResponse.Subcategory>

    @Query("SELECT * FROM interior where prodCatId = :prodCatId")
    fun getInterior(prodCatId: String) : List<NewSubCatResponse.Interior>

    @Query("SELECT * FROM miscellaneous where prod_cat_id = :prodCatId")
    fun getMisc(prodCatId: String) : List<NewSubCatResponse.Miscellaneous>

    @Insert
    fun insertExteriorTags(list: List<NewSubCatResponse.Tags.ExteriorTags>) : List<Long>

    @Insert
    fun insertInteriorTags(list: List<NewSubCatResponse.Tags.InteriorTags>)

    @Insert
    fun insertFocusTags(list: List<NewSubCatResponse.Tags.FocusShoot>)

    @Insert
    fun insertOverlays(overlays: List<OverlaysResponse.Overlays>)

    @Query("SELECT * FROM overlays where prod_sub_cat_id = :prodSubcategoryId and fetchAngle = :fetchAngle")
    fun getOverlays(prodSubcategoryId: String, fetchAngle: Int) : List<OverlaysResponse.Overlays>

    @Insert
    fun insertBackgrounds(backgroundApps: List<CarsBackgroundRes.BackgroundApp>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMarketplace(marketplace:List<MarketplaceRes.Marketplace>)

    @Query("SELECT * FROM backgroundapp where categoryId = :categoryId")
    fun getBackgrounds(categoryId: String) : List<CarsBackgroundRes.BackgroundApp>

    @Query("SELECT * FROM marketplace where prod_cat_id = :categoryId")
    fun getMarketplaceByCategoryId(categoryId: String) : List<MarketplaceRes.Marketplace>



    @Query("update sku set isCreated = :isCreated where uuid = :uuid")
    fun updateSkuCreated(uuid: String,isCreated: Boolean = true): Int

    @Query("update project set isCreated = :isCreated where uuid = :uuid")
    fun updateProjectCreated(uuid: String,isCreated: Boolean = true): Int

    @Transaction
    fun updateProjectAndSkuCreated(projectUuid: String,skuUuid: String){
        val p = updateProjectCreated(projectUuid)
        val s = updateSkuCreated(skuUuid)
    }

    @Query("update sku set skuId = :skuId,projectId = :projectId, isCreated = :isCreated where uuid = :uuid")
    fun updateSKuServerId(uuid: String,skuId: String,projectId: String,isCreated: Boolean = true): Int

    @Query("update image set skuId = :skuId, projectId = :projectId where skuUuid = :skuUuid")
    fun updateImageIds(skuUuid: String,skuId: String,projectId: String): Int


    @Transaction
    fun updateSkuAndImageIds(projectId: String,skuUuid: String,skuId: String){
        updateSKuServerId(skuUuid,skuId,projectId)
        val ss = updateImageIds(skuUuid,skuId,projectId)
        val a = ""
    }

    @Update
    fun updateSku(skuApp: Sku): Int

    @Transaction
    fun updateSubcategory(skuApp: Sku){
//        val projectUpdate = updateProjectSubcategory(project.uuid,project.subCategoryName,project.subCategoryId)
//        Log.d(AppConstants.SHOOT_DAO_TAG, "updateSubcategory: $projectUpdate")
        val skuUpdate = updateSku(skuApp)
        Log.d(AppConstants.SHOOT_DAO_TAG, "updateSubcategory: $skuUpdate")
    }


    @Insert
    fun insertImage(obj: Image) : Long

    @Query("UPDATE project SET imagesCount = imagesCount + 1 WHERE uuid =:uuid ")
    fun updateProjectImageCount(uuid: String) : Int


    @Query("UPDATE project SET isCreated = :isCreated WHERE uuid =:uuid ")
    fun setProjectCreatedFalse(uuid: String,isCreated: Boolean = false) : Int


    @Query("UPDATE project SET imagesCount = imagesCount + 1, thumbnail= :thumbnail WHERE uuid =:uuid ")
    fun updateProjectThumbnail(uuid: String,thumbnail: String) : Int

    @Query("UPDATE sku SET imagesCount = imagesCount + 1, thumbnail= :thumbnail WHERE uuid =:uuid ")
    fun updateSkuThumbnail(uuid: String,thumbnail: String) : Int

    @Query("UPDATE sku SET imagesCount = imagesCount + 1 WHERE uuid =:uuid ")
    fun updateSkuImageCount(uuid: String) : Int

    @Query("select * from sku where uuid = :uuid")
    fun getSku(uuid: String) : Sku

    @Transaction
    fun saveImage(imageApp: Image){
        val sku = getSku(imageApp.skuUuid.toString())

        imageApp.apply {
            projectId = sku.projectId
            skuId = sku.skuId
        }

        val imageId = insertImage(imageApp)
        Log.d(AppConstants.SHOOT_DAO_TAG, "saveImage: $imageId")
        if (imageApp.sequence == 1){
            val thumbUpdate = updateProjectThumbnail(imageApp.projectUuid!!,imageApp.path)
            val skuThumbUpdate = updateSkuThumbnail(imageApp.skuUuid!!,imageApp.path)
            Log.d(AppConstants.SHOOT_DAO_TAG, "saveImage: $thumbUpdate")
            Log.d(AppConstants.SHOOT_DAO_TAG, "saveImage: $skuThumbUpdate")
        }
        else{
            val updateProjectImagesCount = updateProjectImageCount(imageApp.projectUuid!!)
            Log.d(AppConstants.SHOOT_DAO_TAG, "saveImage: $updateProjectImagesCount")

            val skuImagesUpdate = updateSkuImageCount(imageApp.skuUuid!!)
            Log.d(AppConstants.SHOOT_DAO_TAG, "saveImage: $skuImagesUpdate")
        }
    }

    @Query("Select * from exteriortags")
    fun getExteriorTags(): List<NewSubCatResponse.Tags.ExteriorTags>

    @Query("Select * from interiortags")
    fun getInteriorTags(): List<NewSubCatResponse.Tags.InteriorTags>

    @Query("Select * from focusshoot")
    fun getFocusTags(): List<NewSubCatResponse.Tags.FocusShoot>

    @Transaction
    fun updateBackground(map: HashMap<String, Any>, list: List<Sku>) {
        updateProjectStatus(
            map["project_uuid"].toString()
        )

        list.forEach {
            updateSkuBackground(
                it.uuid,
                backgroundName = map["bg_name"].toString(),
                backgroundId = map["bg_id"].toString(),
                marketplaceId = map["marketplace_id"].toString(),
                if (it.imagePresent == 1 && it.videoPresent == 1) it?.threeSixtyFrames?.plus(it?.imagesCount!!)!! else it?.imagesCount!!
            )
        }
    }

    @Query("UPDATE project SET status = 'ongoing' WHERE uuid =:uuid ")
    fun updateProjectStatus(uuid: String): Int

    @Query("UPDATE sku SET backgroundName = :backgroundName, backgroundId= :backgroundId, marketplaceId = :marketplaceId, totalFrames = :totalFrames, status =:status  WHERE uuid =:uuid ")
    fun updateSkuBackground(uuid: String,backgroundName: String,backgroundId: String, marketplaceId: String, totalFrames : Int, status: String = "ongoing") : Int

    @Transaction
    fun updateComboSkuAndProject(skuApp: Sku){
        setProjectCreatedFalse(skuApp.projectUuid!!)

        updateVideoSkuLocally(
            uuid = skuApp.uuid,
            subcategoryId = skuApp.subcategoryId!!,
            subcategoryName = skuApp.subcategoryName!!,
            initialFrames = skuApp.initialFrames!!
        )
    }

    @Query("UPDATE sku SET subcategoryId = :subcategoryId,subcategoryName = :subcategoryName,initialFrames = :initialFrames, imagePresent = :imagePresent,isCreated = :isCreated WHERE uuid =:uuid ")
    fun updateVideoSkuLocally(uuid: String,subcategoryId: String,subcategoryName: String,initialFrames: Int,imagePresent: Int = 1,isCreated: Boolean = false) : Int

    @Query("UPDATE sku SET totalFrames = totalFrames + :totalFrames  WHERE uuid =:uuid ")
    fun updateSkuTotalFrames(uuid: String,totalFrames : Int) : Int

    @Query("select status from sku WHERE uuid =:skuUuid")
    fun getSkuStatus(skuUuid: String): String
}