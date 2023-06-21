package com.spyneai.onboardingv2.ui.intro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.databinding.ActivityChooseCategoryBinding
import com.spyneai.databinding.ActivitySampleImagesBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.singleimageprocessing.data.SingleImageViewModel

class SampleImagesActivity : AppCompatActivity() {
    lateinit var binding : ActivitySampleImagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_category)
        binding = ActivitySampleImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val shootViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            SingleImageViewModel::class.java)

        //set category details
        shootViewModel.setCategoryDeatils(Utilities.getPreference(this, AppConstants.SELECTED_CATEGORY_ID).toString())

        shootViewModel.categoryFetched.observe(this) {
            if (it) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, SampleImagesFragment())
                    .commit()
            }
        }


    }
}