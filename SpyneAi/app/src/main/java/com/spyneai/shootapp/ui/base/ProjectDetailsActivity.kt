package com.spyneai.shootapp.ui.base

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import com.spyneai.R
import com.spyneai.base.BaseActivity
import com.spyneai.databinding.ActivityProjectDetailsBinding
import com.spyneai.needs.AppConstants
import com.spyneai.output.ui.OutputFragment
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.repository.model.payment.PaymentStatus
import com.spyneai.shootapp.ui.dialogs.ShootExitDialog
import com.spyneai.showConnectionChangeView
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectDetailsActivity : BaseActivity() {

    lateinit var binding: ActivityProjectDetailsBinding
    lateinit var shootViewModelApp: ShootViewModelApp
    lateinit var threeSixtyViewModel:ThreeSixtyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shootViewModelApp = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            ShootViewModelApp::class.java
        )

        if (intent.getStringExtra(AppConstants.SKU_ID) == null){
            setUpDetails()
        }else {
           lifecycleScope.launch {
               val sku = withContext(Dispatchers.IO) {
                   shootViewModelApp.getSkuById(intent.getStringExtra(AppConstants.SKU_ID).toString())
               }

//               sku?.let {
//                   intent.putExtra(AppConstants.PROJECT_UUIID,it.projectUuid)
//                   intent.putExtra(AppConstants.SKU_UUID,it.skuId)
//               }
               setUpDetails()
           }
        }
    }

    private fun setUpDetails() {
        threeSixtyViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            ThreeSixtyViewModel::class.java
        )

        val projectUuid = intent.getStringExtra(AppConstants.PROJECT_UUIID)!!
        val skuUUid = intent.getStringExtra(AppConstants.SKU_UUID)!!

        Log.d(TAG, "onCreate: ${projectUuid}")
        threeSixtyViewModel.fromVideo = intent.getBooleanExtra(AppConstants.FROM_VIDEO,false)

        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                shootViewModelApp.setProjectAndSkuData(
                    projectUuid,
                    skuUUid
                )
            }

            withContext(Dispatchers.Main){
                //set category details
                intent.getStringExtra(AppConstants.CATEGORY_ID)?.let {
                    shootViewModelApp.setCategoryDeatils(it)
                }

                if (intent.getBooleanExtra(AppConstants.FROM_DRAFTS, false)){
                    supportFragmentManager.beginTransaction()
                        .add(R.id.flContainer, OutputFragment(), "OutputFragment")
                        .addToBackStack("OutputFragment")
                        .commit()
                } else{
                    supportFragmentManager.beginTransaction()
                        .add(R.id.flContainer, OutputFragment(), "OutputFragment")
                        .addToBackStack("OutputFragment")
                        .commit()
                }
            }
        }
    }

    override fun onBackPressed() {
        ShootExitDialog().show(supportFragmentManager, "ShootExitDialog")
    }

    override fun onConnectionChange(isConnected: Boolean) {
        showConnectionChangeView(isConnected, binding.root)
    }

}