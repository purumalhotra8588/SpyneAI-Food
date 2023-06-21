package com.spyneai.reshoot.ui

import CameraFragmentApp
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.base.AppViewModelFactory
import com.spyneai.getImageCategory
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.reshoot.data.SelectedImagesHelper
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.setLocale
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ConfirmReshootEcomDialog
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.data.model.CategoryDetails

import com.spyneai.shootapp.ui.dialogs.ShootExitDialog
import com.spyneai.shootapp.utils.log
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class ReshootPortraitActivity : AppCompatActivity() {

    lateinit var shootViewModelApp: ShootViewModelApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reshoot_portrait)
        setLocale()

        shootViewModelApp = ViewModelProvider(this, AppViewModelFactory()).get(ShootViewModelApp::class.java)
        shootViewModelApp.isReshoot = true

        intent.getStringExtra(AppConstants.CATEGORY_ID)?.let {
            shootViewModelApp.setCategoryDeatils(it)
        }

        shootViewModelApp.categoryFetched.observe(this) {
            shootViewModelApp.subcategoryV2 = shootViewModelApp.category?.subCategoryV2s?.get(0)

            val categoryDetails = CategoryDetails()

            categoryDetails.apply {
                categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
                categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)
                gifList = intent.getStringExtra(AppConstants.GIF_LIST)
                if(intent.getStringExtra(AppConstants.CATEGORY_ID) !=null)
                    imageType = getImageCategory(intent.getStringExtra(AppConstants.CATEGORY_ID)!!)
                val s=""
            }

            shootViewModelApp.categoryDetails.value = categoryDetails

            GlobalScope.launch(Dispatchers.IO) {
                shootViewModelApp.setProjectAndSkuData(
                    intent.getStringExtra(AppConstants.PROJECT_UUIID)!!,
                    intent.getStringExtra(AppConstants.SKU_UUID)!!
                )

                GlobalScope.launch(Dispatchers.Main) {
                    setShoot()

                    val transaction = supportFragmentManager.beginTransaction()
                        .add(R.id.flContainer, CameraFragmentApp())

                    when (categoryDetails.categoryId) {
                        AppConstants.ECOM_CATEGORY_ID,
                        AppConstants.PHOTO_BOX_CATEGORY_ID -> {

                            if(SelectedImagesHelper.selectedOverlayIds.size!=0 )
                                transaction.add(R.id.flContainer, EcomOverlayReshootFragment())
                            else
                                transaction.add(R.id.flContainer, EcomGridReshootFragment())
                        }
                        else -> {
                            transaction.add(R.id.flContainer, EcomOverlayReshootFragment())
                        }
                    }

                    transaction.commit()

                    shootViewModelApp.reshootCompleted.observe(this@ReshootPortraitActivity) {
                        Intent(this@ReshootPortraitActivity, ReshootDoneActivity::class.java)
                            .apply {
                                putExtra(AppConstants.CATEGORY_ID, categoryDetails.categoryId)
                                startActivity(this)
                            }
                    }
                }
            }
        }
    }

    private fun setShoot() {
        shootViewModelApp.getSubCategories(
            Utilities.getPreference(this, AppConstants.AUTH_KEY).toString(),
            intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )

        shootViewModelApp.isProjectCreated.value = true
        shootViewModelApp.projectId.value = intent.getStringExtra(AppConstants.PROJECT_ID)

        val sku = Sku(
            projectUuid = intent.getStringExtra(AppConstants.PROJECT_ID)!!,
            uuid = intent.getStringExtra(AppConstants.SKU_UUID)!!,
            skuName = intent.getStringExtra(AppConstants.SKU_NAME),
            skuId = intent.getStringExtra(AppConstants.SKU_ID),
            projectId = intent.getStringExtra(AppConstants.PROJECT_ID)
        )
//
        shootViewModelApp.skuApp = sku
    }

    override fun onBackPressed() {
        ShootExitDialog().show(supportFragmentManager, "ShootExitDialog")
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

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

                ConfirmReshootEcomDialog().show(supportFragmentManager, "ConfirmReshootEcomDialog")

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }
}