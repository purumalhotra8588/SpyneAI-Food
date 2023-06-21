package com.spyneai.dashboard.repository

import com.spyneai.dashboard.repository.model.CategoryDataAppDao
import javax.inject.Inject
import com.spyneai.dashboard.response.CatAgnosticResV2

class DashboardLocalDS @Inject constructor(
    private val categoryDataAppDao: CategoryDataAppDao,
) {


    fun savePostApi(
        response: CatAgnosticResV2,
    ) {
        val subCatList = ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>()

        response.data[0].subCategoryV2s!!.forEach {
            response.data[0].subCategoryV2s?.let { it ->
                subCatList.addAll(it)
            }
        }
        categoryDataAppDao.saveCatAgnosData(response.data, subCatList)
    }
}