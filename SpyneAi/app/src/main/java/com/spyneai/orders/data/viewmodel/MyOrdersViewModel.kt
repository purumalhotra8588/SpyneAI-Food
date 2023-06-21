package com.spyneai.orders.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.room.withTransaction
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.ProjectApiClient
import com.spyneai.base.network.Resource
import com.spyneai.base.room.SpyneAppDatabase

import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.paging.PagedRepository
import com.spyneai.orders.data.paging.ProjectPagedRes
import com.spyneai.orders.data.repository.MyOrdersRepository
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.data.ProcessedRepository
import com.spyneai.shootapp.data.model.FilterStatus
import com.spyneai.reshoot.data.ReshootProjectRes
import com.spyneai.reshoot.data.ReshootSkuRes
import com.spyneai.shootapp.repository.model.image.Image
import com.spyneai.shootapp.repository.model.project.Project
import com.spyneai.shootapp.repository.model.sku.Sku


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MyOrdersViewModel : ViewModel() {
    var moveToZero: Boolean = false
    private val repository = MyOrdersRepository()
    private val processedRepository = ProcessedRepository()
    private val spyneAppDatabase = SpyneAppDatabase.getInstance(BaseApplication.getContext())


    @ExperimentalPagingApi
    fun getAllProjects(status: String): Flow<PagingData<Project>> {
        return PagedRepository(
            ProjectApiClient().getClient(),
            spyneAppDatabase,
            status,
            ""
        ).getSearchResultStream()
            .cachedIn(viewModelScope)
    }


    var refreshProjectData: MutableLiveData<Boolean> = MutableLiveData()
    val position: MutableLiveData<Int> = MutableLiveData()
    val ongoingCardPosition: MutableLiveData<Int> = MutableLiveData()
    val projectItemClicked: MutableLiveData<Boolean> = MutableLiveData()
    var aiScoreOrder: MutableLiveData<String> = MutableLiveData()
    var imageRatingOrder: MutableLiveData<String> = MutableLiveData()
    val alphabeticOrder: MutableLiveData<String> = MutableLiveData()
    val dateOrder: MutableLiveData<String> = MutableLiveData()
    val categoryOrder: MutableLiveData<List<String>> = MutableLiveData()
    val viewType: MutableLiveData<Boolean> = MutableLiveData()
    val onReshootDataSynced: MutableLiveData<Boolean> = MutableLiveData()
    val goToHomeFragment: MutableLiveData<Boolean> = MutableLiveData()


    private val _CompletedSKUsResponse: MutableLiveData<Resource<CompletedSKUsResponse>> =
        MutableLiveData()
    val completedSKUsResponse: LiveData<Resource<CompletedSKUsResponse>>
        get() = _CompletedSKUsResponse

    val updateOngoingProjects: MutableLiveData<Boolean> = MutableLiveData()
    val updateCompletedProjects: MutableLiveData<Boolean> = MutableLiveData()

    private val _getProjectsResponse: MutableLiveData<Resource<GetProjectsResponse>> =
        MutableLiveData()
    val getProjectsResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _getProjectsResponse


    private val _reshootRequestSkuRes: MutableLiveData<Resource<ReshootSkuRes>> =
        MutableLiveData()
    val reshootRequestSkuRes: LiveData<Resource<ReshootSkuRes>>
        get() = _reshootRequestSkuRes

    fun getReshootSkus(
    ) = viewModelScope.launch {
        _reshootRequestSkuRes.value = Resource.Loading
        _reshootRequestSkuRes.value = repository.getReshootSkus()
    }

    var _reshootProjectRes: MutableLiveData<Resource<ReshootProjectRes>> =
        MutableLiveData()
    val reshootProjectRes: LiveData<Resource<ReshootProjectRes>>
        get() = _reshootProjectRes

    fun getReshootProjectData(
        projectId: String
    ) = viewModelScope.launch {
        _reshootProjectRes.value = Resource.Loading
        _reshootProjectRes.value = repository.getReshootProjectData(projectId)

    }


    private val _categoryResponse: MutableLiveData<Resource<CatAgnosticResV2>> =
        MutableLiveData()
    val categoryResponse: LiveData<Resource<CatAgnosticResV2>>
        get() = _categoryResponse


    fun getProjects(
        tokenId: String, status: String
    ) = viewModelScope.launch {
        _getProjectsResponse.value = Resource.Loading
        _getProjectsResponse.value = repository.getProjects(tokenId, status)

    }

    private val _getCompletedProjectsResponse: MutableLiveData<Resource<GetProjectsResponse>> =
        MutableLiveData()
    val getCompletedProjectsResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _getCompletedProjectsResponse

    private val _getCompletedPagedResponse: MutableLiveData<Resource<ProjectPagedRes>> =
        MutableLiveData()
    val getCompletedPagedResponse: LiveData<Resource<ProjectPagedRes>>
        get() = _getCompletedPagedResponse

    fun getCompletedProjects(
        tokenId: String, status: String
    ) = viewModelScope.launch {
        _getCompletedProjectsResponse.value = Resource.Loading
        _getCompletedProjectsResponse.value = repository.getProjects(tokenId, status)
    }

    fun getPagedCompletedProjects() = viewModelScope.launch {
        _getCompletedPagedResponse.value = Resource.Loading
        _getCompletedPagedResponse.value = repository.getCompletedProjects()
    }

    private val _imagesOfSkuRes: MutableLiveData<Resource<ImagesOfSkuRes>> = MutableLiveData()
    val imagesOfSkuRes: LiveData<Resource<ImagesOfSkuRes>>
        get() = _imagesOfSkuRes

    fun getImages(skuId: String?, projectUuid: String,skuUuid: String) = viewModelScope.launch {
        _imagesOfSkuRes.value = Resource.Loading

        if (skuId != null && BaseApplication.getContext().isInternetActive()) {
            val response = processedRepository.getImagesOfSku(
                skuId = skuId
            )

            if (response is Resource.Success) {
                spyneAppDatabase.withTransaction {
                    spyneAppDatabase.imageDao().insertImagesWithCheck(
                        response.value.data as ArrayList<Image>,
                        projectUuid,
                        skuUuid
                    )
                }

                GlobalScope.launch(Dispatchers.IO) {
                    val response = spyneAppDatabase.imageDao().getImagesBySkuUuid(
                        skuUuid = skuUuid
                    )

                    GlobalScope.launch(Dispatchers.Main) {
                        _imagesOfSkuRes.value = Resource.Success(
                            ImagesOfSkuRes(
                                data = response,
                                message = "done",
                                "",
                                "",
                                200,
                                fromLocal = false
                            )
                        )
                    }
                }
            }else{
                _imagesOfSkuRes.value = response
            }
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                val response = spyneAppDatabase.imageDao().getImagesBySkuUuid(
                    skuUuid = skuUuid
                )

                GlobalScope.launch(Dispatchers.Main) {
                    _imagesOfSkuRes.value = Resource.Success(
                        ImagesOfSkuRes(
                            data = response,
                            message = "done",
                            "",
                            "",
                            200,
                            fromLocal = true
                        )
                    )
                }
            }

        }
    }

    fun getFilterCheck(): FilterStatus {
        return FilterStatus().apply {
                isAiScoreSelected = Utilities.getBool(
                    BaseApplication.getContext(),
                    "isAiScoreSelected" + AppConstants.FILTER_AISCORE_SELECTED, false
                )
                isImageRatingSelected = Utilities.getBool(
                    BaseApplication.getContext(),
                    "isImageRatingSelected" + AppConstants.FILTER_IMAGE_RATING_SELECTED, false
                )
                isDateSelected = Utilities.getBool(
                    BaseApplication.getContext(),
                    "isDateSelected" + AppConstants.FILTER_DATE_SELECTED, false
                )
            isAlphabeticalSelected = Utilities.getBool(
                BaseApplication.getContext(),
                "isAlphabeticalSelected" + AppConstants.FILTER_ALPHABETICAL_SELECTED, false
            )
            isAutomobileSelected = Utilities.getBool(
                BaseApplication.getContext(),
                "isAutomobileSelected" + AppConstants.FILTER_AUTOMOBILE_SELECTED, false
            )

            }
    }

    suspend fun savetoDB(response: ProjectPagedRes) {
        spyneAppDatabase.withTransaction {
            val ss = spyneAppDatabase.projectDao().insertWithCheck(response.data)

        }
    }

    fun syncData(data: ReshootProjectRes.Data) {
        viewModelScope.launch(Dispatchers.IO) {
            spyneAppDatabase.projectDao().insertWithCheck(listOf(data.projectAppData))

            val project = spyneAppDatabase.projectDao().getProjectByProjectId(data.projectAppData.projectId)

            data.skuAppData.forEach {
                val list = ArrayList<Sku>()
                list.add(it)

                spyneAppDatabase.skuDao().insertSkuWithCheck(list,project.uuid,projectId = project.projectId)

                val sku = spyneAppDatabase.skuDao().getSkuBySkuId(it.skuId)

                it.images?.let { images ->
                    sku.projectUuid?.let { it1 ->
                        spyneAppDatabase.imageDao().insertImagesWithCheck(images as ArrayList<Image>,
                            projectUuid = it1,skuUuid = sku.uuid)
                    }
                }
            }

            viewModelScope.launch(Dispatchers.Main) {
                onReshootDataSynced.value = true
            }
        }
    }

    fun getSku(skuId: String?) = spyneAppDatabase.skuDao().getSkuBySkuId(skuId)


    suspend fun getSubCatIDBySku(skuId: String) = spyneAppDatabase.skuDao().getSubCatIdOfSku(
        skuId = skuId
    )


    fun saveData(data: List<CatAgnosticResV2.CategoryAgnos>) {
        val subCatList = ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>()

        data.forEach {
            it.subCategoryV2s?.let { it ->
                subCatList.addAll(it)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            processedRepository.saveCatAgnosData(data,subCatList, spyneAppDatabase.categoryDataDao())
        }
    }


    fun getCategoryDataV2(catId : String) = viewModelScope.launch {
        _categoryResponse.value = Resource.Loading

        GlobalScope.launch(Dispatchers.IO) {
            val data = processedRepository.getLiveDataCategoryById(catId, spyneAppDatabase.categoryDataDao())

            if (data == null){
                val response = processedRepository.getCategoryData(catId, spyneAppDatabase.categoryDataDao())

                if (response is Resource.Success){
                    //save data
                    saveData(response.value.data)
                }
                GlobalScope.launch(Dispatchers.Main) {
                    _categoryResponse.value = processedRepository.getCategoryData(
                        catId,
                        spyneAppDatabase.categoryDataDao()
                    )
                }
            }else {
                val list = ArrayList<CatAgnosticResV2.CategoryAgnos>()
                list.add(data)

                GlobalScope.launch(Dispatchers.Main) {
                    _categoryResponse.value = Resource.Success(
                        CatAgnosticResV2(
                            data = list,
                            message = "Fetched Category",
                            status = 200
                        )
                    )
                }
            }
        }
    }


}