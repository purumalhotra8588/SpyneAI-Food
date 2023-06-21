package com.spyneai.processedimages.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.credits.model.CalculateCreditResponse
import com.spyneai.credits.model.CreditResourceBody
import com.spyneai.credits.model.DeductCreditResponse
import com.spyneai.credits.model.ProjectStatusBody
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.data.MessageRes
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.data.ProcessedRepository

import com.spyneai.shootapp.data.AppShootLocalRepository
import com.spyneai.shootapp.data.ShootRepository
import com.spyneai.shootapp.repository.model.image.Image
import com.spyneai.shootapp.repository.model.payment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProcessedViewModelApp : ViewModel() {

    val TAG = "ProcessedViewModelApp"
    val paymentStatus: MutableLiveData<PaymentStatus> = MutableLiveData()
    val refreshCreditCalculation: MutableLiveData<Boolean> = MutableLiveData()
    val downloadAllImage: MutableLiveData<Boolean> = MutableLiveData()

    private val spyneAppDatabase = SpyneAppDatabase.getInstance(BaseApplication.getContext())
    private val localRepository = AppShootLocalRepository(
        spyneAppDatabase.shootDao(),
        spyneAppDatabase.shootDao(),
        spyneAppDatabase.projectDao(),
        spyneAppDatabase.skuDao(),
        imageDaoApp = spyneAppDatabase.imageDao()
    )
    private val processedRepository = ProcessedRepository()
    private val repository = ShootRepository()

    private val _imagesOfSkuRes: MutableLiveData<Resource<ImagesOfSkuRes>> = MutableLiveData()
    val imagesOfSkuRes: LiveData<Resource<ImagesOfSkuRes>>
        get() = _imagesOfSkuRes


    private val _updatedImages: MutableLiveData<Image> =
        MutableLiveData()
    val updatedImages: LiveData<Image>
        get() = _updatedImages

    var projectId: String? = null
    var projectUuid: String? = null
    var skuId: String? = null
    var skuUuid: String? = null
    var skuName: String? = null
    var selectedImageUrl: String? = null
    var categoryId: String? = null
    var imageType: String? = null

    var creditDeficiet = 0
    var creditBalance = 0

    val reshoot = MutableLiveData<Boolean>()
    var skuSucessResultCount: MutableLiveData<Int> = MutableLiveData()
    var downloadImage: MutableLiveData<Boolean> = MutableLiveData()
    var isImageProcessed: MutableLiveData<Boolean> = MutableLiveData()
    var showSpyne360: MutableLiveData<Boolean> = MutableLiveData()


    private val _projectStatusUpdate: MutableLiveData<Resource<MessageRes>> =
        MutableLiveData()
    val projectStatusUpdate: LiveData<Resource<MessageRes>>
        get() = _projectStatusUpdate

    fun projectStatusUpdate(body: ProjectStatusBody) =
        viewModelScope.launch {
            _projectStatusUpdate.value = Resource.Loading
            _projectStatusUpdate.value = processedRepository.projectStatusUpdate(body)
        }


    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> =
        MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse


    suspend fun getSkuImageList(uuid: String): List<Image>? {
        val images = spyneAppDatabase.imageDao().getImagesBySkuUuid(uuid)
        return images
    }

    fun getSubCategories(
        authKey: String, prodId: String
    ) = viewModelScope.launch {
        _subCategoriesResponse.value = Resource.Loading

        GlobalScope.launch(Dispatchers.IO) {
            val subcatList = localRepository.getSubcategories()

            if (!subcatList.isNullOrEmpty()) {
                val interiorList = localRepository.getInteriorList(prodId)
                val miscList = localRepository.getMiscList(prodId)

                val exteriorTags = localRepository.getExteriorTags()
                val interiorTags = localRepository.getInteriorTags()
                val focusTags = localRepository.getFocusTags()


                GlobalScope.launch(Dispatchers.Main) {
                    _subCategoriesResponse.value = Resource.Success(
                        NewSubCatResponse(
                            data = subcatList,
                            interior = interiorList,
                            "",
                            miscellaneous = miscList,
                            200,
                            tags = NewSubCatResponse.Tags(exteriorTags, focusTags, interiorTags)
                        )
                    )
                }
            } else {
                val response = repository.getSubCategories(authKey, prodId)

                if (response is Resource.Success) {
                    //save response to local DB
                    GlobalScope.launch(Dispatchers.IO) {
                        val subcatList = response.value.data
                        val interiorList =
                            if (response.value.interior.isNullOrEmpty()) ArrayList() else response.value.interior
                        val miscList =
                            if (response.value.miscellaneous.isNullOrEmpty()) ArrayList() else response.value.miscellaneous

                        val exteriorTags =
                            if (response.value.tags.exteriorTags.isNullOrEmpty()) ArrayList() else response.value.tags.exteriorTags
                        val interiorTags =
                            if (response.value.tags.interiorTags.isNullOrEmpty()) ArrayList() else response.value.tags.interiorTags
                        val focusTags =
                            if (response.value.tags.focusShoot.isNullOrEmpty()) ArrayList() else response.value.tags.focusShoot

                        localRepository.insertSubCategories(
                            subcatList,
                            interiorList,
                            miscList,
                            exteriorTags,
                            interiorTags,
                            focusTags
                        )

                        GlobalScope.launch(Dispatchers.Main) {
                            _subCategoriesResponse.value = Resource.Success(
                                NewSubCatResponse(
                                    subcatList,
                                    interiorList,
                                    "",
                                    miscList,
                                    200,
                                    response.value.tags
                                )
                            )
                        }
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        _subCategoriesResponse.value = response
                    }
                }
            }
        }
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
                        val localResponse = spyneAppDatabase.imageDao().getImagesBySkuUuid(
                            skuUuid = skuUuid
                        )

                        GlobalScope.launch(Dispatchers.Main) {
                            _imagesOfSkuRes.value = Resource.Success(
                                ImagesOfSkuRes(
                                    data = localResponse,
                                    message = "done",
                                    "",
                                    "",
                                    200,
                                    threeSixtyFrame = response.value.threeSixtyFrame
                                )
                            )
                        }
                    }
                } else {
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
                                200
                            )
                        )
                    }
                }

            }
        }

    private val _categoryResponse: MutableLiveData<Resource<CatAgnosticResV2>> =
        MutableLiveData()
    val categoryResponse: LiveData<Resource<CatAgnosticResV2>>
        get() = _categoryResponse

    fun getCategoryDataV2(catId: String) = viewModelScope.launch {
        _categoryResponse.value = Resource.Loading

        GlobalScope.launch(Dispatchers.IO) {
            val data =
                processedRepository.getLiveDataCategoryById(catId, spyneAppDatabase.categoryDataDao())

            if (data == null) {
                val response =
                    processedRepository.getCategoryData(catId, spyneAppDatabase.categoryDataDao())

                if (response is Resource.Success) {
                    //save data
                    saveData(response.value.data)
                }
                GlobalScope.launch(Dispatchers.Main) {
                    _categoryResponse.value = processedRepository.getCategoryData(
                        catId,
                        spyneAppDatabase.categoryDataDao()
                    )
                }
            } else {
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

    fun saveData(data: List<CatAgnosticResV2.CategoryAgnos>) {
        val subCatList = ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>()

        data.forEach {
            it.subCategoryV2s?.let { it ->
                subCatList.addAll(it)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            processedRepository.saveCatAgnosData(data, subCatList, spyneAppDatabase.categoryDataDao())
        }
    }

    suspend fun getSubCatIDBySku(skuId: String) = spyneAppDatabase.skuDao().getSubCatIdOfSku(
        skuId = skuId
    )

    private val _calculateCreditsRes: MutableLiveData<Resource<CalculateCreditResponse>> =
        MutableLiveData()
    val calculateCreditsRes: LiveData<Resource<CalculateCreditResponse>>
        get() = _calculateCreditsRes


    private val _deductCreditRes: MutableLiveData<Resource<DeductCreditResponse>> =
        MutableLiveData()
    val deductCreditRes: LiveData<Resource<DeductCreditResponse>>
        get() = _deductCreditRes


    fun calculateCredits(
        body: CreditResourceBody
    ) = viewModelScope.launch {
        _calculateCreditsRes.value = Resource.Loading
        _calculateCreditsRes.value = processedRepository.calculateCreditResponse(body)
    }


    fun deductCredits(
        body: CreditResourceBody
    ) = viewModelScope.launch {
        _deductCreditRes.value = Resource.Loading
        _deductCreditRes.value = processedRepository.deductCredits(body)
    }

    private val _generateOrderRes: MutableLiveData<Resource<GenerateOrderRes>> =
        MutableLiveData()
    val generateOrderRes: LiveData<Resource<GenerateOrderRes>>
        get() = _generateOrderRes


    fun generateOrderId(finalAmount: Double, totalAmount: Double) = viewModelScope.launch {
        _generateOrderRes.value = Resource.Loading
        _generateOrderRes.value = repository.generateOrder(
            GenerateOrderBody(
                finalAmount = finalAmount,
                totalAmount = totalAmount,
                discount = Utilities.getPreference(
                    BaseApplication.getContext(),
                    AppConstants.ENTERPRISE_DISCOUNT
                )!!.toInt(),
                authKey = Utilities.getPreference(
                    BaseApplication.getContext(),
                    AppConstants.AUTH_KEY
                ).toString(),
                source = "App_android"
            )
        )
    }

    private val _sendPaymentIdRes: MutableLiveData<Resource<PaymentIdRes>> =
        MutableLiveData()
    val sendPaymentIdRes: LiveData<Resource<PaymentIdRes>>
        get() = _sendPaymentIdRes


    fun sendPaymentId(body: PaymentIdBody) = viewModelScope.launch {
        _sendPaymentIdRes.value = Resource.Loading
        _sendPaymentIdRes.value = repository.sendPaymentId(body)
    }

    fun getProcessedImageData(skuUuid: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val uploadedImage = spyneAppDatabase.imageDao().getUploadedImage(skuUuid)

            Log.d(TAG, "getProcessedImageData: ${uploadedImage?.name}")

            uploadedImage?.let { image ->
                val firstUploadedImageId = image.imageId
                val firstUploadedImageSkuId = image.skuId

                firstUploadedImageSkuId?.let { skuId ->
                    firstUploadedImageId?.let { imageId ->
                        updateProcessedImage(image)
                    }
                }
            }
        }
    }

    fun updateProcessedImage(
        image: Image
    ) = viewModelScope.launch {

        val it = processedRepository.updateProcessedImageState(
            imageId = image.imageId.toString(),
            skuId = image.skuId.toString()
        )

        if (it is Resource.Success) {
            if (it.value.data.isNotEmpty()) {
                val item = it.value.data[0]
                if (!item.output_image_lres_url.isNullOrEmpty()) {
                    withContext(Dispatchers.IO) {
                        spyneAppDatabase.withTransaction {
                            image.projectUuid?.let { projectUuid ->
                                image.skuUuid?.let { skuUuid ->
                                    spyneAppDatabase.imageDao().insertImagesWithCheck(
                                        it.value.data as java.util.ArrayList<Image>,
                                        projectUuid,
                                        skuUuid
                                    )
                                }
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if(item.input_image_hres_url.isNullOrEmpty())
                            item.input_image_hres_url = ""

                        if(item.input_image_lres_url.isNullOrEmpty())
                            item.input_image_lres_url = ""

                        if(item.output_image_hres_url.isNullOrEmpty())
                            item.output_image_hres_url = ""

                        if(item.output_image_lres_url.isNullOrEmpty())
                            item.output_image_lres_url = ""

                        if(item.output_image_lres_wm_url.isNullOrEmpty())
                            item.output_image_lres_wm_url = ""

                        _updatedImages.value = item
                    }
                }
            }
        }
    }

}