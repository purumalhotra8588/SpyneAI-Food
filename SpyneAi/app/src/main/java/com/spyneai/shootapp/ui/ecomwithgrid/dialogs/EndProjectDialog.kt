package com.spyneai.shootapp.ui.ecomwithgrid.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.EndProjectDialogBinding
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.utils.log


class EndProjectDialog : BaseDialogFragment<ShootViewModelApp, EndProjectDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        viewModel.projectApp?.let {
            binding.apply {
                pbEndProject.visibility = View.GONE
                btYes.isEnabled = true

                tvProjectName.text = it.projectName
                tvTotalSkuCaptured.text = it.skuCount.toString()
                tvTotalImageCaptured.text = it.imagesCount.toString()
            }
        }

        binding.btNo.setOnClickListener {
            dismiss()
            log("end project dialog dismiss- NO")
        }

        binding.btYes.setOnClickListener {
            viewModel.showProjectDetail.value = true
            dismiss()
        }

        binding.ivCloseDialog.setOnClickListener {
            dismiss()
            log("end project dialog dismiss- Image")
        }

    }

    override fun onStop() {
        super.onStop()
        log("onStop(EndProjectDialog) called")
        dismissAllowingStateLoss()
    }


    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
        dialog?.window?.setGravity(Gravity.BOTTOM)
        getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.WHITE));
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = EndProjectDialogBinding.inflate(inflater, container, false)

}