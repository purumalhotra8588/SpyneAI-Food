package com.spyneai

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentInfoDialogBinding
import com.spyneai.service.ServerSyncTypes
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.ui.dialogs.ConfirmTagsDialog


class InfoDialog :  BaseDialogFragment<ShootViewModelApp, FragmentInfoDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvEndProject.setOnClickListener {
            if (viewModel.fromDrafts){
                viewModel.stopShoot.value = true
            }else {
                if (viewModel.isStopCaptureClickable)
                    viewModel.stopShoot.value = true
            }
            if(viewModel.shootList.value?.size==1){

                requireContext().startUploadingService(
                    ConfirmTagsDialog::class.java.simpleName,
                    ServerSyncTypes.UPLOAD
                )
            }

            dismiss()
        }

        binding.llShootInfo.setOnClickListener {
//            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
//            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
//            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
            viewModel.categoryDetails.value?.imageType = "Info"
            viewModel.hideLeveler.value = true
            viewModel.showGrid.value=false
            viewModel.imageTypeInfo.value=true
            viewModel.showLeveler.value = false

            if(viewModel.shootList.value?.size==1){

                requireContext().startUploadingService(
                    ConfirmTagsDialog::class.java.simpleName,
                    ServerSyncTypes.UPLOAD
                )
            }

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


    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentInfoDialogBinding.inflate(inflater, container, false)
}