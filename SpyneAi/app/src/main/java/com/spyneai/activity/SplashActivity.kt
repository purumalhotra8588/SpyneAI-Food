package com.spyneai.activity


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.AppSignatureHelper
import com.spyneai.BuildConfig
import com.spyneai.R
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.getNetworkName
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.ui.intro.ChooseCategoryActivity
import com.spyneai.registration.view.ui.activity.RegistrationBaseActivity
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()

    }

    private fun initView() {
        val hash = AppSignatureHelper(packageName, packageManager).getAppSignatures()

        Log.d(TAG, "onCreate: ${hash.toString()}")
        hash?.forEach {
            Log.d(TAG, "onCreate: $it")
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)

        when (getString(R.string.app_name)) {
            AppConstants.SPYNE_AI_AUTOMOBILE, AppConstants.SPYNE_AI, AppConstants.AUTO_FOTO -> {
                ivPowredBy.visibility = View.INVISIBLE
            }
            else -> {
                ivPowredBy.visibility = View.VISIBLE
            }
        }


        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val version = Build.VERSION.SDK_INT
        val versionCode: Int = BuildConfig.VERSION_CODE
        val versionName: String = BuildConfig.VERSION_NAME
        val networkCarrier = getNetworkName()


        Utilities.savePrefrence(this, AppConstants.DEVICE_ID, deviceId)
        Utilities.savePrefrence(this, AppConstants.DEVICE_MANUFACTURER, manufacturer)
        Utilities.savePrefrence(this, AppConstants.MODEL, model)
        Utilities.savePrefrence(this, AppConstants.OS_VERSION, version.toString())
        Utilities.savePrefrence(this, AppConstants.APP_VERSION, versionName)
        Utilities.savePrefrence(this, AppConstants.APP_VERSION_CODE, versionCode.toString())
        Utilities.savePrefrence(this, AppConstants.NETWORK_TYPE, networkCarrier)
        Utilities.savePrefrence(this, AppConstants.DEVICE_ID, deviceId)
        Utilities.savePrefrence(this, AppConstants.OS, "Android")
        if (getString(R.string.app_name) == AppConstants.SPYNE_AI){
            Utilities.savePrefrence(this, AppConstants.TYPE, "self-serve")
    }else{
            Utilities.savePrefrence(this, AppConstants.TYPE, "")
        }



        if (Utilities.getPreference(this, AppConstants.STATUS_PROJECT_NAME).isNullOrEmpty()) {
            Utilities.savePrefrence(this, AppConstants.STATUS_PROJECT_NAME, "true")
        }

        setSplash()
    }


    //Start splash
    private fun setSplash() {
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            if (Utilities.getPreference(this, AppConstants.AUTH_KEY).isNullOrEmpty()) {

                Utilities.saveBool(this, AppConstants.SHOW_IMPROVE_SHOOT, true)

                var intent = Intent(this, RegistrationBaseActivity::class.java)

                startActivity(intent)
                finishAffinity()
            } else {
                Log.d(TAG, "setSplash: ${Utilities.getPreference(this,AppConstants.SELECTED_CATEGORY_ID)}")
                if (Utilities.getPreference(this,AppConstants.SELECTED_CATEGORY_ID).isNullOrEmpty() || Utilities.getPreference(this,AppConstants.SELECTED_CATEGORY_ID) == "cat_d8R14zUNE")
                    startActivity(Intent(this, ChooseCategoryActivity::class.java))
                else{
                    val intent = Intent(this,MainDashboardActivity::class.java)
                    intent.putExtra("SOURCE","0")
                    startActivity(intent)
                }


                finishAffinity()
            }
        }, 1000)
    }

}