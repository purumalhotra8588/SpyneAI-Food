package com.spyneai.shootapp.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.DialogNoMagnatoMeterBinding

class NoMagnaotoMeterDialog : com.spyneai.dashboard.ui.base.BaseDialogFragment<DashboardViewModel, DialogNoMagnatoMeterBinding>() {

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )  = DialogNoMagnatoMeterBinding.inflate(inflater, container, false)


    override fun getViewModelClass() = DashboardViewModel::class.java


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        binding?.tvOKay?.setOnClickListener {
            viewModel.continueAnyway.value = true
            dismiss()
        }

    }

    override fun onResume() {
        super.onResume()
        if (dialog != null){
            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        }
    }
}