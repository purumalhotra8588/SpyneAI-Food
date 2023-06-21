package com.spyneai.onboardingv2.ui.onboarding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.base.AppViewModelFactory
import com.spyneai.databinding.ActivityLoginWithPhoneBinding
import com.spyneai.onboarding.data.viewmodels.OnBoardingViewModel
import com.spyneai.onboarding.ui.EnterOtpFragment
import com.spyneai.onboarding.ui.PhoneOtpFragment

class LoginWithPhoneActivity : AppCompatActivity() {

    lateinit var binding : ActivityLoginWithPhoneBinding
    lateinit var viewModel : OnBoardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginWithPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, AppViewModelFactory()).get(OnBoardingViewModel::class.java)

        supportFragmentManager
            .beginTransaction()
            .add(binding.flContainer.id, PhoneOtpFragment())
            .commit()

        viewModel?.reqOtpSuccess?.observe(this, {
            if (it) {
                val fragment: Fragment = EnterOtpFragment()
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                    )
                    .replace(binding.flContainer.id, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        })
    }
}