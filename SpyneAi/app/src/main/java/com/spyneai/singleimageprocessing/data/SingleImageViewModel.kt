package com.spyneai.singleimageprocessing.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.camera2.ShootDimensions
import com.spyneai.dashboard.repository.model.GetGCPUrlRes
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.data.SampleImagesRes
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.shootapp.data.AppShootLocalRepository
import com.spyneai.shootapp.data.ShootRepository
import com.spyneai.shootapp.data.model.CarsBackgroundRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class SingleImageViewModel : ViewModel() {

    var uploadData: GetGCPUrlRes.Data? = null
    var outputUrl = ""
    val imageProcessed: MutableLiveData<Boolean> = MutableLiveData()
    private val repository = ShootRepository()
    private val spyneAppDatabase = SpyneAppDatabase.getInstance(BaseApplication.getContext())
    private val localRepository = AppShootLocalRepository(
        spyneAppDatabase.shootDao(),
        spyneAppDatabase.shootDao(),
        spyneAppDatabase.projectDao(),
        spyneAppDatabase.skuDao(),
        spyneAppDatabase.categoryDataDao()
    )

    var isCameraButtonClickable = true

    var subcategoryV2: CatAgnosticResV2.CategoryAgnos.SubCategoryV2? = null
    val showLeveler: MutableLiveData<Boolean> = MutableLiveData()
    var desiredAngle : Int = 0

    private var _overlaysResponse: MutableLiveData<Resource<OverlaysResponse>> = MutableLiveData()
    val overlaysResponse: LiveData<Resource<OverlaysResponse>>
        get() = _overlaysResponse

    private var _sampleImagesRes: MutableLiveData<Resource<SampleImagesRes>> = MutableLiveData()
    val sampleImagesRes: LiveData<Resource<SampleImagesRes>>
        get() = _sampleImagesRes

    var _gcpUrlResponse: MutableLiveData<Resource<GetGCPUrlRes>> = MutableLiveData()
    val gcpUrlResponse: LiveData<Resource<GetGCPUrlRes>>
        get() = _gcpUrlResponse

    var _processSingleImage: MutableLiveData<Resource<SingleImageProcessRes>> = MutableLiveData()
    val processSingleImage: LiveData<Resource<SingleImageProcessRes>>
        get() = _processSingleImage



    fun getPreSignedUrl(
        imageName: String
    ) = viewModelScope.launch {
        val map = HashMap<String,Any?>().apply {
            put("image_name",imageName)
            put("project_details",true)
            put("prod_cat_id",Utilities.getPreference(BaseApplication.getContext(),AppConstants.SELECTED_CATEGORY_ID).toString())
            put("source","App_android_single")
        }

        subcategoryV2?.let {
            map.put("prod_sub_cat_id",it.prodSubCatId)
        }

        _gcpUrlResponse.value = Resource.Loading
        _gcpUrlResponse.value = repository.getPreSignedUrl(
            map
        )
    }

    fun processImage(
        map: HashMap<String,Any>
    ) = viewModelScope.launch {
        _processSingleImage.value = Resource.Loading
        _processSingleImage.value = repository.processSingleImage(map)
    }



    fun getSampleImages(
    ) = viewModelScope.launch {
        _overlaysResponse.value = Resource.Loading
        _sampleImagesRes.value = repository.getSampleImages()
    }

    fun getSubcategoriesV2(categoryId: String) =
        localRepository.getSubcategoriesV2(categoryId = categoryId)

    fun getOverlays(
        authKey: String, prodId: String,
        prodSubcategoryId: String, frames: String
    ) = viewModelScope.launch {
        _overlaysResponse.value = Resource.Loading
        _overlaysResponse.value = repository.getOverlays(authKey, prodId, prodSubcategoryId, frames)
    }

    fun setCategoryDeatils(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            category = localRepository.getCategoryById(categoryId)

            viewModelScope.launch(Dispatchers.Main) {
                categoryFetched.value = true
            }
        }
    }

    val shootDimensions: MutableLiveData<ShootDimensions> = MutableLiveData()

    val categoryFetched: MutableLiveData<Boolean> = MutableLiveData()
    var category: CatAgnosticResV2.CategoryAgnos? = null

    var displayName = ""
    var displayThumbanil = ""

    fun getOrientation() = localRepository.getCategoryOrientation(
        com.spyneai.needs.Utilities.getPreference(
            BaseApplication.getContext(),
            AppConstants.SELECTED_CATEGORY_ID
        ).toString()
    )

    suspend fun uploadImageToGcp(
        gcpUrl: String,
        asRequestBody: RequestBody
    ) = repository.uploadImageToGcp(
        gcpUrl,
        asRequestBody,
        "image/jpeg"
    )

    fun getBackgrounds(): List<CarsBackgroundRes.BackgroundApp>? {
        if (category?.fetchBackgroundsBy == "prodCatId")
            return category?.backgroundApps
        else
            return category?.backgroundApps?.filter {
                it.prodSubCatId == subcategoryV2?.prodSubCatId
            }
    }

    fun getBackgroundsHome(): List<CarsBackgroundRes.BackgroundApp>? {

            return category?.backgroundApps

    }
}