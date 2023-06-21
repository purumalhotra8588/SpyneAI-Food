package com.spyneai.processedimages.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogProcessedImageBinding
import com.spyneai.processedimages.data.ProcessedViewModelApp

class ProcessedImageDialog : BaseDialogFragment<ProcessedViewModelApp, DialogProcessedImageBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext()) // replace with 'this' if it's in activity
            .load(viewModel.selectedImageUrl!!)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(binding.ivProcessed)

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()

        //getDialog()?.requestWindowFeature(Window.FEATURE_NO_TITLE);FEATURE_NO_TITLE
        getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

    }

    override fun getViewModel() = ProcessedViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogProcessedImageBinding.inflate(inflater, container, false)
}