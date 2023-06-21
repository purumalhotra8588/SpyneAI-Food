package com.spyneai.onboardingv2.ui.intro

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.R
import com.spyneai.databinding.ActivityChooseCategoryBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class ChooseCategoryActivity : AppCompatActivity() {
    lateinit var binding : ActivityChooseCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_category)
        binding = ActivityChooseCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        Utilities.savePrefrence(this, AppConstants.DEVICE_ID,deviceId)
        Log.d("ChooseCategoryActivity", "onCreate: $deviceId")
        val chooseCategoryFragment = ChooseCategoryFragment()
        val bundle = Bundle()

        if(intent!=null)
        bundle.putString("countryName",intent.getStringExtra("CompanyName"))
        chooseCategoryFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, chooseCategoryFragment)
            .commit()
    }
}