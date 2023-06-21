package com.spyneai.registration.view.ui.fragment.signup

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSelectedBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureIdentity
import com.spyneai.registration.viewmodels.RegistrationViewModel


class SelectedFragment : Fragment() {

    lateinit var binding: FragmentSelectedBinding
    private val viewModel: RegistrationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View  {
        // Inflate the layout for this fragment
        binding = FragmentSelectedBinding.inflate(inflater, container, false)

        binding.tvCountryCode.registerPhoneNumberTextView(binding.edtPhoneNumber)


        initView()
        return binding.root
    }

    private fun initView() {
        clickListener()
    }

    private fun clickListener() {
        binding.apply {
            btSentOtp.setOnClickListener {
                val map = java.util.HashMap<String, String>()
                val bundle = Bundle()
                if (!edtPhoneNumber.text.isNullOrEmpty()) {
                    when {
                        edtPhoneNumber.text.isNullOrEmpty()
                        -> edtPhoneNumber.error = "Please Enter Phone Number"
                        TextUtils.isDigitsOnly(binding.edtPhoneNumber.text) -> {
                            if (!tvCountryCode.isValid) {
                                edtPhoneNumber.error = "Please Enter Valid Phone Number"
                            } else {
                                bundle.putString("reqOTP","sign_up_enterprise_flow")
                                if(binding.btSentOtp.text==getString(R.string.contiune)){
                                    Utilities.savePrefrence(
                                        requireContext(),
                                        AppConstants.PHONE_NUMBER, binding.edtPhoneNumber.text.toString()
                                    )


                                    Navigation.findNavController(
                                        binding.root
                                    ).navigate(R.id.action_fragment_selected_to_user_info_fragment,bundle)

                                    // Have to use after fixing on backend side
//                                    map["phone"] =  binding.edtPhoneNumber.text.toString()
//                                    Utilities.showProgressDialog(requireContext())
//                                    viewModel.updateUserDetail(map = map)
//                                    observeUpdatePhone(map,bundle)
                                }else{
                                    reqOtp(edtPhoneNumber.text.toString().trim(), bundle)
                                }


                            }
                        }
                        !TextUtils.isDigitsOnly(edtPhoneNumber.text) -> {
                            edtPhoneNumber.error = "Please Enter Valid Phone Number"
                        }
                        else -> {
                            bundle.putString("reqOTP","sign_up_enterprise_flow")

                            if(binding.btSentOtp.text==getString(R.string.contiune)){
                                Utilities.savePrefrence(
                                    requireContext(),
                                    AppConstants.PHONE_NUMBER, binding.edtPhoneNumber.text.toString()
                                )
//                                map["phone"] =  binding.edtPhoneNumber.text.toString()
//                                Utilities.showProgressDialog(requireContext())
//                                viewModel.updateUserDetail(map = map)
//                                observeUpdatePhone(map,bundle)

                                Navigation.findNavController(
                                    binding.root
                                ).navigate(R.id.action_fragment_selected_to_user_info_fragment,bundle)
                            }else{
                                reqOtp(edtPhoneNumber.text.toString().trim(), bundle)
                            }
                        }
                    }
                }else{
                    edtPhoneNumber.error = "Please Enter Phone Number"
                }
            }
            tvSkip.setOnClickListener {
                findNavController().navigate(R.id.action_fragment_selected_to_user_info_fragment)
            }

            binding.tvCountryCode.setOnCountryChangeListener { selectedCountry ->

                if(selectedCountry.phoneCode=="91") {
                    btSentOtp.text=getString(R.string.send_otp)
                    tvCreateYour.text=getString(R.string.validate_phone)
                    tvValiate.visibility=View.VISIBLE


                }else{
                    btSentOtp.text=getString(R.string.contiune)
                    tvCreateYour.text=getString(R.string.enter_phone_number)
                    tvValiate.visibility=View.GONE
                }
            }


        }

    }

    private fun reqOtp(details: String, bundle: Bundle) {
        binding.btSentOtp.isClickable = false
        val properties = HashMap<String, Any?>()
            .apply {
                this["email/phone"] = id
            }

        requireContext().captureEvent(Events.OTP_LOGIN_INTIATED, properties)
        Utilities.showProgressDialog(requireContext())

        Utilities.savePrefrence(
            requireContext(),
            AppConstants.ID,
            binding.edtPhoneNumber.text.toString()
        )
        Utilities.saveBool(
            requireContext(),
            AppConstants.VERIFICATION,
            true
        )

        viewModel.reqOtp("sign_up_flow")
        observeReqOtp(bundle)
    }

    private fun observeReqOtp(bundle: Bundle) {
        val properties = HashMap<String, Any?>()
        viewModel.reqOtpResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    properties.apply {
                        this["email/phone"] = binding.edtPhoneNumber.text
                    }
                    requireContext().captureEvent(Events.OTP_LOGIN_SUCCEED, properties)
                    requireContext().captureIdentity(it.value.userId, properties)
                    binding.btSentOtp.isClickable = true
                    if(binding.btSentOtp.text!=getString(R.string.contiune))
                        Toast.makeText(requireContext(), "OTP sent!", Toast.LENGTH_SHORT).show()

                    if (TextUtils.isDigitsOnly(binding.edtPhoneNumber.text))
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.PHONE_NUMBER, binding.edtPhoneNumber.text.toString()
                        )
                    else
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.EMAIL_ID, binding.edtPhoneNumber.text.toString()
                        )

                    Navigation.findNavController(
                        binding.root
                    ).navigate(R.id.action_fragment_selected_to_validate_otp_, bundle)


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
                else -> {
                }
            }
        }
    }

//    private fun observeUpdatePhone(map: java.util.HashMap<String, String>,bundle: Bundle) {
//        viewModel.updateUserDetail.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
//                    Utilities.hideProgressDialog()
//                    Navigation.findNavController(
//                        binding.root
//                    ).navigate(R.id.action_fragment_selected_to_user_info_fragment,bundle)
//                }
//
//                is Resource.Failure -> {
//                    Utilities.hideProgressDialog()
//                    handleApiError(it) {
//                        viewModel.updateUserDetail(map)
//                    }
//                }
//                else -> {
//
//                }
//            }
//        }
//    }

}