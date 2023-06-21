package com.spyneai.shootapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.TAG
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.credits.model.DownloadHDRes
import com.spyneai.credits.model.ReduceCreditResponse
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.food.ImageBody
import com.spyneai.food.MarkDoneBody
import com.spyneai.food.StableDiffusionMarkDoneResponse
import com.spyneai.food.StableDiffusionResponse
import com.spyneai.isInternetActive
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.shootapp.repository.model.project.Project
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.shootapp.data.model.CarsBackgroundRes
import com.spyneai.shootapp.data.model.MarketplaceRes
import com.spyneai.shootapp.data.model.ProcessSkuRes
import com.spyneai.shootapp.data.model.UpdateTotalFramesRes


import com.spyneai.shootapp.response.SkuProcessStateResponse
import com.spyneai.shootapp.utils.objectToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProcessViewModelApp : ViewModel() {


    val TAG = "ProcessViewModelApp"

    private val repository = ProcessRepository()
    private val db = SpyneAppDatabase.getInstance(BaseApplication.getContext())
    private val sdkDb = SpyneAppDatabase.getInstance(BaseApplication.getContext())
    private val localRepository = AppShootLocalRepository(
        db.shootDao(),
        sdkDb.shootDao(),
        sdkDb.projectDao(),
        sdkDb.skuDao(),
        recentBackgroundDao = db.recentBackgroundDao(),
        imageDaoApp = sdkDb.imageDao(),
        categoryDataAppDao = db.categoryDataDao()
    )

    var category: CatAgnosticResV2.CategoryAgnos? = null
    var processDataMap = HashMap<String, Any>()

    var fromVideo = false

    var enableAfter = true
    var enableBefore = false
    val exteriorAngles: MutableLiveData<Int> = MutableLiveData()

    var skuApp: Sku? = null
    var projectApp: Project? = null
    val showFoodSkuDetails: MutableLiveData<Boolean> = MutableLiveData()
    val startTimer: MutableLiveData<Boolean> = MutableLiveData()
    val processSku: MutableLiveData<Boolean> = MutableLiveData()
    val foodoutputfragment: MutableLiveData<String> = MutableLiveData()
    val skuQueued: MutableLiveData<Boolean> = MutableLiveData()
    var addRegularShootSummaryFragment: MutableLiveData<Boolean> = MutableLiveData()
    var backgroundAppSelect: CarsBackgroundRes.BackgroundApp? = null
    var marketplaceSelect: MarketplaceRes.Marketplace? = null

    val projectId: MutableLiveData<String> = MutableLiveData()

    var isRegularShootSummaryActive = false

    var interiorMiscShootsCount = 0

    var categoryId: String? = null
    var subCategoryId: String? = null

    val _carGifRes: MutableLiveData<Resource<CarsBackgroundRes>> = MutableLiveData()
    val carGifRes: LiveData<Resource<CarsBackgroundRes>>
        get() = _carGifRes


    private val _processSkuRes: MutableLiveData<Resource<ProcessSkuRes>> = MutableLiveData()
    val processSkuRes: LiveData<Resource<ProcessSkuRes>>
        get() = _processSkuRes

    private val _userCreditsRes: MutableLiveData<Resource<CreditDetailsResponse>> =
        MutableLiveData()
    val userCreditsRes: LiveData<Resource<CreditDetailsResponse>>
        get() = _userCreditsRes


    private val _reduceCreditResponse: MutableLiveData<Resource<ReduceCreditResponse>> =
        MutableLiveData()
    val reduceCreditResponse: LiveData<Resource<ReduceCreditResponse>>
        get() = _reduceCreditResponse


    private var _stableDiffusionResponse: MutableLiveData<Resource<StableDiffusionResponse>?> =
        MutableLiveData()
    val stableDiffusionResponse: MutableLiveData<Resource<StableDiffusionResponse>?>
        get() = _stableDiffusionResponse


    private val _stableDiffusionMarkDone: MutableLiveData<Resource<StableDiffusionMarkDoneResponse>> =
        MutableLiveData()
    val stableDiffusionMarkDone: LiveData<Resource<StableDiffusionMarkDoneResponse>>
        get() = _stableDiffusionMarkDone

    private val _downloadHDRes: MutableLiveData<Resource<DownloadHDRes>> = MutableLiveData()
    val downloadHDRes: LiveData<Resource<DownloadHDRes>>
        get() = _downloadHDRes

    private val _skuProcessStateWithBgResponse: MutableLiveData<Resource<SkuProcessStateResponse>> =
        MutableLiveData()
    val skuProcessStateWithBgResponse: LiveData<Resource<SkuProcessStateResponse>>
        get() = _skuProcessStateWithBgResponse

    private val _updateTotalFramesRes: MutableLiveData<Resource<UpdateTotalFramesRes>> =
        MutableLiveData()
    val updateTotalFramesRes: LiveData<Resource<UpdateTotalFramesRes>>
        get() = _updateTotalFramesRes


    fun getBackgroundGifCars(
        fetchId: String,
        map: HashMap<String, Any>,
        getMarketPlaceBySubCat: Boolean = false
    ) = viewModelScope.launch {
        _carGifRes.value = Resource.Loading

        GlobalScope.launch(Dispatchers.IO) {
            val backgroundList = localRepository.getBackgrounds(fetchId)

            val marketplaceList =
                if (getMarketPlaceBySubCat) localRepository.getMarketplaceBySubCatId(fetchId) else localRepository.getMarketplaceByCatId(
                    fetchId
                )

            val checkList = if (!category!!.fetchMarketplace)
                backgroundList
            else
                marketplaceList


            if (!checkList.isNullOrEmpty() && !BaseApplication.getContext()
                    .isInternetActive()
            ) {
                val finalList = if (category?.fetchBackgroundsBy == "prodCatId")
                    backgroundList
                else
                    backgroundList?.filter {
                        it.prodSubCatId == fetchId
                    }

                val finalMarketList = if (category?.fetchMarketplaceBy == "prodCatId")
                    marketplaceList
                else
                    marketplaceList?.filter {
                        it.prod_sub_cat_id == skuApp?.subcategoryId
                    }

                GlobalScope.launch(Dispatchers.Main) {
                    _carGifRes.value = Resource.Success(
                        CarsBackgroundRes(finalList, finalMarketList)
                    )
                }
            } else {
                val response = repository.getBackgroundGifCars(map)

                if (response is Resource.Success) {
                    //insert overlays
                    val bgList = response.value.data
                    val marketplaceList = response.value.marketPlace

                    var finalBgList = ArrayList<CarsBackgroundRes.BackgroundApp>()


                    bgList.forEach {
                        if (it.bgName != null && it.imageUrl != null) {
                            it.categoryId = fetchId
                            it.prodCatId = fetchId
                            finalBgList.add(it)
                        }
                    }
                    localRepository.insertBackgrounds(finalBgList)
                    localRepository.insertMarketplace(marketplaceList)

                    GlobalScope.launch(Dispatchers.Main) {
                        _carGifRes.value = response
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        _carGifRes.value = response
                    }
                }
            }
        }
    }


    fun updateCarTotalFrames(authKey: String, skuId: String, totalFrames: String) =
        viewModelScope.launch {
            _updateTotalFramesRes.value = Resource.Loading
            _updateTotalFramesRes.value = repository.updateTotalFrames(authKey, skuId, totalFrames)
        }


    fun getUserCredits(
        userId: String
    ) = viewModelScope.launch {
        _userCreditsRes.value = Resource.Loading
        _userCreditsRes.value = repository.getUserCredits(userId)
    }

    fun reduceCredit(
        userId: String,
        creditReduce: String,
        skuId: String
    ) = viewModelScope.launch {
        _reduceCreditResponse.value = Resource.Loading
        _reduceCreditResponse.value = repository.reduceCredit(userId, creditReduce, skuId)
    }

    fun stableDiffusion(
        imageBody: ImageBody
    ) = viewModelScope.launch {
        _stableDiffusionResponse.value = Resource.Loading
        _stableDiffusionResponse.value = repository.stableDiffusion(imageBody)

    }

    fun stableDiffusionMarkDone(
        imageBodyMarkDone: MarkDoneBody
    ) = viewModelScope.launch {
        _stableDiffusionMarkDone.value = Resource.Loading
        _stableDiffusionMarkDone.value = repository.stableDiffusionMarkDone(imageBodyMarkDone)
    }


    fun updateDownloadStatus(
        userId: String,
        skuId: String,
        enterpriseId: String,
        downloadHd: Boolean
    ) = viewModelScope.launch {
        _downloadHDRes.value = Resource.Loading
        _downloadHDRes.value =
            repository.updateDownloadStatus(userId, skuId, enterpriseId, downloadHd)
    }

    suspend fun setProjectAndSkuData(projectUuid: String, skuUuid: String) {
        projectApp = localRepository.getProject(projectUuid)
        skuApp = localRepository.getSkuById(skuUuid)
    }

    fun getRecentBg() = localRepository.getRecentBg(skuApp?.categoryId!!)

    suspend fun updateBackground(map: HashMap<String, Any>) {
        localRepository.updateBackground(HashMap<String, Any>()
            .apply {
                skuApp?.projectUuid?.let { put("project_uuid", it) }
                skuApp?.uuid?.let { put("sku_uuid", it) }
                backgroundAppSelect?.imageId?.let { put("bg_id", it) }
                if (category?.fetchMarketplace == true)
                    marketplaceSelect?.marketPlace_id?.let { put("marketplace_id", it) }
                backgroundAppSelect?.bgName?.let { put("bg_name", it) }
                put("total_frames", getTotalFrames())
                put("process_data", map)
                put("status", "ongoing")
            }
        )
    }

    private fun getTotalFrames(): Int {
        return if (fromVideo) skuApp?.threeSixtyFrames?.plus(skuApp?.imagesCount!!)!! else skuApp?.imagesCount!!
    }

    fun getExteriorImages() = localRepository.getExteriorImages(skuApp?.uuid!!)


    val categoryFetched: MutableLiveData<Boolean> = MutableLiveData()

    fun setCategoryDeatils(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            category = localRepository.getCategoryById(categoryId)

            viewModelScope.launch(Dispatchers.Main) {
                categoryFetched.value = true
            }
        }
    }

    fun getImageUrl() {

        viewModelScope.launch {
            var splittedUrl: String? = null  // Initialize as nullable

            withContext(Dispatchers.IO) {
                //get image from local DB
                skuApp?.let {
                    Log.d(TAG, "getImageUrl: ${it.uuid}")
                    val image =
                        localRepository.imageDaoApp?.getImageSingleImageByUuid(it.uuid)

                    Log.d(TAG, "getImageUrl: ${image?.objectToString()}")
                    Log.d(TAG, "presigned${image?.preSignedUrl}")
                    // presigned and split

                    image?.let {
                        if (image.preSignedUrl == AppConstants.DEFAULT_PRESIGNED_URL){
                            withContext(Dispatchers.Main) {
                                splittedUrl?.let {
                                    foodoutputfragment.value = image.input_image_hres_url  // Use the nullable variable here
                                }
                            }
                        }else {
                            val rawImageUrl = image.preSignedUrl
                            val splittedRawImageUrl = rawImageUrl.split("?")

                            if (splittedRawImageUrl.size > 1) {
                                splittedUrl =
                                    splittedRawImageUrl[0]  // Assign the value to the nullable variable

                                withContext(Dispatchers.Main) {
                                    splittedUrl?.let {
                                        foodoutputfragment.value = it  // Use the nullable variable here
                                    }
                                }
                            }
                        }


                    }
                }
            }


        }


    }

    fun updateSDResponse() {
        //_stableDiffusionResponse.value = null
    }
}