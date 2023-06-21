package com.spyneai

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.FragmentInvalidLocationDialogBinding

class InvalidLocationDialog : com.spyneai.dashboard.ui.base.BaseDialogFragment<DashboardViewModel, FragmentInvalidLocationDialogBinding>() {
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )  = FragmentInvalidLocationDialogBinding.inflate(inflater, container, false)

    override fun getViewModelClass() = DashboardViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true

        binding.btOk.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.getWindow()?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}