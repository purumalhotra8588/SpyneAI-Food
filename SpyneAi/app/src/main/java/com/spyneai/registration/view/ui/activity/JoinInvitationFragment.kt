package com.spyneai.orders.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.ExperimentalPagingApi
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.JoinInvitationBinding
import com.spyneai.setLocale

@ExperimentalPagingApi
class JoinInvitationFragment : BaseFragment<DashboardViewModel,JoinInvitationBinding>() {



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().setLocale()

        listeners()
    }

    private fun listeners(){
        binding.btJoinTeam.setOnClickListener {

            viewModel.replaceJoinDetails.value=true

        }
        binding.btNotNow.setOnClickListener {
            
        }
    }


    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )  = JoinInvitationBinding.inflate(inflater, container, false)




    private fun setText(){


    }

    override fun getViewModel() = DashboardViewModel::class.java

}




