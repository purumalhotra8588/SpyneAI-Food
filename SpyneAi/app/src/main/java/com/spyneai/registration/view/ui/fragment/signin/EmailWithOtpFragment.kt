package com.spyneai.registration.view.ui.fragment.signin

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentEmailWithOtpBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureIdentity
import com.spyneai.registration.viewmodels.RegistrationViewModel


class EmailWithOtpFragment : Fragment() {

    lateinit var binding: FragmentEmailWithOtpBinding
    private val viewModel: RegistrationViewModel by viewModels()
    private var loginType = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmailWithOtpBinding.inflate(inflater, container, false)
        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.arguments?.apply {
            loginType = this.getString("reqOTP")!!
        }
        initView()
    }

    private fun initView() {
        binding.btSentOtp.isClickable = true
        clickListener()
    }

    private fun clickListener() {
        binding.apply {
            btSentOtp.setOnClickListener {

                when {
                    etEmail.text.isNullOrEmpty()
                    -> etEmail.error = "Please enter email or phone number"

                    TextUtils.isDigitsOnly(etEmail.text) -> {
                        if (etEmail.length() != 10) {
                            etEmail.error = "Please enter valid phone number"
                        } else {
                            reqOtp()
                        }
                    }
                    !TextUtils.isDigitsOnly(etEmail.text) -> {
                        if (!Utilities.isValidEmail(etEmail.text.toString().trim()))
                            etEmail.error = "Please enter valid email id"
                        else {
                            reqOtp()
                        }
                    }
                    else -> {
                        reqOtp()
                    }
                }
            }
            tvSkip.setOnClickListener {
                findNavController().navigate(R.id.action_fragment_mobile_otp_to_choose_your_plan_fragment)
            }
        }

    }

    private fun reqOtp() {
        binding.btSentOtp.isClickable = false
        val properties = HashMap<String, Any?>()
            .apply {
                this["email/phone"] = id
            }

        requireContext().captureEvent(Events.OTP_LOGIN_INTIATED, properties)
        Utilities.showProgressDialog(requireContext())
        viewModel.reqOtp("sign_up_flow")
        observeReqOtp()
    }

    private fun observeReqOtp() {
        val bundle = Bundle()
        bundle.putString("reqOtp","login_flow")
        val properties = HashMap<String, Any?>()
        viewModel.reqOtpResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    properties.apply {
                        this["email/phone"] = binding.etEmail.text
                    }
                    requireContext().captureEvent(Events.OTP_LOGIN_SUCCEED, properties)
                    requireContext().captureIdentity(it.value.userId, properties)

                    binding.btSentOtp.isClickable = true
                    Toast.makeText(requireContext(), "OTP sent!", Toast.LENGTH_SHORT).show()

                    if (TextUtils.isDigitsOnly(binding.etEmail.text))
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.PHONE_NUMBER, binding.etEmail.text.toString()
                        )
                    else
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.EMAIL_ID, binding.etEmail.text.toString()
                        )


                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.ENTERPRISE_DISCOUNT,
                        it.value.discount.toString()
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.PRICE_PER_CREDIT,
                        it.value.price_per_credit.toString()
                    )

                    // send user to enter otp fragment
                    findNavController().navigate(R.id.action_fragment_mobile_otp_to_verify_otp,bundle)
                    viewModel.reqOtpSuccess.value = true
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                    requireContext().captureEvent(Events.OTP_LOGIN_FAILED, properties)
                    binding.btSentOtp.isClickable = true
//                    handleApiError(it)
                    Toast.makeText(
                        requireContext(),
                        "OTP sent failed: " + it.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }
}