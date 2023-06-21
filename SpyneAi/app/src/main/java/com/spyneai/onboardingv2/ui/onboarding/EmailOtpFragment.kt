package com.spyneai.onboardingv2.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureIdentity
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentEmailOtpBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboarding.data.viewmodels.OnBoardingViewModel
import com.spyneai.posthog.Events
import java.util.HashMap

class EmailOtpFragment :  BaseFragment<OnBoardingViewModel, FragmentEmailOtpBinding>() {

    override fun getViewModel() = OnBoardingViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEmailOtpBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeners()
    }

    private fun listeners() {
        binding.flPhone.setOnClickListener {
            Intent(requireContext(),LoginWithPhoneActivity::class.java)
                .apply { startActivity(this) }
        }

        binding.tvSignup.setOnClickListener {
            Intent(requireContext(),SignUpV2Activity::class.java)
                .apply { startActivity(this) }
        }

        binding.tvUsePassword.setOnClickListener {
            viewModel.usePassword.value = true
        }

        binding.btnRequestOtp.setOnClickListener {
            when {
                binding.etId.text.isNullOrEmpty()
                -> binding.etId.error = "Please enter email or phone number"

                TextUtils.isDigitsOnly(binding.etId.text) -> {
                    if (binding.etId.length() != 10) {
                        binding.etId.error = "Please enter valid phone number"
                    } else {
                        reqOtp(binding.etId.text.toString().trim())
                    }
                }
                !TextUtils.isDigitsOnly(binding.etId.text) -> {
                    if (!Utilities.isValidEmail(binding.etId.text.toString().trim()))
                        binding.etId.error = "Please enter valid email id"
                    else {
                        reqOtp(binding.etId.text.toString().trim())
                    }
                }
                else -> {
                    reqOtp(binding.etId.text.toString().trim())
                }
            }
        }
    }

    private fun reqOtp(id: String) {
        viewModel.reqOtpSuccess.value = true
//        binding.btRequestOtp.isClickable = false
//        val properties = HashMap<String, Any?>()
//            .apply {
//                this.put("email/phone", id)
//            }
//
//        requireContext().captureEvent(Events.OTP_LOGIN_INTIATED, properties)
//        Utilities.showProgressDialog(requireContext())
//
//        Utilities.savePrefrence(requireContext(),AppConstants.ID,binding.etId.text.toString())
//        viewModel.reqOtp()
//        observeReqOtp()
    }

    private fun observeReqOtp() {

        val properties = HashMap<String, Any?>()
        viewModel.reqOtpResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    properties.apply {
                        this["email/phone"] = binding.etId.text
                    }
                    requireContext().captureEvent(Events.OTP_LOGIN_SUCCEED, properties)
                    requireContext().captureIdentity(it.value.userId, properties)

                    binding.btnRequestOtp.isClickable = true
                    Toast.makeText(requireContext(), "OTP sent!", Toast.LENGTH_SHORT).show()

                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.EMAIL_ID, binding.etId.text.toString()
                        )

                    // send user to enter otp fragment
                    viewModel.reqOtpSuccess.value = true
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                    requireContext().captureEvent(Events.OTP_LOGIN_FAILED, properties)
                    binding.btnRequestOtp.isClickable = true
//                    handleApiError(it)
                    Toast.makeText(
                        requireContext(),
                        "OTP sent failed: " + it.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {

                }
            }
        })
    }


}