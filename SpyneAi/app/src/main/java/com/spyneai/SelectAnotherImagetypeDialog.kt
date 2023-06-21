package com.spyneai

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentSelectAnotherImagetypeBinding
import com.spyneai.shootapp.data.ShootViewModelApp

class SelectAnotherImagetypeDialog : BaseDialogFragment<ShootViewModelApp, FragmentSelectAnotherImagetypeBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding?.ivClose?.setOnClickListener {
            dismiss()
        }

        binding.tvProductShoot.setOnClickListener {
            viewModel.categoryDetails.value?.imageType = "Ecom"
            if (viewModel.categoryDetails.value?.categoryName.equals("E-Commerce") || viewModel.categoryDetails.value?.categoryName.equals("Food & Beverages"))
            {
//                viewModel.showLeveler.value = true
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
                viewModel.addMoreAngle.value = true
            }

            dismiss()
        }

        binding.tvInfoShoot.setOnClickListener {
            viewModel.categoryDetails.value?.imageType = "Info"
            viewModel.hideLeveler.value = true
            viewModel.showGrid.value = false
            viewModel.showLeveler.value = false
            viewModel.imageTypeInfo.value=true
//            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
//            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
//            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
            viewModel.addMoreAngle.value = true

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

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectAnotherImagetypeBinding.inflate(inflater, container, false)

    override fun getViewModel() = ShootViewModelApp::class.java
}