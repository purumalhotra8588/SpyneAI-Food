package com.spyneai.processedimages.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.base.AppViewModelFactory
import com.spyneai.databinding.ActivityProcessedImageBinding
import com.spyneai.needs.AppConstants
import com.spyneai.processedimages.data.ProcessedViewModelApp
import com.spyneai.reshoot.ui.SelectImagesFragment

class ProcessedPagedImagesActivity : AppCompatActivity() {
    private lateinit var binding : ActivityProcessedImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessedImageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val fragment = ProcessedImagesFragment()

        supportFragmentManager
            .beginTransaction()
            .add(binding.flContainer.id,fragment)
            .commit()

        val processViewModel = ViewModelProvider(this, AppViewModelFactory()).get(ProcessedViewModelApp::class.java)

        processViewModel.categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)

        processViewModel.reshoot.observe(this) {
            supportFragmentManager
                .beginTransaction()
                .add(binding.flContainer.id, SelectImagesFragment())
                .commit()
        }
    }
}