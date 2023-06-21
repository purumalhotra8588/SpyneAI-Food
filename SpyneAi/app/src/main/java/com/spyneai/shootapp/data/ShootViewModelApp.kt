package com.spyneai.shootapp.data


import InvitationEmailBody
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.camera2.ShootDimensions
import com.spyneai.dashboard.repository.model.AngleClassifierRes
import com.spyneai.dashboard.repository.model.AngleClassifierResponseV2
import com.spyneai.dashboard.repository.model.GetGCPUrlRes
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.getUuid
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.captureEvent
import com.spyneai.reshoot.data.ReshootOverlaysRes

import com.spyneai.shootapp.data.model.*
import com.spyneai.shootapp.repository.model.image.Image
import com.spyneai.shootapp.repository.model.payment.*
import com.spyneai.shootapp.repository.model.project.Project
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.shootapp.response.InvitationEmailIdRes
import com.spyneai.shootapp.workmanager.OverlaysPreloadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

class ShootViewModelApp : ViewModel() {

    val paymentStatus: MutableLiveData<PaymentStatus> = MutableLiveData()

    private val TAG = "ShootViewModel"
    private val repository = ShootRepository()
    private val spyneAppDatabase = SpyneAppDatabase.getInstance(BaseApplication.getContext())
    private val localRepository = AppShootLocalRepository(
        spyneAppDatabase.shootDao(),
        spyneAppDatabase.shootDao(),
        spyneAppDatabase.projectDao(),
        spyneAppDatabase.skuDao(),
        categoryDataAppDao = spyneAppDatabase.categoryDataDao()
    )

    // private val imageRepository = ImageLocalRepository()
    private val imageRepositoryV2 = ImagesRepoV2(spyneAppDatabase.imageDao())

    val removeSubcatFragment = MutableLiveData<Boolean>()

    val showHint: MutableLiveData<Boolean> = MutableLiveData()
    val hideChangeSubcat: MutableLiveData<Boolean> = MutableLiveData()

    val addedToCart: MutableLiveData<Boolean> = MutableLiveData()


    val projectRemoved: MutableLiveData<Boolean> = MutableLiveData()

    var isCameraButtonClickable = true
    var processSku: Boolean = true
    var isStopCaptureClickable = false

    var projectCount = 0
    var skuCount = 0


    var fileUrl = ""
    var preSignedUrl = ""

    var totalAmount = 0.0
    var hasEcomOverlay = false
    var finalAmount = 0.0
    var FROM_VIDEO = false
    var discountRupee = 0.0
    var additionalCreditRequired = 0
    var proceedPayment: MutableLiveData<Boolean> = MutableLiveData()
    var replaceFragment: MutableLiveData<Boolean> = MutableLiveData()


    var threeSixtyInteriorSelected = false
    var onVolumeKeyPressed: MutableLiveData<Boolean> = MutableLiveData()
    var pointAngle: MutableLiveData<Boolean> = MutableLiveData()
    var fromDrafts = false
    var fromVideo = false
    val isSensorAvailable: MutableLiveData<Boolean> = MutableLiveData()
    val isSkuNameAdded: MutableLiveData<Boolean> = MutableLiveData()
    val isProjectNameEdited: MutableLiveData<Boolean> = MutableLiveData()

    var showDialog = true
    var miscDialogShowed = false

    val enableCameraButton: MutableLiveData<Boolean> = MutableLiveData()
    val showSubCat: MutableLiveData<Boolean> = MutableLiveData()

    val skuNumber: MutableLiveData<Int> = MutableLiveData()
    var frameAngle = ""

    val location_data: MutableLiveData<JSONObject> = MutableLiveData()

    val isSubCategorySelected: MutableLiveData<Boolean> = MutableLiveData()

    val categoryPosition: MutableLiveData<Int> = MutableLiveData()

    var dafault_project: MutableLiveData<String> = MutableLiveData()
    var dafault_sku: MutableLiveData<String> = MutableLiveData()

    val isSubCatAngleConfirmed: MutableLiveData<Boolean> = MutableLiveData()

    val startInteriorShoot: MutableLiveData<Boolean> = MutableLiveData()
    val begin: MutableLiveData<Long> = MutableLiveData()
    val end: MutableLiveData<Long> = MutableLiveData()

    val totalSkuCaptured: MutableLiveData<String> = MutableLiveData()
    val totalImageCaptured: MutableLiveData<Int> = MutableLiveData()
    val show360InteriorDialog: MutableLiveData<Boolean> = MutableLiveData()
    val interior360Dialog: MutableLiveData<Boolean> = MutableLiveData()
    val refreshOutput: MutableLiveData<Boolean> = MutableLiveData()

    val iniProgressFrame: MutableLiveData<Boolean> = MutableLiveData()
    val subCatName: MutableLiveData<String> = MutableLiveData()

    val shootList: MutableLiveData<ArrayList<ShootData>> = MutableLiveData()

    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> =
        MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse



    var _gcpUrlResponse: MutableLiveData<Resource<GetGCPUrlRes>> = MutableLiveData()
    val gcpUrlResponse: LiveData<Resource<GetGCPUrlRes>>
        get() = _gcpUrlResponse

    var _inviteEmailRes: MutableLiveData<Resource<InvitationEmailIdRes>> = MutableLiveData()
    val inviteEmailRes: LiveData<Resource<InvitationEmailIdRes>>
        get() = _inviteEmailRes

    val _classificationRes: MutableLiveData<Resource<AngleClassifierResponseV2>> = MutableLiveData()
    val classificationRes: LiveData<Resource<AngleClassifierResponseV2>>
        get() = _classificationRes


    private var _overlaysResponse: MutableLiveData<Resource<OverlaysResponse>> = MutableLiveData()
    val overlaysResponse: LiveData<Resource<OverlaysResponse>>
        get() = _overlaysResponse


    private val _updateFootwearSubcatRes: MutableLiveData<Resource<UpdateFootwearSubcatRes>> =
        MutableLiveData()
    val updateFootwearSubcatRes: LiveData<Resource<UpdateFootwearSubcatRes>>
        get() = _updateFootwearSubcatRes


    private val _improveShootResponse: MutableLiveData<Resource<ImproveShootResponse>> =
        MutableLiveData()
    val improveShootResponse: LiveData<Resource<ImproveShootResponse>>
        get() = _improveShootResponse

    var _improveShootList: MutableLiveData<List<ImproveShootResponse.Guidelines>> = MutableLiveData()
    val improveShootList: LiveData<List<ImproveShootResponse.Guidelines>>
        get() = _improveShootList

    val _angleClassifierRes: MutableLiveData<Resource<AngleClassifierRes>> = MutableLiveData()
    val angleClassifierRes: LiveData<Resource<AngleClassifierRes>>
        get() = _angleClassifierRes

    val shootDimensions: MutableLiveData<ShootDimensions> = MutableLiveData()

    // val sku: MutableLiveData<Sku> = MutableLiveData()
    var skuApp: Sku? = null
    var projectApp: Project? = null

    //var subCategory: MutableLiveData<NewSubCatResponse.Subcategory> = MutableLiveData()
    var categoryDetails: MutableLiveData<CategoryDetails> = MutableLiveData()
    val isSubCategoryConfirmed: MutableLiveData<Boolean> = MutableLiveData()
    val removeImproveShootFragment : MutableLiveData<Boolean> = MutableLiveData()
    val showVin: MutableLiveData<Boolean> = MutableLiveData()
    val isProjectCreated: MutableLiveData<Boolean> = MutableLiveData()
    val isProjectCreatedEcom: MutableLiveData<Boolean> = MutableLiveData()
    val isSkuCreated: MutableLiveData<Boolean> = MutableLiveData()
    val showLeveler: MutableLiveData<Boolean> = MutableLiveData()
    val showOverlay: MutableLiveData<Boolean> = MutableLiveData()
    val showGrid: MutableLiveData<Boolean> = MutableLiveData()
    var isHintShowen: MutableLiveData<Boolean> = MutableLiveData()

    val subCategoryId: MutableLiveData<String> = MutableLiveData()
    val exterirorAngles: MutableLiveData<Int> = MutableLiveData()
    var desiredAngle: Int = 0

    var currentShoot = 0
    var allExteriorClicked = false
    var allEcomOverlyasClicked = false
    var allInteriorClicked = false
    var allMisc = false
    var allReshootClicked = false

    val shootData: MutableLiveData<ShootData> = MutableLiveData()
    val reshootCompleted: MutableLiveData<Boolean> = MutableLiveData()

    val showConfirmReshootDialog: MutableLiveData<Boolean> = MutableLiveData()
    val showCropDialog: MutableLiveData<Boolean> = MutableLiveData()

    //interior and misc shots
    val showInteriorDialog: MutableLiveData<Boolean> = MutableLiveData()
    val startInteriorShots: MutableLiveData<Boolean> = MutableLiveData()
    val hideLeveler: MutableLiveData<Boolean> = MutableLiveData()
    val showMiscDialog: MutableLiveData<Boolean> = MutableLiveData()
    val startMiscShots: MutableLiveData<Boolean> = MutableLiveData()
    val selectBackground: MutableLiveData<Boolean> = MutableLiveData()
    val stopShoot: MutableLiveData<Boolean> = MutableLiveData()
    val stopFoodShoot: MutableLiveData<Boolean> = MutableLiveData()
    val showProjectDetail: MutableLiveData<Boolean> = MutableLiveData()

    val imageTypeInfo: MutableLiveData<Boolean> = MutableLiveData()

    val interiorAngles: MutableLiveData<Int> = MutableLiveData()
    val miscAngles: MutableLiveData<Int> = MutableLiveData()

    val reshootCapturedImage: MutableLiveData<Boolean> = MutableLiveData()


    // val confirmCapturedImage: MutableLiveData<Boolean> = MutableLiveData()
    val projectId: MutableLiveData<String> = MutableLiveData()

    val addMoreAngle: MutableLiveData<Boolean> = MutableLiveData()
    var isReshoot = false
    var isReclick = false
    var reclickSequence = 0
    var reshotImageName = ""
    var reshootSequence = 0
    var updateSelectItem: MutableLiveData<Boolean> = MutableLiveData()

    private var _selectedImage: MutableLiveData<Image> =
        MutableLiveData()
    val selectedImage: LiveData<Image>
        get() = _selectedImage



    fun getImagesById(skuId: String, overlayId: String) =
        imageRepositoryV2.getImageForComment(skuId, overlayId)


    private val _reshootOverlaysRes: MutableLiveData<Resource<ReshootOverlaysRes>> =
        MutableLiveData()
    val reshootOverlaysRes: LiveData<Resource<ReshootOverlaysRes>>
        get() = _reshootOverlaysRes


    suspend fun getSkuWithProjectUuid() = projectApp?.uuid?.let {
        spyneAppDatabase.skuDao().getSkuListWithProjectUuid(
            it
        )
    }

    suspend fun getPreviewSkuWithProjectUuid(uuid: String) =
        spyneAppDatabase.skuDao().getSkuListWithProjectUuid(uuid)







    fun deleteCategoryData() {
        spyneAppDatabase.categoryDataDao().deleteCatData()
    }

    fun getCartProjectsData(projectIds: List<String>) =
        spyneAppDatabase.projectDao().getCartProjectsData(projectIds)

    fun updateProjectOrderId(orderId: String, projectsUuids: List<String>) =
        spyneAppDatabase.projectDao()
            .updateProjectOrderId(orderId = orderId, projectUuids = projectsUuids)

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

    var subcategoryV2: CatAgnosticResV2.CategoryAgnos.SubCategoryV2? = null

    fun getSubcategoriesV2(categoryId: String = category?.categoryId.toString()) =
        localRepository.getSubcategoriesV2(categoryId = categoryId)

    fun getOverlays(
        authKey: String, prodId: String,
        prodSubcategoryId: String, frames: String
    ) = viewModelScope.launch {
        _overlaysResponse.value = Resource.Loading

        GlobalScope.launch(Dispatchers.IO) {
            val overlaysList = localRepository.getOverlays(prodSubcategoryId, frames)

            if (!overlaysList.isNullOrEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    _overlaysResponse.value = Resource.Success(
                        OverlaysResponse(
                            overlaysList,
                            "Overlyas fetched successfully",
                            200
                        )
                    )
                }
            } else {
                val response = repository.getOverlays(authKey, prodId, prodSubcategoryId, frames)

                if (response is Resource.Success) {
                    //insert overlays
                    val overlaysList = response.value.data

                    overlaysList.forEach {
                        it.fetchAngle = frames.toInt()
                    }

                    localRepository.insertOverlays(overlaysList)

                    GlobalScope.launch(Dispatchers.Main) {
                        _overlaysResponse.value = response
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        _overlaysResponse.value = response
                    }
                }
            }
        }
    }

    fun getGCPUrl(
        imageName: String,
    ) = viewModelScope.launch {
        _gcpUrlResponse.value = Resource.Loading
        _gcpUrlResponse.value = repository.getGCPUrl(imageName)
    }


    fun getInteriorList() = localRepository.getInteriorList(subcategoryV2?.prodCatId!!)

    fun getMiscList() = localRepository.getMiscList(subcategoryV2?.prodCatId!!)

    public val tags = HashMap<String, Any>()

    suspend fun getTags(type: String): Any? {
        when (type) {
            "Exterior" -> {
                val extags = tags[type]
                if (extags == null) {
                    val exTags = localRepository.getExteriorTags()
                    tags[type] = exTags
                    return tags[type]
                } else {
                    return extags
                }
            }

            "Interior" -> {
                val extags = tags[type]
                if (extags == null) {
                    val exTags = localRepository.getInteriorTags()
                    tags[type] = exTags
                    return tags[type]
                } else {
                    return extags
                }
            }
            else -> {
                val extags = tags[type]
                if (extags == null) {
                    val exTags = localRepository.getFocusTags()
                    tags[type] = exTags
                    return tags[type]
                } else {
                    return extags
                }
            }
        }

    }

    var isAddImage = false


    suspend fun preloadOverlays(overlays: List<String>) {
        //check if preload worker is alive
        val workManager = WorkManager.getInstance(BaseApplication.getContext())

        val workQuery = WorkQuery.Builder
            .fromTags(listOf("Preload Overlays"))
            .addStates(
                listOf(
                    WorkInfo.State.BLOCKED,
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING,
                    WorkInfo.State.CANCELLED
                )
            )
            .build()

        val workInfos = workManager.getWorkInfos(workQuery).await()

        if (workInfos.size > 0) {
            // stop worker
            startPreloadWorker(overlays)
        } else {
            startPreloadWorker(overlays)
        }
    }

    private fun startPreloadWorker(overlays: List<String>) {
        val data = Data.Builder()
            .putStringArray("overlays", overlays.toTypedArray())
            .putInt("position", 0)
            .build()

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val overlayPreloadWorkRequest =
            OneTimeWorkRequest.Builder(OverlaysPreloadWorker::class.java)
                .addTag("Preload Overlays")
                .setConstraints(constraints)
                .setInputData(data)
                .build()

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(overlayPreloadWorkRequest)
    }


    fun getSelectedAngles(appName: String): Int {
        return if (exterirorAngles.value == null) {
            when (appName) {
                AppConstants.CARS24, AppConstants.CARS24_INDIA -> 5
                AppConstants.SELL_ANY_CAR -> 4
                else -> 8
            }
        } else {
            exterirorAngles.value!!
        }
    }


    fun getAllProjectsSize() = localRepository.getAllProjectsSize()
    fun getSkusCountByProjectUuid(uuid: String) = localRepository.getSkusCountByProjectUuid(uuid)

    suspend fun isImageExist(skuUuid: String, overlayId: String, sequence: Int) =
        imageRepositoryV2.isImageExist(skuUuid, overlayId, sequence)


    suspend fun insertImage(shootData: ShootData) {

        BaseApplication.getContext().captureEvent(
            "Insert Image call",
            HashMap<String, Any?>().apply {
                put("sku_id", shootData.sku_id)
                put("data", Gson().toJson(shootData))
                put("current_shoot", currentShoot)
                put("overlay_id", overlayId)
            }
        )

        val name = if (shootData.image_category == "360int")
            skuApp?.skuName?.uppercase() + "_" + skuApp?.uuid + "_360int_1.JPG"
        else {
            if (shootData.name.contains(".")) shootData.name else shootData.name + "." + shootData.capturedImage.substringAfter(
                "."
            )
        }

        Log.d(TAG, "insertImage: $name")

        var imageApp: Image? = null

        skuApp?.uuid?.let {
            imageApp = isImageExist(it, shootData.overlayId.toString(), shootData.sequence)
        }

        if (imageApp == null) {
            val newImage = Image(
                uuid = getUuid(),
                projectUuid = projectApp?.uuid,
                skuUuid = skuApp?.uuid,
                image_category = shootData.image_category,
                skuName = skuApp?.skuName,
                name = name,
                sequence = shootData.sequence,
                overlayId = shootData.overlayId.toString(),
                isReclick = isReclick,
                isReshoot = isReshoot,
                path = shootData.capturedImage,
                angle = shootData.angle,
                tags = shootData.meta,
                debugData = shootData.debugData,
                isExtraImage = isAddImage
            )

            localRepository.insertImage(
                newImage
            )

            BaseApplication.getContext().captureEvent(
                "Image Inserted",
                HashMap<String, Any?>().apply {
                    put("sku_id", shootData.sku_id)
                    put("data", Gson().toJson(shootData))
                    put("current_shoot", currentShoot)
                    put("overlay_id", overlayId)
                }
            )
        } else {
            imageApp?.let { nonNulImage ->
                nonNulImage.path = shootData.capturedImage
                nonNulImage.toProcessAT = System.currentTimeMillis()
                nonNulImage.isUploaded = false
                nonNulImage.isMarkedDone = false
                nonNulImage.isReclick = isReclick
                nonNulImage.isReshoot = isReshoot
                nonNulImage.preSignedUrl = AppConstants.DEFAULT_PRESIGNED_URL

                imageRepositoryV2.updateImage(nonNulImage)

                BaseApplication.getContext().captureEvent(
                    "Image Updated",
                    HashMap<String, Any?>().apply {
                        put("sku_id", shootData.sku_id)
                        put("data", Gson().toJson(shootData))
                        put("current_shoot", currentShoot)
                        put("overlay_id", overlayId)
                    }
                )
            }
        }

        Utilities.savePrefrence(BaseApplication.getContext(),AppConstants.CAPTURED_IMAGE,shootData.capturedImage)


        isReclick = false
    }


    suspend fun insertSku() {
        projectApp?.isCreated = false
        localRepository.insertSku(skuApp!!, projectApp!!)
    }


    suspend fun insertProject(): Long {
        projectApp?.entityId =
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.ENTITY_ID)
        return localRepository.insertProject(projectApp!!)
    }

    suspend fun updateSubcategory() {
//        sku?.isSelectAble = true
        skuApp?.let {
            localRepository.updateSubcategory(it)
        }

    }

    suspend fun createProjectSync() {
        skuApp?.isSelectAble = true
        skuApp?.let { localRepository.updateSubcategory(it) }
    }

    fun getImagesbySkuId(skuId: String) = imageRepositoryV2.getImagesBySkuId(skuId)

    fun updateFootwearSubcategory(
    ) = viewModelScope.launch {
        _updateFootwearSubcatRes.value = Resource.Loading
        _updateFootwearSubcatRes.value = repository.updateFootwearSubcategory(
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
            skuApp?.skuId!!,
            exterirorAngles.value!!,
            subcategoryV2?.prodSubCatId!!
        )
    }

    fun updateVideoSkuLocally() {
        localRepository.updateVideoSkuLocally(skuApp!!)
    }


    fun getFileName(
        interiorSize: Int?,
        miscSize: Int?,
    ): String {
        return if (isReshoot) {
            reshotImageName
        } else {
            val filePrefix = FileNameManager().getFileName(
                if (categoryDetails.value?.imageType == "Misc") "Focus Shoot" else categoryDetails.value?.imageType!!,
                currentShoot,
                shootList.value,
                interiorSize,
                miscSize
            )

            skuApp?.skuName?.uppercase() + "_" + skuApp?.uuid + "_" + filePrefix
        }
    }

    fun getSequenceNumber(exteriorSize: Int, interiorSize: Int, miscSize: Int): Int {
        return if (isReshoot)
            reshootSequence
        else SequeneNumberManager().getSequenceNumber(
            fromDrafts,
            if (categoryDetails.value?.imageType == "Misc") "Focus Shoot" else categoryDetails.value?.imageType!!,
            currentShoot,
            shootList.value?.size!!,
            exteriorSize,
            interiorSize,
            miscSize
        )
    }

    fun getOnImageConfirmed(): Boolean {
        return if (onImageConfirmed.value == null) true
        else !onImageConfirmed.value!!
    }

    fun getOverlay(): String {
        return displayThumbanil
//        val overlayRes = (overlaysResponse.value as Resource.Success).value
//        return overlayRes.data[overlayRes.data.indexOf(selectedOverlay)].display_thumbnail
    }

    fun getName(): String {
//        val overlayRes = (overlaysResponse.value as Resource.Success).value
//        return overlayRes.data[overlayRes.data.indexOf(selectedOverlay)].display_name
        return displayName
    }


    var displayName = ""
    var displayThumbanil = ""

    //var sequence = 0
    var overlayId = 0

    // var selectedOverlay : OverlaysResponse.Data? = null
    val getSubCategories = MutableLiveData<Boolean>()
    var isSubcategoriesSelectionShown = false
    val selectAngles = MutableLiveData<Boolean>()

    val onImageConfirmed = MutableLiveData<Boolean>()

    fun getCurrentShoot() = shootList.value?.firstOrNull() {
        it.overlayId == overlayId
    }

    fun checkMiscShootStatus(appName: String) {

        GlobalScope.launch(Dispatchers.IO) {
            val MiscList = category?.miscellaneous
            if (!MiscList.isNullOrEmpty()) {

                GlobalScope.launch(Dispatchers.Main) {
                    showMiscDialog.value = true
                }
                return@launch
            }

            GlobalScope.launch(Dispatchers.Main) {
                selectBackground(appName)
            }
        }
    }

    fun selectBackground(appName: String) {
        if (appName == AppConstants.OLA_CABS || appName == "Moladin Agent" || appName == "Caricarz")
            if (isAddImage)
                selectBackground.value = true
            else
                show360InteriorDialog.value = true
        else
            selectBackground.value = true
    }

    fun skipImage(appName: String) {
        when (categoryDetails.value?.imageType) {
            "Interior" -> {
                if (isAddImage)
                    selectBackground(appName)
                else
                    checkMiscShootStatus(appName)

            }

            "Focus Shoot" -> {
                selectBackground(appName)
            }
        }
    }

    val notifyItemChanged = MutableLiveData<Int>()
    val scrollView = MutableLiveData<Int>()

    fun setSelectedItem(thumbnails: List<Any>) {
        if (getCurrentShoot() == null) {
            Log.d(TAG, "setSelectedItem: $overlayId")
        } else {
            when (categoryDetails.value?.imageType) {
                "Exterior", "Footwear", "Ecom", "Food" -> {
                    val list =
                        thumbnails as List<CatAgnosticResV2.CategoryAgnos.SubCategoryV2.OverlayApp>

                    val position = currentShoot

                    list[position].isSelected = false
                    list[position].imageClicked = true
                    list[position].imagePath = getCurrentShoot()!!.capturedImage

                    notifyItemChanged.value = position

                    if (position != list.size.minus(1)) {
                        var foundNext = false

                        for (i in position..list.size.minus(1)) {

                            if (!list[i].isSelected && !list[i].imageClicked) {
                                foundNext = true
                                list[i].isSelected = true
                                currentShoot = i

                                notifyItemChanged.value = i
                                scrollView.value = i
                                break
                            }
                        }

                        if (!foundNext) {
                            val element = list.firstOrNull {
                                !it.isSelected && !it.imageClicked
                            }

                            if (element != null) {
                                element?.isSelected = true
                                notifyItemChanged.value = list.indexOf(element)
                                scrollView.value = element?.sequenceNumber!!
                            }
                        }
                    } else {
                        val element = list.firstOrNull {
                            !it.isSelected && !it.imageClicked
                        }

                        if (element != null) {
                            element?.isSelected = true
                            notifyItemChanged.value = list.indexOf(element)
                            scrollView.value = element?.sequenceNumber!!
                        }
                    }
                }

                "Interior" -> {
                    val list = thumbnails as List<CatAgnosticResV2.CategoryAgnos.Interior>

                    val position = currentShoot

                    list[position].isSelected = false
                    list[position].imageClicked = true
                    list[position].imagePath = getCurrentShoot()!!.capturedImage

                    notifyItemChanged.value = position

                    if (position != list.size.minus(1)) {
                        var foundNext = false

                        for (i in position..list.size.minus(1)) {
                            if (!list[i].isSelected && !list[i].imageClicked) {
                                foundNext = true
                                list[i].isSelected = true
                                notifyItemChanged.value = i
                                scrollView.value = i
                                break
                            }
                        }

                        if (!foundNext) {
                            val element = list.firstOrNull {
                                !it.isSelected && !it.imageClicked
                            }

                            if (element != null) {
                                element?.isSelected = true
                                notifyItemChanged.value = list.indexOf(element)
                                scrollView.value = element?.sequenceNumber!!
                            }
                        }
                    } else {
                        val element = list.firstOrNull {
                            !it.isSelected && !it.imageClicked
                        }

                        if (element != null) {
                            element?.isSelected = true
                            notifyItemChanged.value = list.indexOf(element)
                            scrollView.value = element?.sequenceNumber!!
                        }
                    }
                }

                "Focus Shoot" -> {
                    val list = thumbnails as List<CatAgnosticResV2.CategoryAgnos.Miscellaneou>

                    val position = currentShoot

                    list[position].isSelected = false
                    list[position].imageClicked = true
                    list[position].imagePath = getCurrentShoot()!!.capturedImage

                    notifyItemChanged.value = position

                    if (position != list.size.minus(1)) {
                        var foundNext = false

                        for (i in position..list.size.minus(1)) {
                            if (!list[i].isSelected && !list[i].imageClicked) {
                                foundNext = true
                                list[i].isSelected = true
                                notifyItemChanged.value = i
                                scrollView.value = i
                                break
                            }
                        }

                        if (!foundNext) {
                            val element = list.firstOrNull {
                                !it.isSelected && !it.imageClicked
                            }

                            if (element != null) {
                                element?.isSelected = true
                                notifyItemChanged.value = list.indexOf(element)
                                scrollView.value = element?.sequenceNumber!!
                            }
                        }
                    } else {
                        val element = list.firstOrNull {
                            !it.isSelected && !it.imageClicked
                        }

                        if (element != null) {
                            element?.isSelected = true
                            notifyItemChanged.value = list.indexOf(element)
                            scrollView.value = element?.sequenceNumber!!
                        }
                    }
                }
            }
        }
    }


    fun getOverlayIds(
        ids: JSONArray
    ) = viewModelScope.launch {
        _reshootOverlaysRes.value = Resource.Loading
        _reshootOverlaysRes.value = repository.getOverlayIds(ids)
    }

    suspend fun updateSkuExteriorAngles() {
        localRepository.updateSkuExteriorAngles(skuApp!!)
    }


    fun getCameraSetting(): CameraSettings {
        subcategoryV2?.let {
            return CameraSettings().apply {
                isGryroActive = Utilities.getBool(
                    BaseApplication.getContext(),
                    categoryDetails.value?.categoryId + AppConstants.SETTING_STATUS_GYRO,
                    it.cameraSettings!!.showGyro
                )
                isOverlayActive = Utilities.getBool(
                    BaseApplication.getContext(),
                    categoryDetails.value?.categoryId + AppConstants.SETTING_STATUS_OVERLAY,
                    it.cameraSettings.showOverlays
                )
                isGridActive = Utilities.getBool(
                    BaseApplication.getContext(),
                    categoryDetails.value?.categoryId + AppConstants.SETTING_STATUS_GRID,
                    it.cameraSettings.showGrid
                )
            }
        }
        return CameraSettings()

    }

    suspend fun setProjectAndSkuData(projectUuid: String, skuUuid: String) {
        projectApp = getProject(projectUuid)
        skuApp = localRepository.getSkuById(skuUuid)
    }

    suspend fun setSubcategoryData(prodSubCatId: String) {
        subcategoryV2 = localRepository.getSubcategory(prodSubCatId)
        val s = ""
    }


    suspend fun getProject(projectUuid: String) = localRepository.getProject(projectUuid)

    fun checkInteriorShootStatus() {

        GlobalScope.launch(Dispatchers.IO) {
            val interiorList = category?.interior

            if (!interiorList.isNullOrEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    showInteriorDialog.value = true
                }
                return@launch
            }

            val MiscList = category?.miscellaneous

            if (!MiscList.isNullOrEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    showMiscDialog.value = true
                }
                return@launch
            }

            GlobalScope.launch(Dispatchers.Main) {
                selectBackground(BaseApplication.getContext().getString(R.string.app_name))
            }
        }
    }

    fun updateTotalFrames() =
        localRepository.updateSkuTotalFrames(skuApp?.uuid!!, skuApp?.imagesCount!!)


    private fun getTotalFrames(): Int {
        return if (fromVideo) skuApp?.threeSixtyFrames?.plus(skuApp?.imagesCount!!)!! else skuApp?.imagesCount!!
    }

    fun getProjectSkus() = localRepository.getSkusByProjectId(projectApp?.uuid!!)

    fun setCategoryDeatils(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            category = localRepository.getCategoryById(categoryId)
            Log.d("setCategoryDeatils", "categories before: ${category}")

            viewModelScope.launch(Dispatchers.Main) {
                Log.d("setCategoryDeatils", "categories after: ${category}")
                categoryFetched.value = true

            }
        }
    }

    fun getImagesPathBySkuId() = imageRepositoryV2.getImagesPathBySkuId(skuApp?.uuid!!)

    suspend fun updateProject() {
        projectApp?.let {
            localRepository.updateProject(it)
        }
    }

    fun updateProjectStatus(uuid: String) {
        localRepository.updateProject(uuid)
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

    val categoryFetched: MutableLiveData<Boolean> = MutableLiveData()
    var gifDialogShown = false
    var createProjectDialogShown = false
    var category: CatAgnosticResV2.CategoryAgnos? = null
    var category1: CatAgnosticResV2.CategoryAgnos.SubCategoryV2? = null

    init {
        if (showVin.value == null) {
            showHint.value = true
        }

        if (showVin.value != null && isProjectCreated.value == null)
            showVin.value = true

        if (isProjectCreated.value == true)
            getSubCategories.value = true

    }

    private val _userCreditsRes: MutableLiveData<Resource<CreditDetailsResponse>> =
        MutableLiveData()
    val userCreditsRes: LiveData<Resource<CreditDetailsResponse>>
        get() = _userCreditsRes

    fun getUserCredits(
        userId: String
    ) = viewModelScope.launch {
        _userCreditsRes.value = Resource.Loading
        _userCreditsRes.value = repository.getUserCredits(userId)
    }


    fun angleClassifier(
        imageFile: MultipartBody.Part,
        requiredAngle: Int,
        cropCheck: Boolean
    ) = viewModelScope.launch {
        _angleClassifierRes.value = Resource.Loading
        _angleClassifierRes.value = repository.angleClassifier(imageFile, requiredAngle, cropCheck)
    }

    fun improveShoot(
    ) = viewModelScope.launch {
        _improveShootResponse.value = Resource.Loading
        _improveShootResponse.value = repository.improveShoot()
    }

    fun angleClassifierV2(
        imageFile : MultipartBody.Part,
        prod_cat_id :  RequestBody,
        overlay_id :  RequestBody,
    ) = viewModelScope.launch {
        _classificationRes.value = Resource.Loading
        _classificationRes.value = repository.angleClassifierV2(imageFile,prod_cat_id,overlay_id)
    }


    //DEEPLINK API
    fun sendInvitaionEmailId(
        body: InvitationEmailBody
    ) = viewModelScope.launch {
        _inviteEmailRes.value = Resource.Loading
        _inviteEmailRes.value = repository.sendInvitaionEmailId(body)
    }


    suspend fun updateSkuName(uuid: String, skuName: String) =
        localRepository.updateSkuName(uuid, skuName)

    suspend fun updateProjectName(uuid: String, projectName: String) =
        localRepository.updateProjectName(uuid, projectName)

    fun getSkuById(skuId: String) = localRepository.getSkuBySkuId(skuId)



    //home page

    var showNavigationMenu: MutableLiveData<Boolean> = MutableLiveData()



}