package com.spyneai.food

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.enable
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.dashboardV2.ui.adapters.FoodBgAdapter
import com.spyneai.databinding.FragmentOutputFoodBinding
import com.spyneai.databinding.FragmentSelectBackgroundBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.pxFromDp
import com.spyneai.shootapp.data.OnOverlaySelectionListener
import com.spyneai.shootapp.data.ProcessViewModelApp
import com.spyneai.shootapp.utils.objectToString
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_order_summary2.ivProductImage


class OutputFoodFragment : BaseFragment<ProcessViewModelApp, FragmentOutputFoodBinding>(),
    OnItemClickListener, OnOverlaySelectionListener {

    val TAG = OutputFoodFragment::class.java.simpleName
    var adapter: FoodBgAdapter? = null
    var frameNumber = 1
    val diffusionImagesList = ArrayList<DiffusionImages>()
    var number = 3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val capturedImage = Utilities.getPreference(requireContext(), AppConstants.CAPTURED_IMAGE)

        diffusionImagesList.add(
            DiffusionImages(
                1,
                capturedImage.toString(),
                null,
                true,
                true,
                null
            ),
        )
        diffusionImagesList.add(
            DiffusionImages(
                2,
                capturedImage.toString(),
                null,
                false,
                false,
                null
            ),
        )
        diffusionImagesList.add(
            DiffusionImages(
                3,
                capturedImage.toString(),
                null,
                false,
                false,
                null
            ),
        )

        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL, false
            )



        adapter = FoodBgAdapter(diffusionImagesList, this, this)

        binding.rvAiFood.layoutManager = layoutManager
        binding.rvAiFood.adapter = adapter

        binding.btnFinish.setOnClickListener {
            getstableMarkDoneDiffusion()
        }


        binding.btnGenerateMore.setOnClickListener {

            if(viewModel.enableAfter==false){
                viewModel.enableAfter = true
                modifySwitchView()
                adapter?.notifyDataSetChanged()
            }

            if (frameNumber < 3) {
                //get current selected item
                val currentSelected = getCurrentSelectedImageProcessedUrl()
                //mark current selected item's isSelected false

                currentSelected?.let {
                    currentSelected.isSelected = false

                    //mark frame+1 item's isSelected true
                    val nextItem = diffusionImagesList[frameNumber]
                    nextItem.isSelected = true
                    nextItem.isEnabled = true
                    //notify data update
                    Utilities.saveBool(requireContext(), AppConstants.ENABLE_AFTER, true)
                    adapter?.notifyDataSetChanged()
                }

            }

        }

        switchListener()
        modifySwitchView()
        observeStableDiffusion()
    }


    private fun observeStableDiffusion() {

        viewModel.stableDiffusionResponse.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    is Resource.Success -> {

                        val item = getCurrentSelectedImageProcessedUrl()

                        item?.let { item ->

                            Log.d(TAG, "observeStableDiffusion: ${item.objectToString()}")
                            Log.d(TAG, "observeStableDiffusion: ${it.value.data.objectToString()}")
                            Log.d(TAG, "observeStableDiffusion: ${it.value.data.outputImageUrl}")
                            Log.d(TAG, "observeStableDiffusion: ${it.value.data.imageId}")
                            item.processedImageUrl = it.value.data.outputImageUrl
                            item.imageId = it.value.data.imageId

                            Utilities.savePrefrence(requireContext(),AppConstants.IMAGE_ID_FOOD,item.imageId)
                            binding.flImage.background = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.bg_inspection_switch
                            )

                            val finalNumber = number - item.frameNumber
                            binding.btnGenerateMore.text = "Generate More" + "($finalNumber Left)"
                            if (finalNumber == 0)
                                binding.btnGenerateMore.visibility = View.GONE

                            val picasso = Picasso.get()
                            picasso.load(item.processedImageUrl)
//                                .resize(
//                                    pxFromDp(requireContext(), 150f).toInt(),
//                                    pxFromDp(requireContext(), 150f).toInt()
//                                )
                                .into(binding.ivImage, object : Callback {
                                    override fun onSuccess() {
                                        binding.progressBar.visibility =
                                            View.GONE // Hide the progress bar on success
                                    }

                                    override fun onError(e: Exception?) {
                                        binding.progressBar.visibility =
                                            View.GONE // Hide the progress bar on error
                                    }
                                })

                            binding.btnFinish.enable(true)
                            binding.btnGenerateMore.enable(true)

                            viewModel.updateSDResponse()
                            Log.d(TAG, "observeStableDiffusion: update ${item.objectToString()}")
                            adapter?.notifyItemChanged(frameNumber.minus(1))
                        }

                    }

                    is Resource.Failure -> {
                        handleApiError(it) { getstableDiffusion() }
                    }

                    else -> {

                    }
                }
            }
        }

    }

    private fun switchListener() {

        binding.tvBefore.setOnClickListener {
            viewModel.enableBefore = true
            viewModel.enableAfter = false
            modifySwitchView()
        }

        binding.ivBackGif.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.tvAfter.setOnClickListener {
            viewModel.enableAfter = true
            viewModel.enableBefore = false
            modifySwitchView()
        }
    }

    private fun modifySwitchView() {
        if (viewModel.enableAfter) {
            Utilities.saveBool(requireContext(), AppConstants.ENABLE_AFTER, viewModel.enableAfter)
            binding.apply {
                tvAfter.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_switch)
                tvBefore.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_off_switch)
                tvAfter.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.primary_light_dark)
                )
                tvBefore.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.blackfood
                    )
                )

                val image = getCurrentSelectedImageProcessedUrl()

                image?.let {
                    val finalUrl =
                        if (it.processedImageUrl == null) it.rawUrl else it.processedImageUrl.toString()
                    val picasso = Picasso.get()

                    picasso
                        .load(finalUrl)
                        .into(binding.ivImage)
                }
            }
        } else {
            val capturedImage =
                Utilities.getPreference(requireContext(), AppConstants.CAPTURED_IMAGE)
            binding.apply {
                tvBefore.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_switch)
                tvAfter.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_off_switch)
                tvAfter.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.blackfood
                    )
                )
                tvBefore.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.primary_light_dark
                    )
                )
                Glide.with(requireContext()).load(capturedImage).into(binding.ivImage)
            }

        }


        adapter?.notifyDataSetChanged()
    }

    private fun getCurrentSelectedImageProcessedUrl(): DiffusionImages? {
        var cuurentItem: DiffusionImages? = null

        adapter?.listItems?.let {
            (it as List<DiffusionImages>)?.firstOrNull { it.isSelected }?.let {
                cuurentItem = it
            }

        }

        return cuurentItem
    }

    private fun getstableMarkDoneDiffusion() {

        val imageMarkDoneBody = MarkDoneBody(
                image_id = Utilities.getPreference(requireContext(),AppConstants.IMAGE_ID_FOOD).toString(),
                sku_id = viewModel.skuApp?.skuId.toString()
            )

        viewModel.stableDiffusionMarkDone(imageMarkDoneBody)


        observeStableDiffusionMarkDone()


    }

    private fun observeStableDiffusionMarkDone() {

        viewModel.stableDiffusionMarkDone.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    requireContext().gotoHome()
                }

                is Resource.Failure -> {
                    handleApiError(it) { getstableMarkDoneDiffusion() }
                }

                else -> {

                }
            }
        }

    }

    private fun getstableDiffusion() {

        val imageBody = ImageBody(
            image_url = arguments?.getString("image_url").toString(),
            sub_category_id = viewModel.skuApp?.subcategoryId!!,
            sku_id = viewModel.skuApp?.skuId!!,
            frame_no = frameNumber,
            project_id = viewModel.projectApp?.projectId!!
        )
        viewModel.stableDiffusion(imageBody)
    }

    override fun getViewModel() = ProcessViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOutputFoodBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when (data) {
            is DiffusionImages -> {

                val currentItem = getCurrentSelectedImageProcessedUrl()

                currentItem?.let { currentItem ->
                    if (currentItem != data) {
                        val index = currentItem.frameNumber.minus(1)
                        currentItem.isSelected = false
                        adapter?.notifyItemChanged(index)

                        data.isSelected = true
                        adapter?.notifyItemChanged(position)
                    }
                }
            }
        }
    }



    override fun onOverlaySelected(view: View, position: Int, data: Any?) {

        when (data) {
            is DiffusionImages -> {

                frameNumber = data.frameNumber
                Log.d(TAG, "onOverlaySelected: ${data.objectToString()}")
                Log.d(TAG, "onOverlaySelected: ${frameNumber}")

                if (data.processedImageUrl == null) {

                    val picasso = Picasso.get()

                    picasso
                        .load(data.rawUrl)
                        .into(binding.ivImage)

                    binding.progressBar.visibility = View.VISIBLE

                    binding.flImage.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_inspection_food)

                    binding.btnFinish.enable(false)
                    binding.btnGenerateMore.enable(false)

                    getstableDiffusion()
                } else {

                    if (viewModel.enableAfter) {
                        binding.progressBar.visibility =
                            View.GONE

                        val picasso = Picasso.get()
                        picasso
                            .load(data.processedImageUrl)
                            .into(binding.ivImage)
                    }
                    else{
                        binding.progressBar.visibility =
                            View.GONE

                        val picasso = Picasso.get()
                        picasso
                            .load(data.rawUrl)
                            .into(binding.ivImage)
                    }

                }
            }
        }
    }


}