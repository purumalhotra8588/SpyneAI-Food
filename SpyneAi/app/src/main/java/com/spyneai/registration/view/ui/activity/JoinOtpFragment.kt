package com.spyneai.registration.view.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.JoinInvitationBinding
import com.spyneai.databinding.JoinOtpBinding
import com.spyneai.setLocale

class JoinOtpFragment : BaseFragment<DashboardViewModel, JoinOtpBinding>(){



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

                viewModel.replaceJoinOtpPost.value=true

            }
        }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = JoinOtpBinding.inflate(inflater, container, false)



    override fun getViewModel() = DashboardViewModel::class.java


}