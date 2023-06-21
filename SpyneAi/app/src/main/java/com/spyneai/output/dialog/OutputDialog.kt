package com.spyneai.output.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentOutputBinding
import com.spyneai.shootapp.data.ShootViewModelApp


class OutputDialog : BaseDialogFragment<ShootViewModelApp, FragmentOutputBinding>() {
    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOutputBinding.inflate(inflater, container, false)

}