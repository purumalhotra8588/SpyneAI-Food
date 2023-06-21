package com.spyneai.trybackground

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.databinding.ActivitySampleImagesBinding
import com.spyneai.databinding.ActivityTryBackgorundBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.ui.intro.SampleImagesFragment
import com.spyneai.singleimageprocessing.data.SingleImageViewModel
import com.spyneai.trybackground.ui.TryBackGroundFragment

class TryBackgroundActivity : AppCompatActivity() {
    lateinit var binding : ActivityTryBackgorundBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_category)
        binding = ActivityTryBackgorundBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val shootViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            SingleImageViewModel::class.java)

        //set category details
        shootViewModel.setCategoryDeatils(Utilities.getPreference(this, AppConstants.SELECTED_CATEGORY_ID).toString())

        val bundle = Bundle()

        bundle.putString(AppConstants.IMAGE_URL,intent.getStringExtra(AppConstants.IMAGE_URL).toString())
        bundle.putString(AppConstants.BACKGROUND_ID,intent.getStringExtra(AppConstants.BACKGROUND_ID).toString())
        bundle.putString(AppConstants.UPLOAD_URL,intent.getStringExtra(AppConstants.UPLOAD_URL).toString())
        bundle.putString(AppConstants.PROJECT_ID,intent.getStringExtra(AppConstants.PROJECT_ID).toString())
        bundle.putString(AppConstants.SKU_ID,intent.getStringExtra(AppConstants.SKU_ID).toString())

        shootViewModel.categoryFetched.observe(this) {
            if (it) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, TryBackGroundFragment().apply {
                        arguments = bundle
                    })
                    .commit()
            }
        }
    }
}