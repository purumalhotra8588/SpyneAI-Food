package com.spyneai.registration.view.ui.activity

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.spyneai.R
import com.spyneai.databinding.ActivityRegistrationBaseBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class RegistrationBaseActivity : AppCompatActivity() {

    lateinit var binding: ActivityRegistrationBaseBinding
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val crashButton = Button(this)
//        crashButton.text = "Test Crash"
//        crashButton.setOnClickListener {
//            throw RuntimeException("Test Crash") // Force a crash
//        }
//
//        addContentView(crashButton, ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT))

        defaultFragmentOpen()
    }

    private fun defaultFragmentOpen() {
        navController = Navigation.findNavController(this, R.id.nav_fragment_container)
        val navBuilder = NavOptions.Builder()
        if (Utilities.getPreference(this, AppConstants.PRESSED_LOGOUT).equals("1")) {
            navBuilder.setEnterAnim(R.anim.nav_default_enter_anim)
                .setExitAnim(R.anim.nav_default_enter_anim)
                .setPopEnterAnim(R.anim.nav_default_enter_anim)
                .setPopExitAnim(R.anim.nav_default_enter_anim)
            navController!!.navigate(R.id.nav_login_fragment, null, navBuilder.build())
            Utilities.savePrefrence(this, AppConstants.PRESSED_LOGOUT, "0")

        }
    }
}