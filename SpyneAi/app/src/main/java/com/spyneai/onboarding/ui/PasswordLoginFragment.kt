package com.spyneai.onboarding.ui

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
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentLoginWithPasswordBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboarding.data.viewmodels.OnBoardingViewModel
import com.spyneai.onboardingv2.ui.intro.ChooseCategoryActivity
import com.spyneai.onboardingv2.ui.onboarding.ReferralFragment
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureIdentity


class PasswordLoginFragment : BaseFragment<OnBoardingViewModel, FragmentLoginWithPasswordBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeners()
    }

    private fun listeners() {
        binding.btLogin.setOnClickListener {

            when {
                binding.etId.text.isNullOrEmpty()
                -> binding.etId.error = "Please enter email or phone number"

                binding.etPassword.text.isNullOrEmpty()
                        -> binding.etPassword.error = "Please enter password"
                TextUtils.isDigitsOnly(binding.etId.text) -> {
                    if (binding.etId.length() != 10) {
                        binding.etId.error = "Please enter valid phone number"
                    } else {
                        login()
                    }
                }
                !TextUtils.isDigitsOnly(binding.etId.text) -> {
                    if (!Utilities.isValidEmail(binding.etId.text.toString().trim()))
                        binding.etId.error = "Please enter valid email id"
                    else {
                        login()
                    }
                }
                else -> {
                    login()
                }
            }
        }

        binding.tvUseOtp.setOnClickListener {
            when {
                binding.etId.text.isNullOrEmpty()
                -> binding.etId.error = "Please enter phone number or email"

                TextUtils.isDigitsOnly(binding.etId.text) -> {
                    if (binding.etId.length() != 10) {
                        binding.etId.error = "Please enter valid phone number"
                    } else {
                        reqOtp(binding.etId.text.toString().trim())
                        binding.tvLabel.isClickable = false
                    }
                }
                !TextUtils.isDigitsOnly(binding.etId.text) -> {
                    if (!Utilities.isValidEmail(binding.etId.text.toString().trim()))
                        binding.etId.error = "Please enter valid email id"
                    else {
                        reqOtp(binding.etId.text.toString().trim())
                        binding.tvLabel.isClickable = false
                    }
                }
            }
        }
    }

    private fun reqOtp(id: String) {
        binding.tvUseOtp.isClickable = false
        val properties = HashMap<String, Any?>()
            .apply {
                this.put("email/phone", id)
            }

        requireContext().captureEvent(Events.OTP_LOGIN_INTIATED, properties)
        Utilities.showProgressDialog(requireContext())

        Utilities.savePrefrence(requireContext(),AppConstants.ID,binding.etId.text.toString())
        viewModel.reqOtp()
        observeReqOtp()
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

                    binding.tvUseOtp.isClickable = true
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
                    binding.tvUseOtp.isClickable = true
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

    private fun login() {
        loginWithPassword(
            binding.etId.text.toString().trim(),
            binding.etPassword.text.toString().trim()
        )
        binding.btLogin.isClickable = false
        binding.tvUseOtp.isClickable = false
    }

    private fun loginWithPassword(id: String, password: String) {

        val properties = HashMap<String, Any?>()
            .apply {
                this.put("email/phone", id)
            }

        requireContext().captureEvent(Events.LOGIN_INTIATED, properties)
        Utilities.showProgressDialog(requireContext())

        val data: MutableMap<String, Any?> = HashMap()
        data["api_key"] = WhiteLabelConstants.API_KEY
        data["password"] = password
        data["strategy"] = "PASSWORD"

        if (TextUtils.isDigitsOnly(binding.etId.text))
            data["contact_no"] = id
         else
            data["email_id"] = id


        viewModel.loginWithPassword(data)

        viewModel.loginEmailPasswordResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is com.spyneai.base.network.Resource.Success -> {
                    Utilities.hideProgressDialog()
                    properties.apply {
                        this["user_id"] = it.value.user_id
                        this["name"] = it.value.user_name
                    }
                    requireContext().captureEvent(Events.LOGIN_SUCCEED, properties)
                    requireContext().captureIdentity(it.value.user_id, properties)

                    Utilities.savePrefrence(requireContext(), AppConstants.ENTERPRISE_DISCOUNT, it.value.discount.toString())
                    Utilities.savePrefrence(requireContext(), AppConstants.PRICE_PER_CREDIT, it.value.price_per_credit.toString())


                    binding.btLogin.isClickable = true
                    binding.tvUseOtp.isClickable = true

                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.AUTH_KEY,
                        it.value!!.auth_token
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.ENTERPRISE_ID,
                        it.value!!.enterprise_id
                    )


                    if (it.value.new_user == 1){
                        ReferralFragment().show(requireActivity().supportFragmentManager,"ReferralFragment")
                    }else {
                        val intent = Intent(requireContext(), ChooseCategoryActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra(AppConstants.IS_NEW_USER, false)
                        startActivity(intent)
                    }
                }
                is com.spyneai.base.network.Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    requireContext().captureEvent(Events.LOGIN_FAILED, properties)
                    binding.btLogin.isClickable = true
                    binding.tvUseOtp.isClickable = true
//                    handleApiError(it)
                    Toast.makeText(
                        requireContext(),
                        "Login failed: " + it.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {

                }
            }
        })
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLoginWithPasswordBinding.inflate(inflater, container, false)

    override fun getViewModel() = OnBoardingViewModel::class.java
}