package com.spyneai.registration.view.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.InvitationFragBinding
import com.spyneai.reshoot.PreferenceListAdapter


class InvitationFragment : BaseFragment<DashboardViewModel, InvitationFragBinding>(),
    OnItemClickListener {

    private var preferencelistadapter : PreferenceListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inviteList()
        listeners()

    }

    private fun listeners(){
        binding.btInviteLink.setOnClickListener {

            viewModel.replacemanageAdd.value=true

        }
        binding.ivBackGif.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun inviteList(){
        try {
//            val list =
//            preferencelistadapter = PreferenceListAdapter(list,this
            binding.tvInviting.visibility = View.VISIBLE
            binding.vectorTwo.visibility = View.VISIBLE
            binding.rvInviteList.apply {
                layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
                adapter = preferencelistadapter
            }
        }catch (e : Exception){
        }
    }
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )  = InvitationFragBinding.inflate(inflater, container, false)

    override fun getViewModel() = DashboardViewModel::class.java

    override fun onItemClick(view: View, position: Int, data: Any?) {

    }



}