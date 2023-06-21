package com.spyneai.singleimageprocessing.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.databinding.ActivitySingleImageClickPortraitBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.singleimageprocessing.data.SingleImageViewModel

class SingleImageClickPortraitActivity : AppCompatActivity() {
    lateinit var binding : ActivitySingleImageClickPortraitBinding
    lateinit var shootViewModel : SingleImageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding = ActivitySingleImageClickPortraitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shootViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            SingleImageViewModel::class.java)

        //set category details
        shootViewModel.setCategoryDeatils(Utilities.getPreference(this, AppConstants.SELECTED_CATEGORY_ID).toString())

        shootViewModel.categoryFetched.observe(this) {
            if (it) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, SingleImageCamera())
                    .commit()
            }
        }
    }
}