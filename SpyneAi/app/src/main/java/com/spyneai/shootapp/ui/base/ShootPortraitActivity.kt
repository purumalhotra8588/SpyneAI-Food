package com.spyneai.shootapp.ui.base


import CameraFragmentApp
import android.Manifest
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.spyneai.CropConfirmDialog
import com.spyneai.R
import com.spyneai.base.AppViewModelFactory
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.setLocale
import com.spyneai.shoot.ui.CreateProjectFragment
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.data.model.CategoryDetails
import com.spyneai.shootapp.data.model.ShootData
import com.spyneai.shootapp.ui.DraftGridFragment
import com.spyneai.shootapp.ui.ImproveShootFragment
import com.spyneai.shootapp.ui.SelectBackgroundFragment
import com.spyneai.shootapp.ui.SubCategoryAndAngleFragment
import com.spyneai.shootapp.ui.dialogs.ShootExitDialog
import com.spyneai.shootapp.ui.ecomwithgrid.GridEcomFragment
import com.spyneai.shootapp.ui.ecomwithoverlays.OverlayEcomFragment
import com.spyneai.shootapp.utils.log
import com.spyneai.shootapp.utils.shoot
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.*


class ShootPortraitActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    lateinit var cameraFragmentApp: CameraFragmentApp
    lateinit var gridEcomFragment: GridEcomFragment
    lateinit var overlayEcomFragment: OverlayEcomFragment
    lateinit var improveShootFragment: ImproveShootFragment
    lateinit var selectBackgroundFragment: SelectBackgroundFragment
    lateinit var shootViewModelApp: ShootViewModelApp
    val location_data = JSONObject()
    val TAG = "ShootPortraitActivity"
    private var googleApiClient: GoogleApiClient? = null
    var subCatId: String? = null
    lateinit var subCategoryAndAngleFragment: SubCategoryAndAngleFragment

//    var projectCount =0
//    var skuCount =0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_shoot)

        googleApiClient =
            GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build()

        setLocale()

        shootViewModelApp =
            ViewModelProvider(this, AppViewModelFactory()).get(ShootViewModelApp::class.java)

        shootViewModelApp.replaceFragment.observe(this) {
            if (it) {
                if (intent.getStringExtra(AppConstants.SUB_CAT_ID) == null) {
                    val transaction = supportFragmentManager.beginTransaction()
                        .remove(gridEcomFragment)
                        .add(R.id.flCamerFragment, OverlayEcomFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }

        }

        GlobalScope.launch(Dispatchers.IO) {
            shootViewModelApp.projectCount = shootViewModelApp.getAllProjectsSize()
            shootViewModelApp.skuCount =
                shootViewModelApp.getSkusCountByProjectUuid(
                    shootViewModelApp.projectApp?.uuid ?: getUuid()
                )
            Log.d("Shootmodellllll", "skuCount: ${shootViewModelApp.skuCount}")
        }
        //set category details
        intent.getStringExtra(AppConstants.CATEGORY_ID)?.let {
            shootViewModelApp.setCategoryDeatils(it)
        }

        shootViewModelApp.skuNumber.value = 1
        try {
            shootViewModelApp.skuNumber.value = intent.getIntExtra("skuNumber", 1)
            val uuid = intent.getStringExtra(AppConstants.PROJECT_UUIID)

            uuid?.let {
                GlobalScope.launch(Dispatchers.IO) {
                    shootViewModelApp.projectApp = shootViewModelApp.getProject(it)
                }
            }
        } catch (e: Exception) {
        }

        if (intent.getStringExtra(AppConstants.SUB_CAT_ID) != null) {
            if (intent.getStringExtra(AppConstants.SUB_CAT_ID) != "subcategoryId") {
                subCatId = intent.getStringExtra(AppConstants.SUB_CAT_ID)
            }
        }

        shootViewModelApp.categoryFetched.observe(this) {
            if (it) {
                val categoryDetails = CategoryDetails()

                categoryDetails.apply {
                    categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
                    categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)
                    gifList = intent.getStringExtra(AppConstants.GIF_LIST)
                }

                shootViewModelApp.categoryDetails.value = categoryDetails


                shootViewModelApp.category?.subCategoryV2s?.let { subcategories ->
                    subcategories.forEach { subcategory ->
                        if (subcategory.prodSubCatId == subCatId) {
                            if (!subcategory.overlayApps.isNullOrEmpty())
                                shootViewModelApp.hasEcomOverlay = true
                        }
                    }
                }

                if (intent.getBooleanExtra(AppConstants.FROM_DRAFTS, false))
                    setUpDraftsData()

                cameraFragmentApp = CameraFragmentApp()
                gridEcomFragment = GridEcomFragment()
//                skuDetailFragment = SkuDetailFragment()
//                projectDetailFragment = ProjectDetailFragment()
                overlayEcomFragment = OverlayEcomFragment()
                selectBackgroundFragment = SelectBackgroundFragment()
                subCategoryAndAngleFragment = SubCategoryAndAngleFragment()
                improveShootFragment = ImproveShootFragment()




                shootViewModelApp.removeImproveShootFragment.observe(this) {
                    if (it) {
                        shootViewModelApp.removeImproveShootFragment.value = false
                        Utilities.saveBool(this, AppConstants.SHOW_IMPROVE_SHOOT, false)

                        val manager: FragmentManager = supportFragmentManager
                        val trans: FragmentTransaction = manager.beginTransaction()
                        trans.remove(improveShootFragment)
                        trans.commit()
                        manager.popBackStackImmediate()
                    }
                }

                shootViewModelApp.processSku = false

                if (savedInstanceState == null) { // initial transaction should be wrapped like this
                    val transaction = supportFragmentManager.beginTransaction()
                        .add(R.id.flCamerFragment, cameraFragmentApp)

                    if (shootViewModelApp.fromDrafts) {
                        shootViewModelApp.hasEcomOverlay = false

                        shootViewModelApp.category?.subCategoryV2s?.let { subcategories ->
                            subcategories.forEach { subcategory ->
                                if (subcategory.prodSubCatId == subCatId) {
                                    if (!subcategory.overlayApps.isNullOrEmpty())
                                        shootViewModelApp.hasEcomOverlay = true
                                }
                            }
                        }

                        if (shootViewModelApp.category?.shootExperience?.hasSubcategories!!) {
                            if (shootViewModelApp.hasEcomOverlay) {
                                transaction.add(R.id.flCamerFragment, overlayEcomFragment)
                                    .commitAllowingStateLoss()
                            } else {
                                transaction
                                    .add(R.id.flCamerFragment, DraftGridFragment())
                                    .commitAllowingStateLoss()

                            }
                        } else {
                            transaction
                                .add(R.id.flCamerFragment, DraftGridFragment())
                                .commitAllowingStateLoss()
                        }
                    } else {
                        if (shootViewModelApp.category?.subCategoryV2s.isNullOrEmpty()) {
                            transaction.add(R.id.flCamerFragment, gridEcomFragment)
                        } else {

                            log("hasSubcategories" + shootViewModelApp.category?.shootExperience?.hasSubcategories!!.toString())
                            log("hasSubcategories" + shootViewModelApp.category?.subCategoryV2s!![0].overlayApps?.size)


                            if (shootViewModelApp.category?.shootExperience?.hasSubcategories!!
                                && shootViewModelApp.hasEcomOverlay
                            ) {
                                transaction.add(R.id.flCamerFragment, overlayEcomFragment)
                            } else {
                                transaction.add(R.id.flCamerFragment, gridEcomFragment)
                            }

                            observeProjectCreated()
                        }

                        transaction
                            .add(R.id.flCamerFragment, CreateProjectFragment())
                            .commitAllowingStateLoss()
                    }
                }

                shootViewModelApp.category?.imageCategories?.let { it ->
                    shootViewModelApp.categoryDetails.value?.imageType = it[0]
                }


                shootViewModelApp.stopShoot.observe(this) {
                    if (it) {
                        // start process activity
                        val processIntent = Intent(this, ProcessActivity::class.java)
                        processIntent.apply {
                            this.putExtra(AppConstants.CATEGORY_NAME, categoryDetails.categoryName)
                            this.putExtra(AppConstants.CATEGORY_ID, categoryDetails.categoryId)
                            this.putExtra(AppConstants.SKU_UUID, shootViewModelApp.skuApp?.uuid)
                            this.putExtra(
                                AppConstants.PROJECT_UUIID,
                                shootViewModelApp.skuApp?.projectUuid
                            )
                            this.putExtra(
                                "exterior_angles",
                                shootViewModelApp.exterirorAngles.value
                            )
                            this.putExtra("process_sku", shootViewModelApp.processSku)
                            this.putExtra(
                                AppConstants.FROM_VIDEO,
                                intent.getBooleanExtra(AppConstants.FROM_VIDEO, false)
                            )
                            Log.d("ShootPortraitActivity", "ProcessStart" + "ShootPortraitActivity")
                            startActivity(this)
                        }
                    }
                }
            }
        }
    }

    private fun observeProjectCreated() {
        //add subcat selection fragment
        shootViewModelApp.getSubCategories.observe(
            this
        ) {
            if (!shootViewModelApp.isSubcategoriesSelectionShown) {
                val bundle = Bundle()
                bundle.putString(AppConstants.SUB_CAT_ID, subCatId)
                subCategoryAndAngleFragment.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .add(R.id.flCamerFragment, subCategoryAndAngleFragment)
                    .commit()
            }
        }
    }

    private fun setUpDraftsData() {
        shootViewModelApp.fromDrafts = true
        shootViewModelApp.showVin.value = true
        shootViewModelApp.isProjectCreated.value = true

        intent.getStringExtra(AppConstants.PRODUCT_ID).let {
            shootViewModelApp.projectId.value = it
        }

        lifecycleScope.launch(Dispatchers.IO) {
            shootViewModelApp.setProjectAndSkuData(
                intent.getStringExtra(AppConstants.PROJECT_UUIID)!!,
                intent.getStringExtra(AppConstants.SKU_UUID)!!
            )

            intent.getStringExtra(AppConstants.SUB_CAT_ID)?.let {
                if (it != "subcategoryId")
                    shootViewModelApp.setSubcategoryData(it)
            }

            lifecycleScope.launch(Dispatchers.Main) {
                when (intent.getStringExtra(AppConstants.CATEGORY_ID)) {
                    AppConstants.FOOTWEAR_CATEGORY_ID,
                    AppConstants.FOOD_AND_BEV_CATEGORY_ID,
                    AppConstants.MENS_FASHION_CATEGORY_ID,
                    AppConstants.WOMENS_FASHION_CATEGORY_ID,
                    AppConstants.CAPS_CATEGORY_ID,
                    AppConstants.FASHION_CATEGORY_ID,
                    AppConstants.ECOM_CATEGORY_ID,
                    AppConstants.ACCESSORIES_CATEGORY_ID,
                    AppConstants.HEALTH_AND_BEAUTY_CATEGORY_ID -> {
                        when {
                            intent.getStringExtra(AppConstants.SUB_CAT_ID) == null
                                    || intent.getStringExtra(AppConstants.SUB_CAT_ID) == "subcategoryId" -> {
                                observeProjectCreated()
                                shootViewModelApp.getSubCategories.value = true
                            }

                            intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0)
                                    == intent.getIntExtra(AppConstants.EXTERIOR_SIZE, 0)
                                    && intent.getIntExtra(AppConstants.EXTERIOR_SIZE, 0) != 0 -> {
                                setClickedImages()
                                shootViewModelApp.stopShoot.value = true
                            }

                            else -> {
                                setClickedImages()
                                shootViewModelApp.exterirorAngles.value = 0
                                shootViewModelApp.isSkuCreated.value = true
                                //sub category selected
                                shootViewModelApp.subCatName.value =
                                    intent.getStringExtra(AppConstants.SUB_CAT_NAME)

                                shootViewModelApp.isSubCategoryConfirmed.value = true
                                shootViewModelApp.showDialog = false
                            }
                        }


                    }
                    else -> {
                        shootViewModelApp.showDialog = false
                        shootViewModelApp.isSubCategoryConfirmed.value = true

                        shootViewModelApp.shootList.value = ArrayList()

                        if (intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)) {
                            setClickedImages()
                        } else {
                            shootViewModelApp.shootList.value = ArrayList()

                            val list = intent.getStringArrayListExtra(AppConstants.EXTERIOR_LIST)
                            val imageNameList =
                                intent.getStringArrayListExtra(AppConstants.SHOOT_IMAGE_NAME_LIST)

                            list?.forEachIndexed { index, image ->
                                val shootData = ShootData(
                                    image,
                                    intent.getStringExtra(AppConstants.PROJECT_ID)!!,
                                    intent.getStringExtra(AppConstants.SKU_ID)!!,
                                    shootViewModelApp.categoryDetails.value?.imageType!!,
                                    Utilities.getPreference(
                                        this@ShootPortraitActivity,
                                        AppConstants.AUTH_KEY
                                    ).toString(),
                                    index,
                                    index.plus(1),
                                    0,
                                    imageNameList!![index]
                                )

                                shootData.imageClicked = true
                                shootViewModelApp.shootList.value!!.add(
                                    shootData
                                )
                            }

                        }
                    }
                }
            }
        }
    }

    private fun setClickedImages() {
        shootViewModelApp.shootList.value = ArrayList()
        GlobalScope.launch(Dispatchers.IO) {
            val list = shootViewModelApp.getImagesbySkuId(shootViewModelApp.skuApp?.uuid!!)
            list.forEachIndexed { index, image ->
                val shootData = ShootData(
                    image.path!!,
                    image.projectUuid!!,
                    image.skuUuid!!,
                    shootViewModelApp.categoryDetails.value?.imageType!!,
                    Utilities.getPreference(this@ShootPortraitActivity, AppConstants.AUTH_KEY)
                        .toString(),
                    image.overlayId?.toInt()!!,
                    image.sequence!!,
                    image.angle!!,
                    image.name!!
                )

                shootData.imageClicked = true
                shootViewModelApp.shootList.value!!.add(
                    shootData
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        googleApiClient?.connect()
        shoot("onStart called(shhot activity)")
    }

    override fun onStop() {
        super.onStop()
        googleApiClient?.disconnect()
        Log.d(TAG, "onStop: ")
        shoot("onStop called(shoot activity)")
    }

    override fun onConnected(bundle: Bundle?) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {

            try {
                val lastLocation =
                    LocationServices.FusedLocationApi.getLastLocation(googleApiClient!!)
                val lat: Double = lastLocation.latitude
                val lon: Double = lastLocation.longitude

                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses: List<Address> = geocoder.getFromLocation(lat, lon, 1)!!
                val postalCode = addresses[0].postalCode
                val cityName = addresses[0].locality
                val countryName = addresses[0].countryName

                location_data.put("city", cityName)
                location_data.put("country", countryName)
                location_data.put("latitude", lat)
                location_data.put("longitude", lon)
                location_data.put("postalCode", postalCode)

                Log.d(TAG, "onConnected: $location_data")

                shootViewModelApp.location_data.value = location_data
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }

    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }


    override fun onBackPressed() {
        ShootExitDialog().show(supportFragmentManager, "ShootExitDialog")
    }

    // 1. onKeyDown is a boolean function, which returns the state of the KeyEvent.
    // 4. This code can be used to check if the device responds to any Key.
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event?.repeatCount == 0) {
                    if (shootViewModelApp.onVolumeKeyPressed.value == null)
                        shootViewModelApp.onVolumeKeyPressed.value = true
                    else
                        shootViewModelApp.onVolumeKeyPressed.value =
                            !shootViewModelApp.onVolumeKeyPressed.value!!
                }
            }

            KeyEvent.KEYCODE_BACK -> {
                onBackPressed()
            }
        }
        return true
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        Log.d(TAG, "onActivityResult: $requestCode $resultCode" )
        
        shootViewModelApp.isCameraButtonClickable = true


        if (data == null || resultCode == 0) {
            shootViewModelApp.shootList.value?.removeAt(shootViewModelApp.shootList.value!!.size - 1)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri

                log("image uri- " + result.uri)

                File(resultUri.path)
                    .copyTo(
                        File(shootViewModelApp.shootData.value?.capturedImage),
                        true
                    )

                //shootViewModel.shootData.value?.capturedImage = file.path

                CropConfirmDialog().show(supportFragmentManager, "CropConfirmDialog")

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
    }
    
    
}



