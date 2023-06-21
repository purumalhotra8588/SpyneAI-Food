package com.spyneai.shootapp.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogExitAppBinding
import com.spyneai.gotoHome
import com.spyneai.reshoot.data.SelectedImagesHelper
import com.spyneai.setLocale
import com.spyneai.shootapp.data.ShootViewModelApp

class ShootExitDialog : BaseDialogFragment<ShootViewModelApp, DialogExitAppBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshText()

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.btnYes.setOnClickListener {
            requireContext().gotoHome()
            SelectedImagesHelper.selectedOverlayIds.clear()
            SelectedImagesHelper.selectedImages.clear()
            dismiss()
        }
    }


    fun refreshText(){
        requireContext().setLocale()
        binding.tvSkuNameDialog.text=getString(R.string.exits)
        binding.btnNo.text=getString(R.string.no)
        binding.btnYes.text=getString(R.string.yes)

    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogExitAppBinding.inflate(inflater, container, false)
}