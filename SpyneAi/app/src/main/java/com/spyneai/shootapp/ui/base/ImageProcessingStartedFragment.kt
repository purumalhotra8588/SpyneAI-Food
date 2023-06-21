package com.spyneai.shootapp.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.*
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.ui.visible
import com.spyneai.databinding.FragmentImageProcessingStartedBinding
import com.spyneai.needs.AppConstants
import com.spyneai.shootapp.data.ProcessViewModelApp


class ImageProcessingStartedFragment : BaseFragment<ProcessViewModelApp, FragmentImageProcessingStartedBinding>()  {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireContext().isInternetActive()){
            arguments?.let {
                if (it.getString(AppConstants.CATEGORY_ID) == AppConstants.CARS_CATEGORY_ID){
                    if (getString(R.string.app_name) == AppConstants.SPYNE_AI || getString(R.string.app_name) == AppConstants.SPYNE_AI_AUTOMOBILE){
//                        Glide.with(this).asGif().load(R.raw.image_processing_started)
//                            .into(binding.ivProcessing)
                    }else {
                        Glide.with(this).load(R.drawable.app_logo)
                            .into(binding.ivProcessing)
                    }
                }else{
                    Glide.with(this).load(R.drawable.app_logo)
                        .into(binding.ivProcessing)
                }
            }
        }else {

            binding.apply {
                tvTitle.text = getString(R.string.images_saved)
                tvMessage.visible(true)
            }

            Glide.with(this).load(R.drawable.app_logo)
                .into(binding.ivProcessing)
        }


        binding.llHome.setOnClickListener {
            requireContext().gotoHome()
        }
    }

    override fun getViewModel() = ProcessViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentImageProcessingStartedBinding.inflate(inflater, container, false)
}