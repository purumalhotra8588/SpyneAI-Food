package com.spyneai.threesixty.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentRequest360DialogBinding
import com.spyneai.processedimages.data.ProcessedViewModelApp

class Request360Dialog : BaseDialogFragment<ProcessedViewModelApp, FragmentRequest360DialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnRequest.setOnClickListener {
            viewModel.showSpyne360.value=true
            dismiss()
        }


    }

    override fun getViewModel() = ProcessedViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRequest360DialogBinding.inflate(inflater, container, false)
}