package com.spyneai.registration.view.ui.fragment.signin

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
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.EnterpriseStartFlowBinding
import com.spyneai.databinding.FragmentEmailWithOtpBinding
import com.spyneai.getTimeStamp
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureIdentity
import com.spyneai.registration.viewmodels.RegistrationViewModel
import kotlinx.android.synthetic.main.enterprise_start_flow.*


class EnterpriseStartFlow : Fragment() {

    lateinit var binding: EnterpriseStartFlowBinding
    private val viewModel: RegistrationViewModel by viewModels()
    private var loginType = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EnterpriseStartFlowBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        clickListener()
    }

    private fun clickListener() {
        binding.apply {
            tvCreateAccount.setOnClickListener {
                findNavController().navigate(
                    R.id.action_EnterpriseStartFlow_to_nav_phone_number_fragment2
                )

                val properties = HashMap<String,Any?>().apply {
                }
                requireContext().captureEvent(
                    Events.CREATE_YOUR_ACCOUNT_CLICKED,
                    properties
                )

            }
            binding.tvClickHere.setOnClickListener {

                val properties = HashMap<String,Any?>().apply {
                }
                requireContext().captureEvent(
                    Events.HAVE_AN_ENTERPRISE_CODE_CLICKED,
                    properties
                )

                Utilities.saveBool(requireContext(),AppConstants.NEW_LOGIN_API,false)
                findNavController().navigate(R.id.action_EnterpriseStartFlow_to_JoinWorkspace2)
            }
            binding.btSignIn.setOnClickListener {
                Utilities.saveBool(requireContext(),AppConstants.NEW_LOGIN_API,true)
                findNavController().navigate(R.id.action_EnterpriseStartFlow_to_nav_login_fragment)


                requireContext().captureEvent(
                    Events.SIGNIN_TO_YOUR_ACCOUNT_CLICKED,
                    HashMap<String,Any?>()
                )

            }
        }
    }
}


