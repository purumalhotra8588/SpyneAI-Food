package com.spyneai.draft.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentAppDraftSkuDetailsBinding

import com.spyneai.draft.data.DraftViewModel

class DraftImagesPagedFragment: BaseFragment<DraftViewModel, FragmentAppDraftSkuDetailsBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getViewModel() = DraftViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAppDraftSkuDetailsBinding.inflate(inflater, container, false)
}