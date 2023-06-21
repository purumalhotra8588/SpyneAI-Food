package com.spyneai

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentCropConfirmDialogBinding
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.service.ServerSyncTypes
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject


class CropConfirmDialog : BaseDialogFragment<ShootViewModelApp, FragmentCropConfirmDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        val uri = viewModel.shootData.value?.capturedImage
//        binding.ivInfoCroppedImage.setRotation(90F)

        viewModel.end.value = System.currentTimeMillis()
        val difference = (viewModel.end.value!! - viewModel.begin.value!!)/1000.toFloat()
        log("dialog- "+difference)

        Glide.with(requireContext())
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.ivInfoCroppedImage)

        log("Image set to dialog: " + uri)

        binding.tvEndProject.setOnClickListener {
            viewModel.imageTypeInfo.value=false
            viewModel.onImageConfirmed.value = true
            viewModel.isStopCaptureClickable = true
            val properties = HashMap<String,Any?>()
            val cameraSetting = viewModel.getCameraSetting()
            properties.apply {
                put("image_data",JSONObject().apply {
                    put("sku_id",viewModel.shootData.value?.sku_id)
                    put("project_id",viewModel.shootData.value?.project_id)
                    put("image_type",viewModel.shootData.value?.image_category)
                    put("sequence",viewModel.shootData.value?.sequence)
                    put("name",viewModel.shootData.value?.name)
                    put("angle",viewModel.shootData.value?.angle)
                    put("overlay_id",viewModel.shootData.value?.overlayId)
                    put("debug_data",viewModel.shootData.value?.debugData)
                }.toString())
                put("camera_setting",JSONObject().apply {
                    put("is_overlay_active",cameraSetting.isOverlayActive)
                    put("is_grid_active",cameraSetting.isGridActive)
                    put("is_gyro_active",cameraSetting.isGryroActive)
                })
            }

            requireContext().captureEvent(
                Events.CONFIRMED,
                properties
            )

            viewModel.isCameraButtonClickable = true


            GlobalScope.launch(Dispatchers.IO) {
                viewModel.
                insertImage(viewModel.shootData.value!!)
            }

            requireContext().startUploadingService(
                CropConfirmDialog::class.java.simpleName,
                ServerSyncTypes.UPLOAD
            )

            viewModel.stopShoot.value = true
            dismiss()
        }

        binding.llShootAnother.setOnClickListener {
            viewModel.onImageConfirmed.value = true
            //viewModel.shootNumber.value = viewModel.shootNumber.value?.plus(1)

            viewModel.isStopCaptureClickable = true
            val properties = HashMap<String,Any?>()

            val cameraSetting = viewModel.getCameraSetting()
            properties.apply {
                put("image_data",JSONObject().apply {
                    put("sku_id",viewModel.shootData.value?.sku_id)
                    put("project_id",viewModel.shootData.value?.project_id)
                    put("image_type",viewModel.shootData.value?.image_category)
                    put("sequence",viewModel.shootData.value?.sequence)
                    put("name",viewModel.shootData.value?.name)
                    put("angle",viewModel.shootData.value?.angle)
                    put("overlay_id",viewModel.shootData.value?.overlayId)
                    put("debug_data",viewModel.shootData.value?.debugData)
                }.toString())
                put("camera_setting",JSONObject().apply {
                    put("is_overlay_active",cameraSetting.isOverlayActive)
                    put("is_grid_active",cameraSetting.isGridActive)
                    put("is_gyro_active",cameraSetting.isGryroActive)
                })
            }


            requireContext().captureEvent(
                Events.CONFIRMED,
                properties
            )

            viewModel.isCameraButtonClickable = true


            GlobalScope.launch(Dispatchers.IO) {
                viewModel.
                insertImage(viewModel.shootData.value!!)
            }

            requireContext().startUploadingService(
                CropConfirmDialog::class.java.simpleName,
                ServerSyncTypes.UPLOAD
            )
            dismiss()
        }
    }




    override fun onResume() {
        super.onResume()

        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }


    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCropConfirmDialogBinding.inflate(inflater, container, false)
}