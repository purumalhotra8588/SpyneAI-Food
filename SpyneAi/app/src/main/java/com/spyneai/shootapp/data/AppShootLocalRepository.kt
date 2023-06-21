package com.spyneai.shootapp.data

import androidx.room.Query
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.repository.model.CategoryDataAppDao
import com.spyneai.dashboard.response.NewSubCatResponse

import com.spyneai.shootapp.repository.model.image.ImageDao
import com.spyneai.shootapp.data.model.CarsBackgroundRes
import com.spyneai.shootapp.data.model.MarketplaceRes
import com.spyneai.shootapp.repository.db.RecentBackgroundDao
import com.spyneai.shootapp.repository.db.ShootDaoApp
import com.spyneai.shootapp.repository.model.RecentBackground
import com.spyneai.shootapp.repository.model.image.Image
import com.spyneai.shootapp.repository.model.project.Project
import com.spyneai.shootapp.repository.model.project.ProjectDao
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.shootapp.repository.model.sku.SkuDao


class AppShootLocalRepository(
    val shootDaoApp: ShootDaoApp,
    val shootDaoSdk: ShootDaoApp,
    val projectDaoApp: ProjectDao,
    val skuDaoApp: SkuDao,
    val categoryDataAppDao: CategoryDataAppDao? = null,
    val recentBackgroundDao: RecentBackgroundDao? = null,
    val imageDaoApp: ImageDao? = null
) {

    private val TAG = "ShootLocalRepository"
    fun insertProject(projectApp: Project): Long {
        return projectDaoApp.insertProject(projectApp)
    }

    suspend fun insertSku(
        Sku: Sku,
        projectApp: Project
    ) = skuDaoApp.saveSku(Sku, projectApp)


    fun getDraftProjects() = projectDaoApp.getDraftProjects()

    fun getSubcategories(): List<NewSubCatResponse.Subcategory> {
        return shootDaoApp.getSubcategories()
    }

    fun getSubcategoriesV2(categoryId: String) = categoryDataAppDao?.getSubcategories(categoryId)

    fun getShootExperience(categoryId: String) = categoryDataAppDao?.getShootExperience(categoryId)

    fun getSubcategory(prodSubCatId: String) = categoryDataAppDao?.getSubcategory(prodSubCatId)

    fun insertSubCategories(
        data: List<NewSubCatResponse.Subcategory>,
        interior: List<NewSubCatResponse.Interior>,
        misc: List<NewSubCatResponse.Miscellaneous>,
        exteriorTagsTags: List<NewSubCatResponse.Tags.ExteriorTags>,
        interiorTags: List<NewSubCatResponse.Tags.InteriorTags>,
        focusTags: List<NewSubCatResponse.Tags.FocusShoot>
    ) {
        shootDaoApp.saveSubcategoriesData(
            data,
            interior,
            misc,
            exteriorTagsTags,
            interiorTags,
            focusTags
        )
    }

    fun getInteriorList(subcatId: String) = shootDaoApp.getInterior(subcatId)

    fun getMiscList(subcatId: String) = shootDaoApp.getMisc(subcatId)

    fun insertOverlays(overlays: List<OverlaysResponse.Overlays>) =
        shootDaoApp.insertOverlays(overlays)

    fun getOverlays(prodSubcategoryId: String, frames: String) =
        shootDaoApp.getOverlays(prodSubcategoryId, frames.toInt())

    fun insertBackgrounds(backgroundApps: List<CarsBackgroundRes.BackgroundApp>) =
        shootDaoApp.insertBackgrounds(backgroundApps)

    fun insertMarketplace(marketplace:List<MarketplaceRes.Marketplace>) =
        shootDaoApp.insertMarketplace(marketplace)

    fun getBackgrounds(categoryId: String) = shootDaoApp.getBackgrounds(categoryId)

    fun getMarketplaceByCatId(categoryId: String) = shootDaoApp.getMarketplaceByCategoryId(categoryId)
    fun getMarketplaceBySubCatId(categoryId: String) = shootDaoApp.getMarketplaceByCategoryId(categoryId)


    fun getExteriorTags() = shootDaoApp.getExteriorTags()

    fun getInteriorTags() = shootDaoApp.getInteriorTags()

    fun getFocusTags() = shootDaoApp.getFocusTags()

    fun updateSubcategory(
        Sku: Sku
    ) = shootDaoSdk.updateSku(Sku)

    fun updateSkuExteriorAngles(Sku: Sku) {
//        sku.isSelectAble = true
        shootDaoSdk.updateSku(Sku)
    }

    fun getSkusByProjectId(uuid: String) = skuDaoApp.getSkusByProjectId(uuid)

    fun getProject(uuid: String) = projectDaoApp.getProject(uuid)

    fun getSkuById(uuid: String) = shootDaoSdk.getSku(uuid)

    fun getSkuBySkuId(skuId: String) = skuDaoApp.getSkuBySkuId(skuId)

    fun updateBackground(map: HashMap<String, Any>) {
        val list = skuDaoApp.getDraftSkusByProjectId(map["project_uuid"].toString())

        shootDaoSdk.updateSkuBackground(
            uuid = map["sku_uuid"].toString(),
            backgroundName = map["bg_name"].toString(),
            backgroundId = map["bg_id"].toString(),
            marketplaceId = map["marketplace_id"].toString(),
            totalFrames = map["total_frames"] as Int,
            status = "ongoing"
        )
        //shootDaoSdk.updateBackground(map,list)
    }


    fun getAllProjectsSize() = projectDaoApp.getAllProjects().size
    fun getSkusCountByProjectUuid(uuid: String) = projectDaoApp.getSkusCountByProjectUuid(uuid)

    fun saveBackground(category: String, categoryId: String, backgroundApp: CarsBackgroundRes.BackgroundApp){
        backgroundApp.imageUrl?.let {
        recentBackgroundDao?.insert(
            RecentBackground(
                    parentId = categoryId,
                    categoryId = category,
                    bgName = backgroundApp.bgName,
                    gifUrl = backgroundApp.gifUrl,
                    imageCredit = backgroundApp.imageCredit,
                    bgId = backgroundApp.imageId,
                    imageUrl = backgroundApp.imageUrl
                )
        )
        }
    }


    fun insertImage(imageApp: Image) {
        return shootDaoSdk.saveImage(imageApp)
    }

    fun updateSkuTotalFrames(uuid: String,totalFrames: Int) = shootDaoSdk.updateSkuTotalFrames(uuid,totalFrames)

    fun updateVideoSkuLocally(Sku: Sku) = shootDaoSdk.updateComboSkuAndProject(Sku)

    suspend fun getLiveDataCategoryById(categoryId: String) = categoryDataAppDao?.getLiveDataCategoryById(categoryId)

    suspend fun getCategoryById(categoryId: String) = categoryDataAppDao?.getCategoryById(categoryId)

    fun getRecentBg(categoryId: String) = recentBackgroundDao?.getRecentBg(parentId = categoryId)

    fun getExteriorImages(uuid: String) = imageDaoApp?.getExteriorImages(uuid)

    fun updateProject(project: Project) = projectDaoApp.updateProject(project)
    fun updateProject(uuid: String) = projectDaoApp.updateProject(uuid)

    fun getCategoryOrientation(categoryId: String) = categoryDataAppDao?.getCategoryOrientation(categoryId)

//    fun updateProjectCredits(uuid: String, finalCredits: Credits) = projectDaoApp?.updateProjectCredits(uuid,finalCredits)

    suspend fun updateSkuName(uuid: String, skuName: String) = projectDaoApp?.updateSkuName(uuid,skuName)

    suspend fun updateProjectName(uuid: String, projectName: String) = projectDaoApp?.updateProjectName(uuid,projectName)

    fun getSkuStatus(uuid : String) = shootDaoSdk.getSkuStatus(uuid)

}
