package com.spyneai.onboardingv2.ui.intro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.databinding.ActivityImageProcessingBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shootapp.ui.dialogs.ShootExitDialog
import com.spyneai.singleimageprocessing.data.SingleImageViewModel

class ImageProcessingActivity : AppCompatActivity() {
    lateinit var binding : ActivityImageProcessingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_category)
        binding = ActivityImageProcessingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val shootViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            SingleImageViewModel::class.java)

        //set category details
        shootViewModel.setCategoryDeatils(Utilities.getPreference(this, AppConstants.SELECTED_CATEGORY_ID).toString())

        shootViewModel.categoryFetched.observe(this,{
            if (it){
                val bundle =  Bundle()
                    .apply {
                        putString("selected_image",intent.getStringExtra("selected_image"))
                        putString(AppConstants.SUB_CAT_ID,intent.getStringExtra(AppConstants.SUB_CAT_ID))
                        putString(AppConstants.PROJECT_ID,intent.getStringExtra(AppConstants.PROJECT_ID))
                        putString(AppConstants.SKU_ID,intent.getStringExtra(AppConstants.SKU_ID))
                        putString(AppConstants.IMAGE_URL,intent.getStringExtra(AppConstants.IMAGE_URL))
                        putString(AppConstants.IMAGE_NAME,intent.getStringExtra(AppConstants.IMAGE_NAME))
                        putInt(AppConstants.IMAGE_ANGLE,intent.getIntExtra(AppConstants.IMAGE_ANGLE,0))
                    }
                supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, ProcessImageFragment().apply {
                        arguments = bundle
                    })
                    .commit()
            }
        })
    }
    override fun onBackPressed() {
        ShootExitDialog().show(supportFragmentManager, "ShootExitDialog")
    }
}