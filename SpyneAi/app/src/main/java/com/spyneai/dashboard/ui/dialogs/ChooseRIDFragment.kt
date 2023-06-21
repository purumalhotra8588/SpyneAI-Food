package com.spyneai.dashboard.ui.dialogs

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.data.model.LayoutHolder
import com.spyneai.dashboard.data.model.RidResponse
import com.spyneai.dashboard.ui.adapters.RidAdapter
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentChooseRIDBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities


class ChooseRIDFragment : BaseDialogFragment<DashboardViewModel, FragmentChooseRIDBinding>(){
    lateinit var btnlistener: RidAdapter.BtnClickListener
    lateinit var ridAdapter: RidAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getRID()

        binding.ivClose.setOnClickListener {
            dismiss()
        }



    }

    private fun getRID() {

        Utilities.showProgressDialog(requireContext())

        viewModel?.getRestaurantList(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString()
        )

        viewModel?.ridResponse?.observe(this) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    binding.tvRID.visibility=View.VISIBLE
                    binding.svRID.visibility=View.VISIBLE
                    if (!it.value.data.isNullOrEmpty()) {
                        ridAdapter = RidAdapter(requireContext(),
                            it.value.data as ArrayList<RidResponse.Data>,
                            object : RidAdapter.BtnClickListener {
                                override fun onBtnClick(position: Int) {

                                    LayoutHolder.categoryPosition = position

                                    try {
                                        Utilities.savePrefrence(
                                            requireContext(),
                                            AppConstants.ENTITY_ID,
                                            it.value.data[position].entity_id
                                        )
                                        Utilities.savePrefrence(
                                            requireContext(),
                                            AppConstants.ENTITY_NAME,
                                            it.value.data[position].entity_name
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                    viewModel.isRIDSelected.value=true

                                    dismiss()
                                }

                            })

                        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        binding.rvRID.layoutManager = layoutManager
                        binding.rvRID.adapter = ridAdapter

                    }
                }
                is Resource.Loading -> {

                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getRID() }
                }
            }
        }
    }


    override fun getViewModel() = DashboardViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentChooseRIDBinding.inflate(inflater, container, false)

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }
}