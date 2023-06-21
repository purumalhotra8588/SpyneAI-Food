
package com.spyneai.dashboard.repository


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.spyneai.app.AppExecutors
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.dashboard.ConnectivityUtil
import com.spyneai.dashboard.network.NetworkAndDBBoundResource
import com.spyneai.dashboard.network.Resource
import com.spyneai.dashboard.repository.model.CategoryDataAppDao
import com.spyneai.dashboard.repository.model.category.DynamicLayout
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.dashboard.response.CategoryAgnosticResponse
import com.spyneai.dashboard.response.NewCategoriesResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val categoryDataAppDao: CategoryDataAppDao,
    private val dashboardLocalDS: DashboardLocalDS,
    private val apiServices: ClipperApi,
    @ApplicationContext val context: Context,
    private val appExecutors: AppExecutors = AppExecutors()
) : BaseRepository() {

    //    private var spyneApi = SpyneAiA  piClient().getClient()
    private var clipperApi = ClipperApiClient().getClient()


    suspend fun getCategories(
        auth_key: String
    ) = safeApiCall {
        clipperApi.getCategories(auth_key)
    }

    fun getCategoryOrientation(categoryId: String) = categoryDataAppDao?.getCategoryOrientation(categoryId)

    suspend fun getOngoingSKUs(
        tokenId: String
    ) = safeApiCall {
        clipperApi.getOngoingSKUs(tokenId)
    }

    suspend fun getCompletedProjects(
        auth_key: String
    ) = safeApiCall {
        clipperApi.getCompletedSkus(auth_key)
    }

    //on going completed
    suspend fun getProjects(
        tokenId: String,
        status: String
    ) = safeApiCall {
        clipperApi.getProjects(tokenId, status)
    }

    suspend fun getUserCredits(
        userId : String
    )= safeApiCall {
        clipperApi.userCreditsDetails(userId)
    }



    suspend fun getGCPUrl(
        imageName: String
    ) = safeApiCall {
        clipperApi.getGCPUrl(imageName)
    }

    suspend fun captureCheckInOut(

        location: JSONObject,
        location_id: String,
        imageUrl: String = ""
    ) = safeApiCall {
        clipperApi.captureCheckInOut(location, location_id, imageUrl)
    }

    suspend fun getLocations(
    ) = safeApiCall {
        clipperApi.getLocations()
    }




    fun saveCatAgnosData(
        catList: List<CatAgnosticResV2.CategoryAgnos>,
        subCatList: ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>
    ) = categoryDataAppDao.saveCatAgnosData(catList, subCatList)



    fun getCategoriesData(catId : String
    ): LiveData<Resource<CatAgnosticResV2.CategoryAgnos?>> {
        return object :
            NetworkAndDBBoundResource<CatAgnosticResV2.CategoryAgnos, CatAgnosticResV2>(
                appExecutors
            ) {
            override fun saveCallResult(item: CatAgnosticResV2) {
                if (!item.data.isNullOrEmpty()) {
                    dashboardLocalDS.savePostApi(item)
                }
            }
            override fun loadFromDb() = categoryDataAppDao.getLiveDataCategoryById(catId)

            override fun createCall() = apiServices.getLivaDataCategoryById(catId)

            override fun shouldFetch(data: CatAgnosticResV2.CategoryAgnos?) =
                ConnectivityUtil.isConnected(context)

        }.asLiveData()
    }

    fun getCatAgnosData() = categoryDataAppDao.getCatAgnosData()
    fun getLiveDataCategoryById(catId: String) = categoryDataAppDao.getCategoryById(catId)

    suspend fun getCategoryData(catId: String
    ) = safeApiCall {
        clipperApi.getCategoryById(catId)
    }

//    fun getCatData(): LiveData<List<CategoryAgnosticResponse.CategoryListData>> {
//        return categoryDataDao.getCategory()
//    }
//
//    fun getInterior(): LiveData<List<CategoryAgnosticResponse.InteriorListData>> {
//        return categoryDataDao.getInterior()
//    }
//
//    fun getCameraSettings(): List<CategoryAgnosticResponse.CameraSettings> {
//        return categoryDataDao.getCameraSettings()
//    }


}



