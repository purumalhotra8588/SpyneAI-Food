package com.spyneai.onboardingv2.ui.onboarding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.spyneai.base.AppViewModelFactory
import com.spyneai.databinding.ActivitySignUp2Binding
import com.spyneai.onboarding.data.viewmodels.OnBoardingViewModel
import com.spyneai.onboarding.ui.SignupFragment

class SignUpV2Activity : AppCompatActivity() {
    lateinit var binding: ActivitySignUp2Binding
    lateinit var viewModel: OnBoardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUp2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, AppViewModelFactory()).get(OnBoardingViewModel::class.java)

        supportFragmentManager
            .beginTransaction()
            .add(binding.flContainer.id, SignupFragment())
            .commit()
    }
}