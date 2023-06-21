package com.spyneai.onboardingv2.ui.intro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.dashboard.ui.enable
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentProcessImageBinding
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.ui.intro.adapter.ImageProcessingAdapter
import com.spyneai.shootapp.data.model.CarsBackgroundRes
import com.spyneai.shootapp.ui.dialogs.ShootExitDialog
import com.spyneai.singleimageprocessing.data.SingleImageDownloadingService
import com.spyneai.singleimageprocessing.data.SingleImageViewModel

class ProcessImageFragment : BaseFragment<SingleImageViewModel, FragmentProcessImageBinding>(),
    OnItemClickListener {

    private var image : String? = ""
    private var backgroundId = ""
    private var currentSelectedItem: CarsBackgroundRes.BackgroundApp? = null
    private lateinit var imageProcessingAdapter: ImageProcessingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = arguments?.getString("selected_image")


        if(backgroundId.isNullOrEmpty()){
            binding.switchOriginal.isChecked=false
            binding.switchOriginal.isClickable=false
        }else{
            binding.switchOriginal.isClickable=true
        }
        
        
        binding.switchOriginal.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.ivSelected.visibility=View.INVISIBLE
                binding.ivWebView.visibility=View.VISIBLE
            } else {
                binding.ivSelected.visibility=View.VISIBLE
                binding.ivWebView.visibility=View.INVISIBLE
            }
        }



        setSelectedImage(image)

        getBackgrounds(image)

        binding.btnDownload.setOnClickListener {
            val imageList = ArrayList<String>()
            val nameList = ArrayList<String>()
            imageList.add(Utilities.getPreference(requireContext(),AppConstants.LAST_PROCESSED_IMAGE).toString())
            nameList.add(System.currentTimeMillis().toString())

            var imageDownloadingServiceIntent = Intent(requireContext(),SingleImageDownloadingService::class.java)
            imageDownloadingServiceIntent.action = "START"
            imageDownloadingServiceIntent.putExtra(AppConstants.LIST_HD_QUALITY,imageList)
            imageDownloadingServiceIntent.putExtra(AppConstants.LIST_IMAGE_NAME,nameList)
            ContextCompat.startForegroundService(requireContext(), imageDownloadingServiceIntent)

            Toast.makeText(requireContext(),"Downloading Started...",Toast.LENGTH_LONG).show()
        }
        binding.ivBackGif!!.setOnClickListener {
            ShootExitDialog().show(requireActivity().supportFragmentManager, "ShootExitDialog")
        }

    }

    private fun getBackgrounds(image: String?) {
        val list = ArrayList<CarsBackgroundRes.BackgroundApp>()

        image?.let {
            val original = CarsBackgroundRes.BackgroundApp(
                id = 0,
                categoryId = "categoryId",
                bgName = "categoryId",
                gifUrl = "categoryId",
                imageCredit = 1,
                imageId = "1",
                imageUrl = it,
                isSelected = true,
                prodCatId = viewModel.category?.categoryId!!,
                prodSubCatId = viewModel?.subcategoryV2?.prodSubCatId,
                backgroundType = ""
            )

//            list.add(original)
//            currentSelectedItem = original
        }

        //get backgrounds
        viewModel.getBackgrounds()?.let {
                list.addAll(it)
        }

        imageProcessingAdapter = ImageProcessingAdapter(list, this)

        binding.rvImages.apply {
            layoutManager =
                GridLayoutManager(requireContext(),3, GridLayoutManager.VERTICAL, false)
            adapter = imageProcessingAdapter
        }

    }

    private fun setSelectedImage(image: String?) {

        viewModel.category?.let {
            if (it.orientation == "landscape"){
                binding.ivSelected.visibility = View.VISIBLE
                binding.ivWebView.visibility = View.INVISIBLE
                Glide.with(requireContext())
                    .load(image)
                    .into(binding.ivSelected!!)
            }else {
                if (image?.contains("/storage/emulated/0/DCIM/Spyne/") == false){
                    binding.ivWebView.visibility = View.VISIBLE
                binding.ivSelected.visibility = View.INVISIBLE
                    Glide.with(requireContext())
                        .load(image)
                        .into(binding.ivSelected!!)
                }else {
                    binding.ivSelected.visibility = View.VISIBLE
                binding.ivWebView.visibility = View.INVISIBLE
                    requireContext().loadSmartly(image,binding.ivSelected)
                }
            }
        }
    }

    override fun getViewModel() = SingleImageViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProcessImageBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when (data) {
            is CarsBackgroundRes.BackgroundApp -> {
                if (currentSelectedItem != data){
                    backgroundId = data.imageId
                    //process single image

                    binding.switchOriginal.isChecked=true

                    processImage()
                    observeProcessImage()
                }else {
                    binding.btnDownload.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_signup_disabled)
                    binding.btnDownload.isClickable = false
                    setSelectedImage(image)
                }
                //unselect last selected item
                currentSelectedItem?.let {
                    it.isSelected = false
                }

                data.isSelected = true
                currentSelectedItem = data

                imageProcessingAdapter?.notifyDataSetChanged()

                binding.switchOriginal.isClickable=true

            }

        }
    }

    private fun processImage() {
        binding.progressBar.isVisible = true
        val processDataMap = HashMap<String,Any>()

        arguments?.let {
            processDataMap.apply {
                put("prod_cat_id",viewModel.category?.categoryId!!)
                put("prod_sub_cat_id",it.getString(AppConstants.SUB_CAT_ID).toString())
                put("image_category", viewModel.category?.imageCategories?.get(0)!!)
                put("project_id", it.getString(AppConstants.PROJECT_ID).toString())
                put("sku_id", it.getString(AppConstants.SKU_ID).toString())
                put("image_url", it.getString(AppConstants.IMAGE_URL).toString())
                put("background_id", backgroundId)
                put("source", "App_android_single")
                put("image_name", it.getString(AppConstants.IMAGE_NAME).toString())
                put("angle", it.getInt(AppConstants.IMAGE_ANGLE,0))

            }
        }
        viewModel.processImage(processDataMap)
    }

    private fun observeProcessImage() {
        viewModel.processSingleImage.observe(viewLifecycleOwner) {
            when (it) {
                is com.spyneai.base.network.Resource.Success -> {

                    val requestOptions = RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(200, 200) // resize does not respect aspect ratio

                    binding.ivWebView.visibility = View.VISIBLE
                    binding.ivSelected.visibility = View.INVISIBLE
                    binding.ivWebView.settings.javaScriptEnabled = true
                    binding.ivWebView.settings.loadWithOverviewMode = true
                    binding.ivWebView.settings.useWideViewPort = true
                    binding.ivWebView.loadUrl(it.value.data.outputWatermarkImage)
                    binding.progressBar.isVisible = false

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.LAST_PROCESSED_IMAGE,
                        it.value.data.outputWatermarkImage
                    )

                    binding.btnDownload.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_gradient_button)
                    binding.btnDownload.enable(true)
                    binding.btnDownload.isClickable = true
                    binding.btnDownload.setTextColor(Color.WHITE)
                }

                is com.spyneai.base.network.Resource.Failure -> {
                    binding.progressBar.isVisible = false
                    handleApiError(it) {
                        processImage()
                    }
                }
                else -> {

                }
            }
        }
    }
}




















