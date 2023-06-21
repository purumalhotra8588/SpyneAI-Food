package com.spyneai.shoot.ui.ecomwithgrid.dialogs

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.enable
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.ConfirmReshootEcomBinding
import com.spyneai.loadSmartly
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import com.spyneai.service.ServerSyncTypes
import com.spyneai.service.log
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.startUploadingService
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File


class ConfirmReshootEcomDialog :
    BaseDialogFragment<ShootViewModelApp, ConfirmReshootEcomBinding>() {

    private var mContext: Context? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)


        checkWithClassifier()

        val uri = viewModel.shootData.value?.capturedImage
//        binding.ivCapturedImage.setRotation(90F)

        viewModel.end.value = System.currentTimeMillis()
        val difference = (viewModel.end.value!! - viewModel.begin.value!!) / 1000.toFloat()
        log("dialog- " + difference)

        requireContext().loadSmartly(uri, binding.ivCapturedImage)


        binding.btReshootImage.setOnClickListener {
            viewModel.isCameraButtonClickable = true
            val properties = HashMap<String, Any?>()
            properties.apply {
                this["sku_id"] = viewModel.shootData.value?.sku_id
                this["project_id"] = viewModel.shootData.value?.project_id
                this["image_type"] = viewModel.shootData.value?.image_category
            }
            requireContext().captureEvent(
                Events.RESHOOT,
                properties
            )

            //remove last item from shoot list
            if (!viewModel.isReclick) {
                viewModel.shootList.value?.let { list ->
                    val currentElement = list.firstOrNull {
                        it.overlayId == viewModel.overlayId
                    }

                    currentElement?.let {
                        list.remove(it)
                    }
                }
            }

            viewModel._classificationRes.value = null

            dismiss()
        }


        binding.btConfirmImage.setOnClickListener {

            if (viewModel.shootList.value?.size == 1 && !viewModel.isReshoot) {
                GlobalScope.launch(Dispatchers.IO) {
                    viewModel.createProjectSync()

//                    start sync service
                    GlobalScope.launch(Dispatchers.Main) {
                        if (viewModel.skuApp?.isSelectAble == true) {
                            BaseApplication.getContext().startUploadingService(
                                ConfirmReshootEcomDialog::class.java.simpleName,
                                ServerSyncTypes.CREATE
                            )
                        }
                    }
                }
            }

            viewModel.isProjectNameEdited.value = true

            viewModel.onImageConfirmed.value = true
            //viewModel.shootNumber.value = viewModel.shootNumber.value?.plus(1)

            viewModel.isStopCaptureClickable = true
            val properties = HashMap<String, Any?>()
            val cameraSetting = viewModel.getCameraSetting()

            properties.apply {
                put("image_data", JSONObject().apply {
                    put("sku_id", viewModel.shootData.value?.sku_id)
                    put("project_id", viewModel.shootData.value?.project_id)
                    put("image_type", viewModel.shootData.value?.image_category)
                    put("sequence", viewModel.shootData.value?.sequence)
                    put("name", viewModel.shootData.value?.name)
                    put("angle", viewModel.shootData.value?.angle)
                    put("overlay_id", viewModel.shootData.value?.overlayId)
                    put("debug_data", viewModel.shootData.value?.debugData)
                }.toString())
                put("camera_setting", JSONObject().apply {
                    put("is_overlay_active", cameraSetting.isOverlayActive)
                    put("is_grid_active", cameraSetting.isGridActive)
                    put("is_gyro_active", cameraSetting.isGryroActive)
                })
            }

            requireContext().captureEvent(
                Events.CONFIRMED,
                properties
            )

            viewModel.isCameraButtonClickable = true

            if (viewModel.isReshoot) {
                GlobalScope.launch(Dispatchers.IO) {
                    viewModel.insertImage(viewModel.shootData.value!!)
                }

                //requireContext().startUploadServiceWithCheck()
                requireContext().startUploadingService(
                    ConfirmReshootEcomDialog::class.java.simpleName,
                    ServerSyncTypes.UPLOAD
                )

                if (viewModel.allReshootClicked)
                    viewModel.reshootCompleted.value = true

                dismiss()

            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    viewModel.insertImage(viewModel.shootData.value!!)
                }

                //requireContext().startUploadServiceWithCheck()
                requireContext().startUploadingService(
                    ConfirmReshootEcomDialog::class.java.simpleName,
                    ServerSyncTypes.UPLOAD
                )
            }
            viewModel._classificationRes.value = null

            dismiss()
        }


    }

    private fun checkWithClassifier() {

        binding.pbClassifier.visibility = View.VISIBLE
        binding.clCropAngleDistance.visibility = View.GONE

        angleClassifierCall()
        observeClassifierResponse()
    }

    private fun angleClassifierCall() {


        requireContext().captureEvent(
            "Angle classifier method call",
            HashMap<String, Any?>().apply {
                put("sku_id", viewModel.skuApp?.skuId)
                put("angle", viewModel.frameAngle)
                put("overlay", viewModel.overlayId)
                put("name", viewModel.displayName)
                put("thumbnail", viewModel.displayThumbanil)
            }
        )

        val requestFile = File(viewModel.shootData.value!!.capturedImage)
        var compressedImageFile: File? = null

        try {
            GlobalScope.launch {
                withContext(Dispatchers.Default) {
                    try {
                        File("/storage/emulated/0/DCIM/Spyneclassifier/").mkdirs()

                        val dir = File("/storage/emulated/0/DCIM/Spyneclassifier/")

                        if (dir.isDirectory) {
                            val outputFile = File(
                                "/storage/emulated/0/DCIM/Spyneclassifier/" + System.currentTimeMillis()
                                    .toString() + ".jpg"
                            )

                            if (outputFile.exists())
                                outputFile.delete()

                            outputFile.createNewFile()

                            compressedImageFile = Compressor.compress(requireContext(), requestFile)
                            {
                                resolution(426, 240)
                                quality(40)
                                destination(outputFile)
                            }

                            val requestBody =
                                compressedImageFile!!.asRequestBody("image/*".toMediaTypeOrNull())

                            var filePart =
                                MultipartBody.Part.createFormData(
                                    "image_file",
                                    compressedImageFile!!.name,
                                    requestBody
                                )

                            val prod_cat_id = "cat_Ujt0kuFxY"
                                .toRequestBody("text/plain".toMediaTypeOrNull())

                            val overlay_id = viewModel.shootData.value?.overlayId.toString()
                                .toRequestBody("text/plain".toMediaTypeOrNull())

                            Log.d("ConfirmReshootEcom", "overlayIdInCClassifier$overlay_id")



                            viewModel.angleClassifierV2(
                                imageFile = filePart,
                                prod_cat_id,
                                overlay_id

                            )

                            requireContext().captureEvent(
                                Events.EXTERIOR_IMAGE_COMPRESSED,
                                HashMap<String, Any?>()
                                    .apply {
                                        put("sku_id", viewModel.shootData.value?.sku_id)
                                        put("angle", viewModel.shootData.value?.angle)
                                    }
                            )
                        } else {
                            requireContext().captureEvent(
                                "Directory not exist",
                                HashMap<String, Any?>()
                                    .apply {
                                        put("sku_id", viewModel.shootData.value?.sku_id)
                                        put("angle", viewModel.shootData.value?.angle)
                                    }
                            )
                        }
                    } catch (e: Exception) {
                        BaseApplication.getContext()?.captureEvent(
                            "Classifier method exception",
                            HashMap<String, Any?>()
                                .apply {
                                    put("sku_id", viewModel.skuApp?.skuId)
                                    put("angle", viewModel.frameAngle)
                                    put("overlay", viewModel.overlayId)
                                    put("name", viewModel.displayName)
                                    put("thumbnail", viewModel.displayThumbanil)
                                    put("exception", e)
                                    put("message", e.localizedMessage)
                                    put("cause", e.cause)
                                    put("image_path", viewModel.shootData.value?.capturedImage)
                                }
                        )
                    }
                }
            }

        } catch (e: Exception) {
            requireContext()?.captureEvent(
                Events.EXTERIOR_IMAGE_COMPRESSED_EXCEPTION,
                HashMap<String, Any?>()
                    .apply {
                        put("sku_id", viewModel.skuApp?.skuId)
                        put("exception", e)
                        put("message", e.localizedMessage)
                        put("cause", e.cause)
                        put("angle", viewModel.frameAngle)
                        put("image_path", viewModel.shootData.value?.capturedImage)
                    }
            )
            e.printStackTrace()
        }
    }


    private fun observeClassifierResponse() {
        viewModel.classificationRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        "Angle Classifier Response",
                        HashMap<String, Any?>().apply {
                            put("sku_id", viewModel.skuApp?.skuId)
                            put("data", Gson().toJson(it))
                        }
                    )

                    it.value.data?.result?.getOrNull(0)?.let { result ->
                        binding.tvBlur.text = result.description
                        val drawableRes = if (result.status == "passed") R.drawable.baseline_check_circle_24
                        else R.drawable.ic_classifier_warning
                        Glide.with(requireContext())
                            .load(drawableRes)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(false)
                            .dontAnimate()
                            .into(binding.ivBlur)
                    }
                    it.value.data?.result?.getOrNull(1)?.let { result ->
                        binding.tvExposer.text = result.description
                        val drawableRes = if (result.status == "passed") R.drawable.baseline_check_circle_24
                        else R.drawable.ic_classifier_warning
                        Glide.with(requireContext())
                            .load(drawableRes)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(false)
                            .dontAnimate()
                            .into(binding.ivExposer)
                    }
                    it.value.data?.result?.getOrNull(2)?.let { result ->
                        binding.tvCrop.text = result.description
                        val drawableRes = if (result.status == "passed") R.drawable.baseline_check_circle_24
                        else R.drawable.ic_classifier_warning
                        Glide.with(requireContext())
                            .load(drawableRes)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(false)
                            .dontAnimate()
                            .into(binding.ivCrop)
                    }
                    it.value.data?.result?.getOrNull(3)?.let { result ->
                        binding.tvDistance.text = result.description
                        val drawableRes = if (result.status == "passed") R.drawable.baseline_check_circle_24
                        else R.drawable.ic_classifier_warning
                        Glide.with(requireContext())
                            .load(drawableRes)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(false)
                            .dontAnimate()
                            .into(binding.ivDistance)
                    }


                    // Classification Status
                    if (it.value.data.classificationStatus == "passed") {
                        binding.llHint.visibility = View.VISIBLE
                        binding.tvHint.text = "Good Capture"
                        binding.tvHint.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.green
                            )
                        )
                        Glide.with(requireContext())
                            .load(R.drawable.baseline_check_circle_24)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(false)
                            .dontAnimate()
                            .into(binding.ivHint)
                        binding.llHint.setBackgroundResource(R.drawable.bg_hint_new)

                        binding.btConfirmImage.visibility = View.VISIBLE
                        binding.btReshootImage.visibility = View.VISIBLE

                    } else {

                        binding.llHint.visibility = View.VISIBLE
                        binding.tvHint.text = "Bad Capture"
                        binding.tvHint.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.red
                            )
                        )
                        Glide.with(requireContext())
                            .load(R.drawable.ic_classifier_warning)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(false)
                            .dontAnimate()
                            .into(binding.ivHint)
                        binding.llHint.setBackgroundResource(R.drawable.bg_hint)


                        binding.btConfirmImage.visibility = View.VISIBLE
                        binding.btReshootImage.visibility = View.VISIBLE

                    }


                    binding.clCropAngleDistance.visibility = View.VISIBLE
                    binding.pbClassifier.visibility = View.GONE

                    binding.llHint.visibility = View.VISIBLE
                    binding.llCropContainer.visibility = View.VISIBLE
                    binding.llExposerContainer.visibility = View.VISIBLE
                    binding.llFocusContainer.visibility = View.VISIBLE
                    binding.llDistanceContainer.visibility = View.VISIBLE


                }


                is Resource.Failure -> {

                    requireContext().captureFailureEvent(
                        "Angle Classifier Api Failed",
                        HashMap<String, Any?>().apply {
                            put("sku_id", viewModel.skuApp?.skuId)
                            put("data", Gson().toJson(it))
                            put("message", it.errorMessage)
                            put("throwable", it.throwable)
                            put("code", it.errorCode)
                        },
                        it.errorMessage.toString()
                    )
                    if (it.errorCode == 400) {
                        onInvalidObject()
                    } else {
                        handleApiError(it) { angleClassifierCall() }
                    }
                }
                else -> {

                }
            }
        }
    }


    private fun onInvalidObject() {
        binding.btReshootImage.visibility = View.VISIBLE
        binding.btConfirmImage.visibility = View.VISIBLE
        binding.clCropAngleDistance.visibility = View.VISIBLE
        binding.pbClassifier.visibility = View.GONE
        binding.llCropContainer.visibility = View.GONE
        binding.llExposerContainer.visibility = View.GONE
        binding.llFocusContainer.visibility = View.GONE
        binding.llDistanceContainer.visibility = View.GONE
        binding.llHint.visibility = View.VISIBLE
        binding.tvHint.text = "Bad Capture"
        binding.tvHint.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.red
            )
        )
        Glide.with(requireContext())
            .load(R.drawable.ic_classifier_warning)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(false)
            .dontAnimate()
            .into(binding.ivHint)
        binding.llHint.setBackgroundResource(R.drawable.bg_hint)
    }


    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // save the context as a member variable
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        // clear the context member variable
        mContext = null
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = ConfirmReshootEcomBinding.inflate(inflater, container, false)

}