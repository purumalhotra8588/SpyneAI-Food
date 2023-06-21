package com.spyneai.shootapp.ui.base

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.base.AppViewModelFactory
import com.spyneai.databinding.ActivityProcessBinding
import com.spyneai.food.OutputFoodFragment
import com.spyneai.needs.AppConstants
import com.spyneai.setLocale
import com.spyneai.shootapp.data.ProcessViewModelApp
import com.spyneai.shootapp.ui.RegularShootSummaryFragment
import com.spyneai.shootapp.ui.SelectBackgroundFragment
import com.spyneai.shootapp.ui.dialogs.ShootExitDialog
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ProcessActivity : AppCompatActivity() {

    lateinit var binding: ActivityProcessBinding
    lateinit var processViewModelApp: ProcessViewModelApp
    lateinit var threeSixtyViewModel: ThreeSixtyViewModel
    lateinit var outputFoodFragment: OutputFoodFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLocale()

        Log.d("ProcessActivity", "onCreateTimes"+"ProcessActivity")


        processViewModelApp =
            ViewModelProvider(this, AppViewModelFactory()).get(ProcessViewModelApp::class.java)

        threeSixtyViewModel =
            ViewModelProvider(this, AppViewModelFactory()).get(ThreeSixtyViewModel::class.java)

        intent.getStringExtra(AppConstants.CATEGORY_ID)?.let {
            processViewModelApp.setCategoryDeatils(it)
        }




        processViewModelApp.foodoutputfragment.observe(this){
            it?.let {

                Log.d("ProcessActivity", "onCreate: $it")
                val bundle = Bundle()
                bundle.putString("image_url",it)

                outputFoodFragment = OutputFoodFragment()

                outputFoodFragment.arguments = bundle

                supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, outputFoodFragment)
                    .commit()
            }

        }



        processViewModelApp.categoryFetched.observe(this) {
            if (it) {
                processViewModelApp.fromVideo = intent.getBooleanExtra(AppConstants.FROM_VIDEO, false)

                processViewModelApp.exteriorAngles.value = intent.getIntExtra("exterior_angles", 0)
                processViewModelApp.categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)

                val projectUuid = intent.getStringExtra(AppConstants.PROJECT_UUIID)!!
                val skuUUid = intent.getStringExtra(AppConstants.SKU_UUID)!!

                GlobalScope.launch(Dispatchers.IO) {
                    processViewModelApp.setProjectAndSkuData(
                        projectUuid,
                        skuUUid
                    )
                }
//
//                if (processViewModelApp.fromVideo) {
//                    supportFragmentManager.beginTransaction()
//                        .commit()
//                } else {
                    supportFragmentManager.beginTransaction()
                        .add(com.spyneai.R.id.flContainer, SelectBackgroundFragment())
                        .commit()
//                }


//                threeSixtyViewModel.showBackgroundFragment.observe(this){
//                    if (it) {
//                        val transaction: FragmentTransaction =
//                            supportFragmentManager.beginTransaction()
//                        transaction.replace(com.spyneai.R.id.flContainer, videoBackgroundFragment)
//                        transaction.addToBackStack(null)
//                        transaction.commit()
//                    }
//                }



                processViewModelApp.startTimer.observe(this) {
                    if (it) {
                        val projectDetailsIntent = Intent(this, ProjectDetailsActivity::class.java)
                        projectDetailsIntent.apply {
                            putExtra(AppConstants.CATEGORY_ID, intent.getStringExtra(AppConstants.CATEGORY_ID))
                            putExtra(AppConstants.PROJECT_UUIID, intent.getStringExtra(AppConstants.PROJECT_UUIID)!!)
                            putExtra(AppConstants.SKU_UUID, intent.getStringExtra(AppConstants.SKU_UUID)!!)
                            startActivity(this)
                        }
                    }
                }

                threeSixtyViewModel.threeSixtyStartTimer.observe(this) {
                    if (it) {
                        val projectDetailsIntent = Intent(this, ProjectDetailsActivity::class.java)
                        projectDetailsIntent.apply {
                            putExtra(AppConstants.CATEGORY_ID, intent.getStringExtra(AppConstants.CATEGORY_ID))
                            putExtra(AppConstants.PROJECT_UUIID, intent.getStringExtra(AppConstants.PROJECT_UUIID)!!)
                            putExtra(AppConstants.FROM_VIDEO, true)
                            putExtra(AppConstants.SKU_UUID, intent.getStringExtra(AppConstants.SKU_UUID)!!)
                        }
                        startActivity(projectDetailsIntent)
                    }
                }

                processViewModelApp.addRegularShootSummaryFragment.observe(this) {
                    if (it) {
                        // add select background fragment
                        supportFragmentManager.beginTransaction()
                            .add(com.spyneai.R.id.flContainer, RegularShootSummaryFragment())
                            .addToBackStack("RegularShootSummaryFragment")
                            .commit()

                        processViewModelApp.addRegularShootSummaryFragment.value = false
                    }
                }
            }
        }
    }


    override fun onBackPressed() {
        if (threeSixtyViewModel.isVideoBackgroundFragmentActive) {
            threeSixtyViewModel.isVideoBackgroundFragmentActive = false
            super.onBackPressed()
        } else {
                ShootExitDialog().show(supportFragmentManager, "ShootExitDialog")
        }

    }
}