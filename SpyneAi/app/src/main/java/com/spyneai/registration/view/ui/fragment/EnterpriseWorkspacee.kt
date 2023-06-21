package com.spyneai.registration.view.ui.fragment

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
import com.spyneai.databinding.BeforeLoginBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureIdentity
import com.spyneai.registration.viewmodels.RegistrationViewModel
import com.spyneai.removeWhiteSpace


class EnterpriseWorkspacee : Fragment() {

    lateinit var binding: BeforeLoginBinding
    private val viewModel: RegistrationViewModel by viewModels()
    private var loginType = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BeforeLoginBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.arguments?.apply {
            loginType = this.getString("reqOTP","sign_up_flow")!!
        }
        initView()
    }

    private fun initView() {
        clickListener()
    }

    private fun clickListener() {
        binding.apply {

            btSentOtp.setOnClickListener {
                val map = HashMap<String, String>()
                if(!removeWhiteSpace(binding.edtEnterpriseCode.text.toString()).isNullOrEmpty() && !binding.edtEmail.text.isNullOrEmpty() && Utilities.isValidEmailNew(
                        edtEmail.text.toString().trim()) ) {
                    map["referral_code"] = binding.edtEnterpriseCode.text.toString()
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.ENTERPRISE_CODE,
                        binding.edtEnterpriseCode.text.toString().trim()
                    )
                    map["email"] = binding.edtEmail.text.toString()
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.ID,
                        binding.edtEmail.text.toString().trim()
                    )
//                    findNavController().navigate(R.id.action_JoinWorkspace_to_nav_phone_otp_verify_fragment)
                    reqOtp()
                }
                else if ( removeWhiteSpace(binding.edtEnterpriseCode.text.toString()).isNullOrEmpty()) {
                    binding.edtEnterpriseCode.error = "Please enter Coupon Code"
                }
                else if(binding.edtEmail.text.isNullOrEmpty()) {
                    binding.edtEmail.error = "Please enter Email"
                }

            }

        }
    }

    private fun reqOtp() {
        binding.btSentOtp.isClickable = false
        val properties = HashMap<String, Any?>()
            .apply {
                this["email"] = id
            }

        requireContext().captureEvent(Events.OTP_LOGIN_INTIATED, properties)
        Utilities.showProgressDialog(requireContext())
        viewModel.reqOtp("login_flow")
        observeReqOtp()
    }


    private fun observeReqOtp() {
        val bundle = Bundle()
        bundle.putString("reqOtp","sign_up_enterprise_flow")
        val properties = HashMap<String, Any?>()
        viewModel.reqOtpResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    properties.apply {
                        this["email/phone"] = binding.edtEmail.text
                    }
                    requireContext().captureEvent(Events.OTP_LOGIN_SUCCEED, properties)
                    requireContext().captureIdentity(it.value.userId, properties)

                    binding.btSentOtp.isClickable = true
                    Toast.makeText(requireContext(), "OTP sent!", Toast.LENGTH_SHORT).show()

                    if (TextUtils.isDigitsOnly(binding.edtEmail.text))
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.PHONE_NUMBER, binding.edtEmail.text.toString()
                        )
                    else
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.EMAIL_ID, binding.edtEmail.text.toString()
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

                    try {
                        // send user to enter otp fragment
                        findNavController().navigate(R.id.action_JoinWorkspace_to_nav_phone_otp_verify_fragment,bundle)
                        viewModel.reqOtpSuccess.value = true
                    }catch (e: Exception){

                    }
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


