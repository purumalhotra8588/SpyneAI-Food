package com.spyneai.onboarding.data.repository

import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.dashboardV2.data.model.LogoutBody
import com.spyneai.loginsignup.models.Categories
import com.spyneai.loginsignup.models.CategoriesDao


class OnBoardingRepository(val categoriesDao: CategoriesDao? = null) : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()


    suspend fun newUserLogout(
        body: LogoutBody
    ) = safeApiCall {
        clipperApi.newUserLogout(body)

    }
    suspend fun loginWithPassword(
        map: MutableMap<String, Any?>
    ) = safeApiCall {
        clipperApi.loginWithPassword(map)
    }

    suspend fun signUp(
        map: MutableMap<String, String>
    ) = safeApiCall {
        clipperApi.signUp(map)
    }

    suspend fun loginWithOTPEmail(
        email_id: String,
        apiKey: String
    ) = safeApiCall {
        clipperApi.loginWithOTPEmail(email_id, apiKey)
    }

    suspend fun getCountries() = safeApiCall {
        clipperApi.getCountries()
    }

    suspend fun reqOtp(
        map: MutableMap<String, Any>
    ) = safeApiCall {
        clipperApi.reqOtp(map)
    }

    suspend fun verityOtp(
        map: MutableMap<String, String>
    ) = safeApiCall {
        clipperApi.postOtp(map)
    }

    suspend fun getCategoryData(catId: String) = safeApiCall {
        clipperApi.getCategoryById(catId)
    }

    suspend fun createEnterPrise(map: MutableMap<String, String>) = safeApiCall {
        clipperApi.createEnterPrise(map)
    }

    suspend fun createAdmin(map: MutableMap<String, Any>) = safeApiCall {
        clipperApi.createAdmin(map)
    }

    suspend fun fetchCategory() = safeApiCall {
        clipperApi.fetchCategory()
    }

    fun savePostApi(
        response: CatAgnosticResV2,
    ) {
        val subCatList = ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>()

        response.data[0].subCategoryV2s!!.forEach {
            response.data[0].subCategoryV2s?.let { it ->
                subCatList.addAll(it)
            }
        }
        SpyneAppDatabase.getInstance(BaseApplication.getContext()).categoryDataDao().saveCatAgnosData(response.data, subCatList)
    }

    suspend fun updateCountry(
        map: HashMap<String,String>
    ) = safeApiCall {
        clipperApi.updateCountry(map)
    }


    fun saveCategory(categories: Categories) = categoriesDao?.saveCategory(categories)

    fun getCategory(userId: String) = categoriesDao?.getCategory(userId)


}





























