package com.spyneai.output.ui


import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.databinding.FragmentSkuBeforeAfterBinding
import com.spyneai.databinding.ViewImagesBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.output.adapters.SkuBeforeAfterAdapter
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import com.spyneai.processedimages.data.ProcessedViewModelApp
import com.spyneai.processedimages.ui.data.ProcessedRepository
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.shootapp.repository.model.image.Image

import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SkuBeforeAfterFragment : BaseFragment<ProcessedViewModelApp, FragmentSkuBeforeAfterBinding>() {

    var mContext: Context? = null
    val TAG = "SkuBeforeAfterFragment"
    private lateinit var skuBeforeAfterAdapter: SkuBeforeAfterAdapter
    lateinit var dataList: ArrayList<Image>
    private var showPrview = false
    private val processedRepository = ProcessedRepository()
    private val sdkDatabase = SpyneAppDatabase.getInstance(BaseApplication.getContext())


    var handler: Handler = Handler(Looper.getMainLooper())
    var runnable: Runnable? = null
    private var refreshData = true
    private var skuUuid: String? = null

    override fun getViewModel() = ProcessedViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSkuBeforeAfterBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataList = ArrayList()

        binding.lvProcessing.isVisible = arguments?.getBoolean("is_360", false) == true

        skuUuid = arguments?.getString("sku_uuid")

        skuUuid?.let {
            lifecycleScope.launch {
                val skuImageList = withContext(Dispatchers.IO){
                    viewModel.getSkuImageList(it)
                }

                skuImageList?.let { skuImageList ->
                    binding.shimmerOutput.stopShimmer()
                    binding.shimmerOutput.visibility = View.GONE
                    binding.lvProcessing.visibility = View.GONE
                    binding.rvBeforeAfter.visibility = View.VISIBLE

                    if (viewModel.skuSucessResultCount.value == null)
                        viewModel.skuSucessResultCount.value = 0

                    viewModel.skuSucessResultCount.value =
                        viewModel.skuSucessResultCount.value!!.plus(1)
                    binding.flBeforeAfter.visibility = View.VISIBLE

                    if (!skuImageList.isNullOrEmpty()) {
                        binding.tvShootName.text = skuImageList[0].skuName
                        binding.tvNoOfImages.text = skuImageList.size.toString()
                    }

                    dataList.clear()
                    dataList.addAll(skuImageList)

                    skuBeforeAfterAdapter =
                        SkuBeforeAfterAdapter(BaseApplication.getContext(),
                            dataList,
                            object : SkuBeforeAfterAdapter.BtnClickListener {
                                override fun onBtnClick(position: Int) {
                                    try {
                                        val image = dataList[position]
                                        if (!image.output_image_lres_url.isNullOrEmpty())
                                            showImagesDialog(image)
                                    } catch (e: java.lang.Exception) {
                                    }
                                }
                            })

                    val layoutManager: RecyclerView.LayoutManager =
                        LinearLayoutManager(
                            BaseApplication.getContext(),
                            LinearLayoutManager.HORIZONTAL, false
                        )

                    binding.rvBeforeAfter.layoutManager = layoutManager
                    binding.rvBeforeAfter.adapter = skuBeforeAfterAdapter
                }
            }
        }

        viewModel.updatedImages.observe(viewLifecycleOwner) {
            it?.let { image ->
                dataList.firstOrNull { it.imageId == image.imageId || (it.overlayId == image.overlayId && it.sequence == image.sequence) }?.let {
                    try {
                        val index = dataList.indexOf(it)
                        if (index != -1) {
                            dataList[index].input_image_lres_url = image.input_image_lres_url
                            dataList[index].input_image_hres_url = image.input_image_hres_url
                            dataList[index].output_image_lres_url = image.output_image_lres_url
                            dataList[index].output_image_hres_url = image.output_image_hres_url
                            dataList[index].output_image_lres_wm_url = image.output_image_lres_wm_url

                            skuBeforeAfterAdapter.notifyItemChanged(index)
                        }
                    }catch (e: Exception){}
                }
            }
        }
    }

    private fun getData(skuId: String?, projectUuid: String, skuUuid: String, skuName: String?) {

        GlobalScope.launch(Dispatchers.IO) {
            var response = arguments?.getString("sku_id")
                ?.let { processedRepository.getImagesOfSku(it) }

            GlobalScope.launch(Dispatchers.Main) {

                var properties = HashMap<String, Any?>().apply {
                    this.put("sku_id", arguments?.getString("sku_id"))
                    this.put("project_uuid", arguments?.getString("project_uuid")!!)
                    this.put("sku_uuid", arguments?.getString("sku_uuid")!!)
                }

                if (isAttachedToActivity()) {
                    BaseApplication.getContext()
                        .captureEvent(Events.FETCH_OUTPUT_IMAGES_CALLED, properties)
                }

                when (response) {
                    is Resource.Success -> {

                        val positionView =
                            (binding.rvBeforeAfter.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                        val positionView2 =
                            (binding.rvBeforeAfter.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        val positionView3 =
                            (binding.rvBeforeAfter.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                        val positionView4 =
                            -(binding.rvBeforeAfter.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                        binding.llPrevious.setOnClickListener {
                            binding.rvBeforeAfter.smoothScrollToPosition(
                                positionView4 - 1
                            )
                            skuBeforeAfterAdapter.notifyDataSetChanged()
                        }
                        binding.rlNext.setOnClickListener {
                            binding.rvBeforeAfter.smoothScrollToPosition(
                                positionView4 + 1
                            )
                            skuBeforeAfterAdapter.notifyDataSetChanged()
                        }
                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Failure -> {
                        var properties = HashMap<String, Any?>().apply {
                            this.put("error_code", response.errorCode)
                            this.put("error_message", response.errorMessage)
                            this.put("sku_id", arguments?.getString("sku_id"))
                            this.put(
                                "auth_key",
                                Utilities.getPreference(
                                    BaseApplication.getContext(),
                                    AppConstants.AUTH_KEY
                                )
                            )
                        }
                        BaseApplication.getContext().captureFailureEvent(
                            Events.FETCH_OUTPUT_IMAGES_FAILED,
                            properties,
                            response.errorMessage.toString()
                        )

                        showPrview = false

                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun showImagesDialog(image: Image) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_show_images)

        val window: Window = dialog.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val carouselViewImages: CarouselView = dialog.findViewById(R.id.carouselViewImages)
        val ivCrossImages: ImageView = dialog.findViewById(R.id.ivCrossImages)

        ivCrossImages.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        carouselViewImages.pageCount = dataList.size
        carouselViewImages.setViewListener(viewListener)

        dataList.firstOrNull { it.imageId == image.imageId }?.let {
            val index = dataList.indexOf(it)
            if (index != -1) {
                carouselViewImages.currentItem = index
            }
        }

        dialog.show()
    }

    var viewListener = object : ViewListener {
        override fun setViewForPosition(position: Int): View? {
            val customView: View = layoutInflater.inflate(R.layout.view_images, null)
            val customBiding = ViewImagesBinding.bind(customView)


            Glide.with(BaseApplication.getContext())
                .load(dataList[position].input_image_lres_url)
                .apply(
                    RequestOptions()
                        .error(com.spyneai.R.mipmap.defaults)
                )
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        @Nullable e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        //on load failed
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        //on load success
                        return false
                    }
                })
                .into(customBiding.ivBefore)

            Glide.with(BaseApplication.getContext())
                .load(dataList[position].output_image_lres_url)
                .apply(
                    RequestOptions()
                        .error(com.spyneai.R.mipmap.defaults)
                )
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        @Nullable e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        //on load failed
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        //on load success
                        return false
                    }
                })
                .into(customBiding.ivAfter)

            return customView
        }
    }

    private fun pingData() {
        handler.postDelayed(Runnable {
            runnable?.let { handler.postDelayed(it, 3000) }
            skuUuid?.let { viewModel.getProcessedImageData(it) }
        }.also { runnable = it }, 3000)

    }


    override fun onResume() {
        super.onResume()
        refreshData = true
        pingData()
    }

    override fun onPause() {
        super.onPause()
        refreshData = false
        runnable?.let { handler.removeCallbacks(it) }
    }

    fun isAttachedToActivity(): Boolean {
        return isVisible && activity != null
    }
}