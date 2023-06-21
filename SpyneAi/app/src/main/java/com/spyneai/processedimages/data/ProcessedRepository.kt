package com.spyneai.processedimages.ui.data

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.credits.model.CreditResourceBody
import com.spyneai.credits.model.ProjectStatusBody
import com.spyneai.dashboard.repository.model.CategoryDataAppDao
import com.spyneai.dashboard.response.CatAgnosticResV2

class ProcessedRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()


    suspend fun getImagesOfSku(
        skuId: String
    ) = safeApiCall{
        clipperApi.getImagesOfSku(skuId = skuId)
    }

    suspend fun getCategoryData(
        catId: String, categoryDataAppDao: CategoryDataAppDao
    ) = safeApiCall {
        clipperApi.getCategoryById(catId)
    }


    suspend fun projectStatusUpdate(
        body: ProjectStatusBody
    ) = safeApiCall {
        clipperApi.projectStatusUpdate(body)
    }

    fun saveCatAgnosData(
        catList: List<CatAgnosticResV2.CategoryAgnos>,
        subCatList: ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>,
        categoryDataAppDao: CategoryDataAppDao
    ) = categoryDataAppDao.saveCatAgnosData(catList, subCatList)


    fun getLiveDataCategoryById(catId: String, categoryDataAppDao: CategoryDataAppDao) = categoryDataAppDao.getCategoryById(catId)

    suspend fun calculateCreditResponse(
        body: CreditResourceBody
    )= safeApiCall {
        clipperApi.calculateCredits(creditResourceBody = body)
    }

    suspend fun deductCredits(
        body: CreditResourceBody
    )= safeApiCall {
        clipperApi.deductCredits(creditResourceBody = body)
    }

    suspend fun updateProcessedImageState(
        imageId : String,
        skuId : String
    ) = safeApiCall {
        clipperApi.updateProcessedImageState(imageId=imageId, skuId =  skuId)
    }
}