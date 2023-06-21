package com.spyneai.threesixty.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.camera2.ShootDimensions
import com.spyneai.credits.model.DownloadHDRes
import com.spyneai.credits.model.ReduceCreditResponse
import com.spyneai.dashboard.repository.model.AngleClassifierRes
import com.spyneai.isInternetActive
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.shootapp.repository.model.project.Project
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.shootapp.data.AppShootLocalRepository
import com.spyneai.shootapp.data.ShootRepository
import com.spyneai.shootapp.data.model.CarsBackgroundRes
import com.spyneai.shootapp.data.model.CreateProjectRes
import com.spyneai.shootapp.data.model.CreateSkuRes
import com.spyneai.threesixty.data.response.ProcessThreeSixtyRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ThreeSixtyViewModel : ViewModel() {

    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var errorHeading: MutableLiveData<String> = MutableLiveData()
    var isCameraButtonClickAble: MutableLiveData<Boolean> = MutableLiveData()
    var showBackgroundFragment: MutableLiveData<Boolean> = MutableLiveData()

    var backgroundAppSelect: CarsBackgroundRes.BackgroundApp? = null

    var isVideoBackgroundFragmentActive = false

    var processDataMap = HashMap<String, Any>()

    val threeSixtyStartTimer: MutableLiveData<Boolean> = MutableLiveData()


    private val repository = ShootRepository()
    private val threeSixtyRepository = ThreeSixtyRepository()
    private val db = SpyneAppDatabase.getInstance(BaseApplication.getContext())
    private val sdkDb = SpyneAppDatabase.getInstance(BaseApplication.getContext())
    private val localRepository = AppShootLocalRepository(db.shootDao(),  sdkDb.shootDao(), sdkDb.projectDao(), sdkDb.skuDao())
    var desiredAngle: Int = 0



    var skuApp: Sku? = null
    var projectApp: Project? = null

    var fromDrafts = false
    var fromVideo = false
    val isDemoClicked: MutableLiveData<Boolean> = MutableLiveData()
    val isFramesUpdated: MutableLiveData<Boolean> = MutableLiveData()
    val title: MutableLiveData<String> = MutableLiveData()
    var processingStarted: MutableLiveData<Boolean> = MutableLiveData()

    var tint: Boolean = false


    val isProjectCreated: MutableLiveData<Boolean> = MutableLiveData()

    val enableRecording: MutableLiveData<Boolean> = MutableLiveData()
    val shootDimensions: MutableLiveData<ShootDimensions> = MutableLiveData()

    private val _createProjectRes: MutableLiveData<Resource<CreateProjectRes>> = MutableLiveData()
    val createProjectRes: LiveData<Resource<CreateProjectRes>>
        get() = _createProjectRes

    private val _createSkuRes: MutableLiveData<Resource<CreateSkuRes>> = MutableLiveData()
    val createSkuRes: LiveData<Resource<CreateSkuRes>>
        get() = _createSkuRes

    private val _carGifRes: MutableLiveData<Resource<CarsBackgroundRes>> = MutableLiveData()
    val carGifRes: LiveData<Resource<CarsBackgroundRes>>
        get() = _carGifRes


    private val _process360Res: MutableLiveData<Resource<ProcessThreeSixtyRes>> = MutableLiveData()
    val process360Res: LiveData<Resource<ProcessThreeSixtyRes>>
        get() = _process360Res

    private val _userCreditsRes: MutableLiveData<Resource<CreditDetailsResponse>> =
        MutableLiveData()
    val userCreditsRes: LiveData<Resource<CreditDetailsResponse>>
        get() = _userCreditsRes

    private val _downloadHDRes: MutableLiveData<Resource<DownloadHDRes>> = MutableLiveData()
    val downloadHDRes: LiveData<Resource<DownloadHDRes>>
        get() = _downloadHDRes

    private val _reduceCreditResponse: MutableLiveData<Resource<ReduceCreditResponse>> =
        MutableLiveData()
    val reduceCreditResponse: LiveData<Resource<ReduceCreditResponse>>
        get() = _reduceCreditResponse


    fun getBackgroundGifCars(
        fetchId: String,
        map: HashMap<String, Any>
    ) = viewModelScope.launch {
        _carGifRes.value = Resource.Loading

        GlobalScope.launch(Dispatchers.IO) {
            val backgroundList = localRepository.getBackgrounds(fetchId)

            val marketplaceList = localRepository.getMarketplaceByCatId(fetchId)

//            val checkList = if(!category!!.fetchMarketplace)
//                backgroundList
//            else
//                marketplaceList


            if (!backgroundList.isNullOrEmpty() && !BaseApplication.getContext()
                    .isInternetActive()
            ) {
                val finalList =
//                    if (category?.fetchBackgroundsBy == "prodCatId")
                    backgroundList
//                else
//                    backgroundList?.filter {
//                        it.prodSubCatId == fetchId
//                    }

                val finalMarketList =
//                    if (category?.fetchMarketplaceBy == "prodCatId")
                    marketplaceList
//                else
//                    marketplaceList?.filter {
//                        it.prod_sub_cat_id == sku?.subcategoryId
//                    }
                GlobalScope.launch(Dispatchers.Main) {
                    _carGifRes.value = Resource.Success(
                        CarsBackgroundRes(finalList, finalMarketList)
                    )
                }
            } else {
                val response = repository.getBackgroundGifCars(map)
                if (response is Resource.Success) {
                    //insert overlays
                    val finalBgList = ArrayList<CarsBackgroundRes.BackgroundApp>()
                    val bgList = response.value.data
                    val marketplaceList = response.value.marketPlace

                    val finalMarketList =
//                        if (category?.fetchMarketplaceBy == "prodCatId")
                        marketplaceList
//                    else
//                        marketplaceList?.filter {
//                            it.prod_sub_cat_id == sku?.subcategoryId
//                        }

                    bgList.forEach {
                        if (it.bgName != null && it.imageUrl != null) {
                            it.categoryId = fetchId
                            it.prodCatId = fetchId
                            it.prodSubCatId = fetchId

                            finalBgList.add(it)
                        }

                    }

                    localRepository.insertBackgrounds(finalBgList)
                    localRepository.insertMarketplace(finalMarketList)

                    val s = ""

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




    fun getUserCredits(
        userId: String
    ) = viewModelScope.launch {
        _userCreditsRes.value = Resource.Loading
        _userCreditsRes.value = threeSixtyRepository.getUserCredits(userId)
    }

    fun reduceCredit(
        userId: String,
        creditReduce: String,
        skuId: String
    ) = viewModelScope.launch {
        _reduceCreditResponse.value = Resource.Loading
        _reduceCreditResponse.value = threeSixtyRepository.reduceCredit(userId, creditReduce, skuId)
    }

    fun updateDownloadStatus(
        userId: String,
        skuId: String,
        enterpriseId: String,
        downloadHd: Boolean
    ) = viewModelScope.launch {
        _downloadHDRes.value = Resource.Loading
        _downloadHDRes.value =
            threeSixtyRepository.updateDownloadStatus(userId, skuId, enterpriseId, downloadHd)
    }

    fun insertProject() {
        localRepository.insertProject(projectApp!!)
    }










    suspend fun setProjectAndSkuData(projectUuid: String, skuUuid: String) {
        projectApp = localRepository.getProject(projectUuid)
        skuApp = localRepository.getSkuById(skuUuid)
    }



    private fun getTotalFrames(): Int {
        return skuApp?.threeSixtyFrames?.plus(skuApp?.imagesCount!!)!!
    }



    val _angleClassifierRes: MutableLiveData<Resource<AngleClassifierRes>> = MutableLiveData()
    val angleClassifierRes: LiveData<Resource<AngleClassifierRes>>
        get() = _angleClassifierRes


    fun angleClassifier(
        imageFile : MultipartBody.Part,
        requiredAngle:Int,
        cropCheck: Boolean
    ) = viewModelScope.launch {
        _angleClassifierRes.value = Resource.Loading
        _angleClassifierRes.value = repository.angleClassifier(imageFile, requiredAngle, cropCheck)
    }
}