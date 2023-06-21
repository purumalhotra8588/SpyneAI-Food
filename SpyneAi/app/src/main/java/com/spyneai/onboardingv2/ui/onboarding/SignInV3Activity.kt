package com.spyneai.onboardingv2.ui.onboarding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.base.AppViewModelFactory
import com.spyneai.databinding.ActivitySignInV3Binding
import com.spyneai.onboarding.data.viewmodels.OnBoardingViewModel
import com.spyneai.onboarding.ui.EnterOtpFragment
import com.spyneai.onboarding.ui.PhoneOtpFragment


class SignInV3Activity : AppCompatActivity() {
    lateinit var binding: ActivitySignInV3Binding
    lateinit var viewModel: OnBoardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInV3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, AppViewModelFactory()).get(OnBoardingViewModel::class.java)



        supportFragmentManager
            .beginTransaction()
            .add(binding.flContainer.id, PhoneOtpFragment())
            .commit()

        viewModel.reqOtpSuccess.observe(this) {
            if (it) {
                val bundle = Bundle()
                bundle.putString("sent_to", viewModel.sentTo)

                val fragment: Fragment = EnterOtpFragment()
                fragment.arguments = bundle

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
        }
    }


}