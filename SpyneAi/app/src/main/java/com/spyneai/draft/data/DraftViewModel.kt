package com.spyneai.draft.data

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
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.isInternetActive
import com.spyneai.orders.data.repository.MyOrdersRepository
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.data.ProcessedRepository
import com.spyneai.shootapp.data.ImagesRepoV2
import com.spyneai.shootapp.data.AppShootLocalRepository
import com.spyneai.shootapp.data.ShootRepository
import com.spyneai.shootapp.repository.model.image.Image
import com.spyneai.shootapp.repository.model.sku.Sku


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DraftViewModel : ViewModel() {
    private val repository = MyOrdersRepository()
    private val shootRepository = ShootRepository()
    private val processedRepository = ProcessedRepository()
    private val spyneAppDatabase = SpyneAppDatabase.getInstance(BaseApplication.getContext())
    private val localRepository = AppShootLocalRepository(spyneAppDatabase.shootDao(),
        spyneAppDatabase.shootDao(),
        spyneAppDatabase.projectDao(),
        spyneAppDatabase.skuDao())
    private val imageRepositoryV2 = ImagesRepoV2(spyneAppDatabase.imageDao())


    val syncImages: MutableLiveData<Boolean> = MutableLiveData()

    private val _draftResponse: MutableLiveData<Resource<GetProjectsResponse>> = MutableLiveData()
    val draftResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _draftResponse

    private val _imagesOfSkuRes: MutableLiveData<Resource<ImagesOfSkuRes>> = MutableLiveData()
    val imagesOfSkuRes: LiveData<Resource<ImagesOfSkuRes>>
        get() = _imagesOfSkuRes

    fun getSkuStatus(uuid: String) = localRepository.getSkuStatus(uuid)

    @ExperimentalPagingApi
    fun getSkus(
        projectId: String?,
        projectUuid: String,
        videoData: Int
    ): Flow<PagingData<Sku>> {
        return SkuRepository(
            ProjectApiClient().getClient(),
            spyneAppDatabase,
            projectId,
            projectUuid,
            videoData
        ).getSearchResultStream()
            .cachedIn(viewModelScope)
    }


    fun getImages(skuId: String?, projectUuid: String, skuUuid: String, skuName: String?) =
        viewModelScope.launch {
            _imagesOfSkuRes.value = Resource.Loading

            if (skuId != null && BaseApplication.getContext().isInternetActive()) {
                val response = processedRepository.getImagesOfSku(
                    skuId = skuId
                )

                if (response is Resource.Success) {

                    //add sku name
                    skuName?.let {
                        skuName
                        response.value.data.forEach {
                            it.skuName = skuName
                        }
                    }

                    spyneAppDatabase.withTransaction {
                        spyneAppDatabase.imageDao().insertImagesWithCheck(
                            response.value.data as ArrayList<Image>,
                            projectUuid,
                            skuUuid
                        )

                    }

                    GlobalScope.launch(Dispatchers.IO) {
                        val response = spyneAppDatabase.imageDao().getImagesBySkuId(
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
                } else {
                    _imagesOfSkuRes.value = response
                }
            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    val response = spyneAppDatabase.imageDao().getImagesBySkuId(
                        skuUuid = skuUuid
                    )

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


    suspend fun getDraftsFromLocal() = localRepository.getDraftProjects()

    suspend fun getSkusByProjectId(projectId: String) =
        localRepository.getSkusByProjectId(projectId)

    suspend fun getImagesbySkuId(skuId: String) = imageRepositoryV2.getImagesBySkuId(skuId)

    fun getImagesOfSku(
        skuId: String
    ) = viewModelScope.launch {
        _imagesOfSkuRes.value = Resource.Loading
        _imagesOfSkuRes.value = processedRepository.getImagesOfSku(skuId)
    }

    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> =
        MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse

    fun getSubCategories(
        authKey: String, prodId: String
    ) = viewModelScope.launch {
        _subCategoriesResponse.value = Resource.Loading
        _subCategoriesResponse.value = shootRepository.getSubCategories(authKey, prodId)
    }
}