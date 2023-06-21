package com.spyneai.shoot.ui.ecomwithoverlays

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
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
import com.spyneai.databinding.ConfirmReshootPortraitDialogBinding
import com.spyneai.loadSmartly
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import com.spyneai.service.ServerSyncTypes
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
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ConfirmReshootPortraitDialog :
    BaseDialogFragment<ShootViewModelApp, ConfirmReshootPortraitDialogBinding>() {

    val TAG = "ConfirmReshootDialog"

    private var mContext: Context? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val uri = viewModel.shootData.value?.capturedImage

        Log.d(TAG, "onViewCreated: " + uri)

        requireContext().loadSmartly(uri, binding.ivCapturedImage)

        setOverlay(binding.ivCapturedImage, viewModel.getOverlay())

        checkWithClassifier()


        isCancelable = false


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

            binding.pbClassifier.visibility = View.VISIBLE
            binding.clCropAngleDistance.visibility = View.GONE

            if (viewModel.shootList.value?.size == 1 && !viewModel.isReshoot) {
                GlobalScope.launch(Dispatchers.IO) {
                    viewModel.createProjectSync()

//                    start sync service
                    GlobalScope.launch(Dispatchers.Main) {
                        if (viewModel.skuApp?.isSelectAble == true) {
                            BaseApplication.getContext().startUploadingService(
                                ConfirmReshootPortraitDialog::class.java.simpleName,
                                ServerSyncTypes.CREATE
                            )
                        }
                    }
                }
            }

            viewModel.isProjectNameEdited.value = true

            viewModel.isSubCategoryConfirmed.value = true

            val properties = HashMap<String, Any?>()
            properties.apply {
                this["sku_id"] = viewModel.shootData.value?.sku_id
                this["project_id"] = viewModel.shootData.value?.project_id
                this["image_type"] = viewModel.shootData.value?.image_category
                this["sequence"] = viewModel.shootData.value?.sequence
            }

            viewModel.isCameraButtonClickable = true


            if (viewModel.isReshoot) {
                uploadImages()

                if (viewModel.allReshootClicked)
                    viewModel.reshootCompleted.value = true

                dismiss()
            } else {
                uploadImages()
                if (viewModel.allEcomOverlyasClicked) {
                    viewModel.isCameraButtonClickable = false
                    viewModel.stopShoot.value = true
                }

                viewModel._classificationRes.value = null

                dismiss()
            }
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
                withContext(Dispatchers.IO) {
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

                            val prod_cat_id =
                                "cat_Ujt0kuFxY".toRequestBody("text/plain".toMediaTypeOrNull())

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

                    if(it.value.data.result.isNullOrEmpty()){
                        handleApiError(Resource.Failure(
                            false,
                            null,
                            "AI classification failed",
                            null
                        )) { angleClassifierCall() }
                    }else {
                        it.value.data.result.getOrNull(0)?.let { result ->
                            binding.tvBlur.text = result.description
                            val drawableRes =
                                if (result.status == "passed") R.drawable.baseline_check_circle_24
                                else R.drawable.ic_classifier_warning
                            Glide.with(requireContext())
                                .load(drawableRes)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(false)
                                .dontAnimate()
                                .into(binding.ivBlur)
                        }
                        it.value.data.result.getOrNull(1)?.let { result ->
                            binding.tvExposer.text = result.description
                            val drawableRes =
                                if (result.status == "passed") R.drawable.baseline_check_circle_24
                                else R.drawable.ic_classifier_warning
                            Glide.with(requireContext())
                                .load(drawableRes)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(false)
                                .dontAnimate()
                                .into(binding.ivExposer)
                        }
                        it.value.data.result.getOrNull(2)?.let { result ->
                            binding.tvCrop.text = result.description
                            val drawableRes =
                                if (result.status == "passed") R.drawable.baseline_check_circle_24
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
                            val drawableRes =
                                if (result.status == "passed") R.drawable.baseline_check_circle_24
                                else R.drawable.ic_classifier_warning
                            Glide.with(requireContext())
                                .load(drawableRes)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(false)
                                .dontAnimate()
                                .into(binding.ivDistance)
                        }
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


                        binding.btConfirmImage.visibility = View.GONE
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
        binding.btConfirmImage.visibility = View.GONE
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


    private fun callUpdateSubcat() {
        Utilities.showProgressDialog(requireContext())
        viewModel.updateFootwearSubcategory()
    }

    private fun onImageConfirmed() {
//        viewModel.isCameraButtonClickable = true
//        uploadImages()
//        if (viewModel.shootNumber.value == viewModel.exterirorAngles.value?.minus(1)) {
//            dismiss()
//            Log.d(TAG, "onViewCreated: "+"checkInteriorShootStatus")
//            viewModel.stopShoot.value = true
//        } else {
//            viewModel.shootNumber.value = viewModel.shootNumber.value!! + 1
//            dismiss()
//        }
    }

    private fun observeupdateFootwarSubcat() {
        viewModel.updateFootwearSubcatRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    onImageConfirmed()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { callUpdateSubcat() }
                }
                else -> {

                }
            }
        }
    }

    private fun uploadImages() {
        viewModel.onImageConfirmed.value = viewModel.getOnImageConfirmed()

        GlobalScope.launch(Dispatchers.IO) {
            viewModel.insertImage(viewModel.shootData.value!!)
        }

        requireContext().startUploadingService(
            ConfirmReshootPortraitDialog::class.java.simpleName,
            ServerSyncTypes.UPLOAD
        )
        //requireContext().startUploadServiceWithCheck()
    }


    private fun setOverlay(view: View, overlay: String) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                Glide.with(requireContext())
                    .load(overlay)
                    .into(binding.ivCapturedOverlay)


                viewModel.shootDimensions.value.let {
//                    var prw = it?.previewWidth
//                    var prh = it?.previewHeight
//
//                    var ow = it?.overlayWidth
//                    var oh = it?.overlayHeight
//
//
//                    Log.d(TAG, "onGlobalLayout: "+prw)
//                    Log.d(TAG, "onGlobalLayout: "+prh)
//
//                    Log.d(TAG, "onGlobalLayout: "+ow)
//                    Log.d(TAG, "onGlobalLayout: "+oh)
//
//                    Log.d(TAG, "onGlobalLayout: "+view.width)
//                    Log.d(TAG, "onGlobalLayout: "+view.height)
//
//                    var newW =
//                        ow!!.toFloat().div(prw!!.toFloat()).times(view.width)
//                    var newH =
//                        oh!!.toFloat().div(prh!!.toFloat()).times(view.height)
//
//                    var equlizerOverlayMargin = (9.5 * resources.displayMetrics.density).toInt()
//
//                    var params = FrameLayout.LayoutParams(newW.toInt(), newH.toInt())
//                    params.gravity = Gravity.CENTER
//                    params.leftMargin = equlizerOverlayMargin
//                    params.rightMargin = equlizerOverlayMargin
//
//                    binding.ivCapturedOverlay.layoutParams = params


                }
            }
        })
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


    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = ConfirmReshootPortraitDialogBinding.inflate(inflater, container, false)

}