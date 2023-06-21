package com.spyneai.dashboard.data.repository


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.spyneai.app.AppExecutors
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.ProjectApiClient
import com.spyneai.base.network.SpyneAiApiClient
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.dashboard.ConnectivityUtil
import com.spyneai.dashboard.network.NetworkAndDBBoundResource
import com.spyneai.dashboard.network.Resource
import com.spyneai.dashboard.repository.DashboardLocalDS
import com.spyneai.dashboard.repository.model.CategoryDataAppDao
import com.spyneai.dashboard.repository.model.category.DynamicLayout
import com.spyneai.dashboard.response.CatAgnosticResV2
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

    private var spyneApi = SpyneAiApiClient().getClient()
    private var clipperApi = ClipperApiClient().getClient()
    private var projectApi = ProjectApiClient().getClient()

    fun getCategories(): List<NewCategoriesResponse.Category> {
        return Room.databaseBuilder(
            BaseApplication.getContext(),
            SpyneAppDatabase::class.java, "spyne-db"
        )
            .fallbackToDestructiveMigration()
            .build().dashboardDao().getAll()
    }

    suspend fun getCategories(
        auth_key: String
    ) = safeApiCall {
        clipperApi.getCategories(auth_key)
    }

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

    //ongoign completed
    suspend fun getProjects(
        tokenId: String,
        status: String
    ) = safeApiCall {
        clipperApi.getProjects(tokenId, status)
    }



    suspend fun getGCPUrl(
        imageName: String
    ) = safeApiCall {
        clipperApi.getGCPUrl(imageName)
    }

    suspend fun getUserCredits(
        userId: String
    ) = safeApiCall {
        clipperApi.userCreditsDetails(userId)
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



    suspend fun userCreditsDetails(
        authKey: String,
        entityId: String
    ) = safeApiCall {
        clipperApi.userCreditsDetails(authKey, entityId)
    }

    suspend fun availableCredits(
    ) = safeApiCall {
        clipperApi.availableCredits()
    }


    suspend fun userDetails(
        authKey: String,
        entityId: String
    ) = safeApiCall {
        clipperApi.userDetails(authKey, entityId)
    }

    suspend fun fetchCredits(
        authKey: String,
    ) = safeApiCall {
        clipperApi.fetchCredits(authKey)
    }

    suspend fun getRestaurantList(
        authKey: String,
    ) = safeApiCall {
        clipperApi.getRestaurantList(authKey)
    }

    suspend fun requestCredits(
        authKey: String,
    ) = safeApiCall {
        clipperApi.requestCredits(authKey)
    }

    suspend fun fetchCategory() = safeApiCall {
        clipperApi.fetchCategory()
    }

    fun insertCategories(
        data: List<NewCategoriesResponse.Category>,
        dynamicLayout: List<DynamicLayout>
    ): List<Long> {
        return Room.databaseBuilder(
            BaseApplication.getContext(),
            SpyneAppDatabase::class.java, "spyne-db"
        )
            .fallbackToDestructiveMigration()
            .build().dashboardDao().insert(data)
    }

    fun getCategoryOrientation(categoryId: String) =
        categoryDataAppDao?.getCategoryOrientation(categoryId)

    fun getCatAgnosData() = categoryDataAppDao.getCatAgnosData()
    fun getLiveDataCategoryById(catId: String) = categoryDataAppDao.getCategoryById(catId)

    suspend fun getCategoryData(
        catId: String
    ) = safeApiCall {
        clipperApi.getCategoryById(catId)
    }


    fun saveCatAgnosData(
        catList: List<CatAgnosticResV2.CategoryAgnos>,
        subCatList: ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>
    ) = categoryDataAppDao.saveCatAgnosData(catList, subCatList)

    fun getCategoriesData(
        catId: String
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

    suspend fun getSubscriptionPlan(
    ) = safeApiCall {
        clipperApi.getSubscriptionPlan()
    }

    suspend fun getPricingPlan(
    ) = safeApiCall {
        clipperApi.getPricingPlan()
    }
}