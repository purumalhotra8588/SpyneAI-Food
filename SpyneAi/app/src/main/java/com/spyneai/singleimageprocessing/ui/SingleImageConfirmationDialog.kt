package com.spyneai.singleimageprocessing.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.repository.model.GetGCPUrlRes
import com.spyneai.dashboard.ui.handleApiError

import com.spyneai.databinding.DialogSingleImageConfirmationBinding
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.ui.intro.ImageProcessingActivity
import com.spyneai.singleimageprocessing.data.SingleImageViewModel
import com.spyneai.trybackground.TryBackgroundActivity
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class SingleImageConfirmationDialog :
    BaseDialogFragment<SingleImageViewModel, DialogSingleImageConfirmationBinding>() {

    private val TAG = SingleImageConfirmationDialog::class.simpleName
    private var imageName = ""
    private var processImage = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        isCancelable = false

        processImage = arguments?.getBoolean(AppConstants.PROCESS_IMAGE, false)!!

        arguments?.getString("cta")?.let {
            binding.btReshootImage.text = it
        }

        //load image
        arguments?.getString("captured_image")?.let { image ->

            if (arguments?.getBoolean(AppConstants.FROM_SELECTION, false) == true) {
                Glide.with(requireContext())
                    .load(image)
                    .into(binding.ivCapturedImage!!)
            } else {
                viewModel.category?.let {
                    if (it.orientation == "landscape") {
                        Glide.with(requireContext())
                            .load(image)
                            .into(binding.ivCapturedImage!!)
                    } else {
                        requireContext().loadSmartly(image, binding.ivCapturedImage)
                    }
                }
            }

        }

        binding.btReshootImage?.setOnClickListener {
            viewModel.isCameraButtonClickable = true
            dismiss()
        }

        binding.btConfirmImage?.setOnClickListener {
            viewModel.isCameraButtonClickable = true
            //get pre signed url
            getPreSignedUrl()
        }
    }

    private fun getPreSignedUrl() {
        Utilities.showProgressDialog(requireContext())

        arguments?.getString("captured_image")?.let { image ->
            imageName = File(image).name.substringBefore(".") + ".jpeg"

            viewModel.getPreSignedUrl(imageName)

            viewModel.gcpUrlResponse.observe(viewLifecycleOwner, {
                when (it) {
                    is Resource.Success -> {
                        //upload image to gcp
                        uploadImage(image, it.value.data)
                    }

                    is Resource.Failure -> {
                        Utilities.hideProgressDialog()
                        handleApiError(it) {
                            getPreSignedUrl()
                        }
                    }
                    else -> {

                    }
                }
            })
        }
    }

    private fun uploadImage(imagePath: String, data: GetGCPUrlRes.Data,showDialog: Boolean = false) {
        if (showDialog)
            Utilities.showProgressDialog(requireContext())

        GlobalScope.launch {
            uploadImageToGcp(imagePath, data)
        }
    }

    private suspend fun uploadImageToGcp(
        imagePath: String,
        data: GetGCPUrlRes.Data
    ) {
        val requestFile = File(imagePath)
        var compressedImageFile: File? = null
        try {
            compressedImageFile = Compressor.compress(requireContext(), requestFile)

            val uploadResponse = viewModel.uploadImageToGcp(
                data.presignedUrl,
                compressedImageFile.asRequestBody("text/x-markdown; charset=utf-8".toMediaTypeOrNull())
            )

            when (uploadResponse) {
                is Resource.Success -> {
                    if (processImage) {
                        processImage(data)
                        GlobalScope.launch(Dispatchers.Main) {
                            observeProcessImage(data)
                        }
                    } else {
                        Utilities.hideProgressDialog()
                        Intent(requireContext(), ImageProcessingActivity::class.java)
                            .apply {
                                putExtra("selected_image", imagePath)
                                putExtra(
                                    AppConstants.SUB_CAT_ID,
                                    viewModel.subcategoryV2?.prodSubCatId
                                )
                                putExtra(AppConstants.PROJECT_ID, data.projectId)
                                putExtra(AppConstants.SKU_ID, data.skuId)
                                putExtra(AppConstants.IMAGE_URL, data.fileUrl)
                                putExtra(AppConstants.IMAGE_NAME, imageName)
                                putExtra(
                                    AppConstants.IMAGE_ANGLE,
                                    arguments?.getInt(AppConstants.IMAGE_ANGLE, 0)
                                )
                                startActivity(this)
                            }
                        dismiss()
                    }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(uploadResponse) {
                        uploadImage(imagePath, data,true)
                    }
                }
                else -> {

                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "uploadImageToGcp: ${e.localizedMessage}")
            Utilities.hideProgressDialog()
        }
    }

    private fun processImage(data: GetGCPUrlRes.Data) {
        val processDataMap = HashMap<String, Any>()

        arguments?.let {
            processDataMap.apply {
                put("prod_cat_id", viewModel.category?.categoryId!!)
                put("prod_sub_cat_id", it.getString(AppConstants.SUB_CAT_ID).toString())
                put("image_category", viewModel.category?.imageCategories?.get(0)!!)
                put("project_id", data.projectId)
                put("sku_id", data.skuId)
                put("image_url", data.fileUrl)
                put("background_id", it.getString(AppConstants.BACKGROUND_ID).toString())
                put("source", "App_android_single")
                put("image_name", it.getString(AppConstants.IMAGE_NAME).toString())
                put("angle", it.getInt(AppConstants.IMAGE_ANGLE, 0))

            }
        }
        viewModel.processImage(processDataMap)
    }

    private fun observeProcessImage(data: GetGCPUrlRes.Data) {
        viewModel.processSingleImage.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    arguments?.let { bundle ->
                        if (bundle.getBoolean(AppConstants.FROM_SELECTION)) {
                            viewModel.uploadData = data
                            viewModel.outputUrl = it.value.data.outputImage
                            viewModel.imageProcessed.value = true
                        } else {
                            //start try new background activity
                            val intent = Intent(requireContext(), TryBackgroundActivity::class.java)
                            intent.putExtra(AppConstants.IMAGE_URL,it.value.data.outputImage)
                            intent.putExtra(AppConstants.UPLOAD_URL,data.fileUrl)
                            intent.putExtra(AppConstants.PROJECT_ID,data.projectId)
                            intent.putExtra(AppConstants.SKU_ID,data.skuId)
                            intent.putExtra(AppConstants.BACKGROUND_ID,arguments?.getString(AppConstants.BACKGROUND_ID))
                            startActivity(intent)
                        }
                    }

                    dismiss()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) {
                        processImage(data)
                    }
                }
                else -> {

                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun getViewModel() = SingleImageViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogSingleImageConfirmationBinding.inflate(inflater, container, false)
}