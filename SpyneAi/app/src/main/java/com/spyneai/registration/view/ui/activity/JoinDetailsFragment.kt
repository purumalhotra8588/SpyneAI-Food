package com.spyneai.orders.ui.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.databinding.FragmentMyOrdersBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.FilterType
import com.spyneai.orders.ui.adapter.OrdersSlideAdapter
import com.spyneai.setLocale
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.JoinDetailsBinding
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureIdentity
import com.spyneai.registration.view.ui.activity.RegistrationBaseActivity
import com.spyneai.registration.viewmodels.RegistrationViewModel
import java.util.HashMap


class JoinDetailsFragment : BaseFragment<DashboardViewModel, JoinDetailsBinding>() {



    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )  = JoinDetailsBinding.inflate(inflater, container, false)



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().setLocale()

        initView()

    }

    private fun initView() {
        clickListener()
    }

    private fun clickListener() {
        binding.apply {

            btSentOtp.setOnClickListener {
                val map = HashMap<String, String>()
                if(!binding.edtName.text.isNullOrEmpty() && !binding.edtEmailId.text.isNullOrEmpty() && Utilities.isValidEmailNew(
                        edtEmailId.text.toString().trim()) ) {
                    map["edtName"] = binding.edtName.text.toString()
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_NAME,
                        binding.edtName.text.toString()
                    )
                    map["email"] = binding.edtEmailId.text.toString()
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.ID,
                        binding.edtEmailId.text.toString()
                    )
//                    reqOtp()
                    viewModel.replaceJoinOtp.value=true
                }
                else if ( binding.edtName.text.isNullOrEmpty()) {
                    binding.edtName.error = "Please enter Name"
                }
                else if(binding.edtEmailId.text.isNullOrEmpty()) {
                    binding.edtEmailId.error = "Please enter Email"
                }



            }



        }
    }
//    private fun reqOtp() {
//        binding.btSentOtp.isClickable = false
//        val properties = HashMap<String, Any?>()
//            .apply {
//                this["email"] = id
//            }
//
//        requireContext().captureEvent(Events.OTP_LOGIN_INTIATED, properties)
//        Utilities.showProgressDialog(requireContext())
//        viewModel.reqOtp("login_flow")
//        observeReqOtp()
//    }
//
//
//    private fun observeReqOtp() {
//        val bundle = Bundle()
//        bundle.putString("reqOtp","sign_up_enterprise_flow")
//        val properties = HashMap<String, Any?>()
//        viewModel.reqOtpResponse.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
//                    Utilities.hideProgressDialog()
//                    properties.apply {
//                        this["email/phone"] = binding.edtEmailId.text
//                    }
//                    requireContext().captureEvent(Events.OTP_LOGIN_SUCCEED, properties)
//                    requireContext().captureIdentity(it.value.userId, properties)
//
//                    binding.btSentOtp.isClickable = true
//                    Toast.makeText(requireContext(), "OTP sent!", Toast.LENGTH_SHORT).show()
//
//                    if (TextUtils.isDigitsOnly(binding.edtEmailId.text))
//                        Utilities.savePrefrence(
//                            requireContext(),
//                            AppConstants.PHONE_NUMBER, binding.edtEmailId.text.toString()
//                        )
//                    else
//                        Utilities.savePrefrence(
//                            requireContext(),
//                            AppConstants.EMAIL_ID, binding.edtEmailId.text.toString()
//                        )
//
//                    Utilities.savePrefrence(
//                        requireContext(),
//                        AppConstants.ENTERPRISE_DISCOUNT,
//                        it.value.discount.toString()
//                    )
//                    Utilities.savePrefrence(
//                        requireContext(),
//                        AppConstants.PRICE_PER_CREDIT,
//                        it.value.price_per_credit.toString()
//                    )
//
//                    // send user to enter otp fragment
//                    findNavController().navigate(R.id.action_JoinWorkspace_to_nav_phone_otp_verify_fragment,bundle)
//                    viewModel.reqOtpSuccess.value = true
//                }
//                is Resource.Failure -> {
//                    Utilities.hideProgressDialog()
//                    handleApiError(it)
//                    requireContext().captureEvent(Events.OTP_LOGIN_FAILED, properties)
//                    binding.btSentOtp.isClickable = true
////                    handleApiError(it)
//                    Toast.makeText(
//                        requireContext(),
//                        "OTP sent failed: " + it.errorMessage,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                else -> {}
//            }
//        }
//    }




    override fun getViewModel() = DashboardViewModel::class.java

}