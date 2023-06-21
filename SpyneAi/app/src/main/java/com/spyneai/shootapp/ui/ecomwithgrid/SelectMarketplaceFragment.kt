package com.spyneai.shootapp.ui.ecomwithgrid

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentSelectMarketplaceBinding
import com.spyneai.shootapp.data.ShootViewModelApp


class SelectMarketplaceFragment : BaseFragment<ShootViewModelApp, FragmentSelectMarketplaceBinding>() {




    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectMarketplaceBinding.inflate(inflater, container, false)


}