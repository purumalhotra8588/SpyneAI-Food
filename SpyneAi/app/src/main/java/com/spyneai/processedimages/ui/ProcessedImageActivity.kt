package com.spyneai.processedimages.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.spyneai.base.AppViewModelFactory
import com.spyneai.databinding.ActivityProcessedImageBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.processedimages.data.ProcessedViewModelApp
import com.spyneai.reshoot.ui.SelectImagesFragment

class ProcessedImageActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProcessedImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessedImageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var bundle = Bundle()
        bundle.putString(AppConstants.CATEGORY_ID, intent.getStringExtra(AppConstants.CATEGORY_ID))

        val fragment = ProcessedImagesFragment()

        fragment.arguments = bundle

        supportFragmentManager
            .beginTransaction()
            .add(binding.flContainer.id,fragment)
            .commit()

        val processViewModel = ViewModelProvider(this, AppViewModelFactory()).get(ProcessedViewModelApp::class.java)

        processViewModel.categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
        processViewModel.projectUuid = intent.getStringExtra(AppConstants.PROJECT_UUIID)
        processViewModel.skuUuid = intent.getStringExtra(AppConstants.SKU_UUID)
        processViewModel.projectId = intent.getStringExtra(AppConstants.PROJECT_ID)
        processViewModel.skuId = intent.getStringExtra(AppConstants.SKU_ID)
        processViewModel.skuName = intent.getStringExtra(AppConstants.SKU_NAME)

        processViewModel.reshoot.observe(this) {
            val selectImagesFragment = SelectImagesFragment()
            var bundle = Bundle()
            bundle.putString(AppConstants.CATEGORY_ID, intent.getStringExtra(AppConstants.CATEGORY_ID))
            selectImagesFragment.arguments = bundle
            var id  = intent.getStringExtra(AppConstants.CATEGORY_ID)
            supportFragmentManager
                .beginTransaction()
                .add(binding.flContainer.id, selectImagesFragment)
                .commit()
        }
    }
}