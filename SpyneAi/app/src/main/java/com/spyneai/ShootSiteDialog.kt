package com.spyneai

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentInvalidLocationDialogBinding
import com.spyneai.databinding.FragmentShootSiteDialogBinding


class ShootSiteDialog : com.spyneai.dashboard.ui.base.BaseDialogFragment<DashboardViewModel, FragmentShootSiteDialogBinding>() {
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )  = FragmentShootSiteDialogBinding.inflate(inflater, container, false)


    override fun getViewModelClass() = DashboardViewModel::class.java


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true

        binding.btCamera.setOnClickListener {

            viewModel.isStartAttendance.value = true
            dismiss()
        }
    }




    override fun onResume() {
        super.onResume()


        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.getWindow()?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}