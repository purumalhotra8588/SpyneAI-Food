package com.spyneai.carinspectionocr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.needs.AppConstants
import com.spyneai.shootapp.data.AppShootLocalRepository
import com.spyneai.shootapp.data.model.CarsBackgroundRes
import com.spyneai.shootapp.repository.model.project.CreateProjectAndSkuRes
import com.spyneai.shootapp.repository.model.project.ProjectBody
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import okhttp3.RequestBody

class RegistrationDataViewModel : ViewModel() {

    var skuName: String = ""
    private val appDatabase = SpyneAppDatabase.getInstance(BaseApplication.getContext())
    private val localRepository = AppShootLocalRepository(
        appDatabase.shootDao(),
        appDatabase.shootDao(),
        appDatabase.projectDao(),
        appDatabase.skuDao(),
        categoryDataAppDao = appDatabase.categoryDataDao()
    )

    var category: CatAgnosticResV2.CategoryAgnos? = null
    val categoryFetched: MutableLiveData<Boolean> = MutableLiveData()


    var subcategoryV2: CatAgnosticResV2.CategoryAgnos.SubCategoryV2? = null
    var selectedAngle: Int = 0
    var is360ShootStarted: Boolean = false

    var backgorund: CarsBackgroundRes.BackgroundApp? = null
    var numberPlate: CatAgnosticResV2.CategoryAgnos.NoPlate? = null

    val enableMultiWalls: MutableLiveData<Boolean> = MutableLiveData()

    var processDataMap = HashMap<String, Any>()

    val _singleWallBgList: MutableLiveData<List<CarsBackgroundRes.BackgroundApp>> =
        MutableLiveData()
    val singleWallBgList: LiveData<List<CarsBackgroundRes.BackgroundApp>>
        get() = _singleWallBgList

    val _multiWallBgList: MutableLiveData<List<CarsBackgroundRes.BackgroundApp>> =
        MutableLiveData()
    val multiWallBgList: LiveData<List<CarsBackgroundRes.BackgroundApp>>
        get() = _multiWallBgList

    var odoClicked :MutableLiveData<Boolean> = MutableLiveData()
    var selectBackground :MutableLiveData<Boolean> = MutableLiveData()
    var mfgYearList : List<String> = listOf()
    var projectId = ""
    var fileUrl = ""
    var skuId = ""
    var photoPath = ""
    var resultCode: Int? = null
    var shootActivity : MutableLiveData<Boolean> = MutableLiveData()
    var cameraOpenafterHint : MutableLiveData<Boolean> = MutableLiveData()
    var okayButton : MutableLiveData<Boolean> = MutableLiveData()
    val isProjectCreated: MutableLiveData<Boolean> = MutableLiveData()
    var mfgMonthList : List<String> = listOf()
    var colorList : List<String> = listOf()
    var modelList : List<String> = listOf()
    var model : String = String()
    var nameOfOwner : String = String()
    var chasis : String = String()
    var odometer : String = String()
    var engine : String = String()
    var confirmRegisterClicked :MutableLiveData<Boolean> = MutableLiveData()
    var startShootClicked :MutableLiveData<Boolean> = MutableLiveData()
    var showCameraPreview :MutableLiveData<Boolean> = MutableLiveData()
//    var showCameraPreview by remember { mutableStateOf(true) }



    fun setCategoryDeatils() {
        viewModelScope.launch(Dispatchers.IO) {
            category = localRepository.getCategoryById(AppConstants.CARS_CATEGORY_ID)
            viewModelScope.launch(Dispatchers.Main) {
                categoryFetched.value = true
            }
        }
    }



    private val _createProject: MutableLiveData<Resource<CreateProjectAndSkuRes>> =
        MutableLiveData()
    val createProject: LiveData<Resource<CreateProjectAndSkuRes>>
        get() = _createProject



    fun getSubcategoriesV2(categoryId: String = AppConstants.CARS_CATEGORY_ID) =
        localRepository.getSubcategoriesV2(categoryId = categoryId)

    fun getShootExperience(categoryId: String = AppConstants.CARS_CATEGORY_ID) =
        localRepository.getShootExperience(categoryId = categoryId)

    fun setBackgropund() {
        category?.backgroundApps?.let {
            val singleList = it.filter { it.backgroundType == "SINGLEWALL" }
            val multiList = it.filter { it.backgroundType == "MULTIWALL" }

            _singleWallBgList.value = singleList
            _multiWallBgList.value = multiList

            if (!_multiWallBgList.value.isNullOrEmpty())
                enableMultiWalls.value = true
        }
    }

}