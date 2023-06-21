package com.spyneai.processedimages.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.spyneai.R
import com.spyneai.activity.DownloadingActivity
import com.spyneai.activity.OrderSummary2Activity
import com.spyneai.adapter.ShowReplacedImagesAdapter
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentProcessedImagesBinding
import com.spyneai.databinding.ViewImagesBinding
import com.spyneai.gotoHome
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ScrollingLinearLayoutManager
import com.spyneai.needs.Utilities
import com.spyneai.processedimages.CreditCalculationFragment
import com.spyneai.processedimages.data.ProcessedViewModelApp


import com.spyneai.shootapp.data.DraftClickedImages
import com.spyneai.shootapp.repository.model.image.Image
import com.spyneai.shootapp.ui.base.ShootPortraitActivity
import com.spyneai.threesixty.ui.dialogs.Request360Dialog
import com.spyneai.videorecording.fragments.DialogEmbedCode
import com.spyneai.videorecording.model.TSVParams
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener

class ProcessedImagesFragment :
    BaseFragment<ProcessedViewModelApp, FragmentProcessedImagesBinding>(),
    View.OnClickListener {

    private lateinit var frontFramesList: ArrayList<String>
    private lateinit var imageListNerf: ArrayList<String>
    lateinit var tsvParamFront: TSVParams
    var handler = Handler()
    var shootId = ""

    lateinit var builder: NotificationCompat.Builder
    lateinit var exteriorImageList: ArrayList<Image>



    lateinit var imageListWaterMark: ArrayList<String>
    lateinit var listHdQuality: ArrayList<String>
    lateinit var imageNameList: ArrayList<String>
    var catName: String = ""
    var numberOfImages: Int = 0

    private lateinit var showReplacedImagesAdapter: ShowReplacedImagesAdapter

    var downloadCount: Int = 0
    lateinit var Category: String
    var TAG = "ShowImagesActivity"

    var downloadHighQualityCount: Int = 5
    lateinit var intent: Intent

    private var localExterior = ArrayList<Image>()
    private var localInteriorList = ArrayList<Image>()
    private var localMiscList = ArrayList<Image>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intent = requireActivity().intent

        viewModel.projectUuid = intent.getStringExtra(AppConstants.PROJECT_UUIID)
        viewModel.projectId = intent.getStringExtra(AppConstants.PROJECT_ID)
        viewModel.skuUuid = intent.getStringExtra(AppConstants.SKU_UUID)
        viewModel.skuId = intent.getStringExtra(AppConstants.SKU_ID)
        viewModel.skuName = intent.getStringExtra(AppConstants.SKU_NAME)

        frontFramesList = ArrayList()
        imageListNerf = ArrayList()

        when (intent.getStringExtra(AppConstants.CATEGORY_ID)) {
            AppConstants.FOOD_AND_BEV_CATEGORY_ID, AppConstants.ECOM_CATEGORY_ID, AppConstants.FOOTWEAR_CATEGORY_ID -> {
                binding.tvInterior.visibility = View.GONE
                binding.tvFocused.visibility = View.GONE
                binding.llThreeSixtyView.visibility = View.GONE
            }
        }

        viewModel.showSpyne360.observe(viewLifecycleOwner) {
            if (it) {
                showThreeSixtyView()
                binding.tvNotSatisfied?.visibility = View.GONE
            }
        }

        if (!intent.getStringExtra(AppConstants.VIDEO_URL_360).isNullOrEmpty()) {
            binding.switchEdited?.visibility = View.VISIBLE
        }

        binding.tvNotSatisfied?.setOnClickListener {
            Request360Dialog().show(
                requireActivity().supportFragmentManager,
                "Request360Dialog"
            )
        }


        exteriorImageList = ArrayList()
        imageListWaterMark = ArrayList<String>()
        listHdQuality = ArrayList<String>()

        getSubCategories()

        setListeners()

        imageNameList = ArrayList()


        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(requireContext(), AppConstants.CATEGORY_NAME)!!

        //checkThreeSixtyInterior()


        if (Utilities.getBool(context, AppConstants.CHECK_QC, false)) {
            binding.tvReshoot.visibility = View.INVISIBLE
            binding.llWatermark.visibility = View.VISIBLE
        } else {
            binding.tvReshoot.visibility = View.VISIBLE
            binding.llWatermark.visibility = View.VISIBLE
        }

        observeSkuData()

        binding.tvReshoot.setOnClickListener {
            viewModel.reshoot.value = true
        }



        viewModel.downloadImage.observe(viewLifecycleOwner) {
            if (it) {
                downloadImage()
            }
        }


    }

    private fun getSubCategories() {
        Utilities.showProgressDialog(requireContext())

        viewModel.getSubCategories(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )

        viewModel.subCategoriesResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    setBulkImages()
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getSubCategories() }
                }
                else -> {}
            }
        }
    }

    private fun observeSkuData() {

        viewModel.imagesOfSkuRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    var dataList: List<Image> = it.value.data
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.TOTAL_IMAGES,
                        dataList.size.toString()
                    )


                    dataList.forEach { list ->
                        if (list.qcStatus == "reshoot") {
                            binding.tvReshoot.visibility = View.VISIBLE
                        }
                    }

                    localExterior = dataList?.filter {
                        it.image_category == "Exterior"
                    } as ArrayList

                    localInteriorList = dataList?.filter {
                        it.image_category == "Interior"
                    } as ArrayList

                    localMiscList = dataList?.filter {
                        it.image_category == "Focus Shoot"
                    } as ArrayList

                    for (i in 0..(dataList.size) - 1) {
                        if (dataList[i].image_category.equals("Exterior") || dataList[i].image_category.equals(
                                "360_exterior"
                            )
                        ) {
                            Category = dataList[i].image_category
                            exteriorImageList.add(dataList[i])

                            (imageListWaterMark as ArrayList).add(dataList[i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList[i].output_image_hres_url)

                            imageNameList.add(dataList[i].name)
                            frontFramesList.add(dataList[i].output_image_lres_url)

                            if (dataList[i].image_category.equals("3D_Model")) {
                                binding.switchEdited?.visibility = View.VISIBLE
                                if (!dataList[i].output_image_lres_url.isNullOrEmpty()) {
                                    imageListNerf.add(dataList[i].output_image_lres_url)
                                } else {
                                    imageListNerf.add(dataList[i].output_image_hres_url)

                                }

                            }

                            if (Utilities.getBool(context, AppConstants.CHECK_QC, false)) {
                                if (dataList[i].qcStatus == "reshoot") {
                                    binding.tvReshoot.visibility = View.VISIBLE
                                }
                            }

                            if (Utilities.getBool(context, AppConstants.CHECK_QC, false)) {
                                if (dataList[i].qcStatus != "approved") {
                                    if (dataList[i].qcStatus != "qc_done") {
                                        if (dataList[i].qcStatus != "in_progress") {
                                            binding.llWatermark.visibility = View.GONE
                                        }
                                    } else {
                                        binding.tvReshoot.visibility = View.INVISIBLE
                                    }
                                } else {
                                    binding.tvReshoot.visibility = View.INVISIBLE

                                }
                            }

                            if (dataList[i].image_category.equals("360_exterior")) {
                                binding.apply {
                                    tvInterior.visibility = View.GONE
                                    rvInteriors.visibility = View.GONE
                                    tvFocused.visibility = View.GONE
                                    rvFocused.visibility = View.GONE
                                    tvReshoot.visibility = View.GONE
                                }
                            }

                            hideData(0)
                        } else if (dataList[i].image_category.equals("Food") || dataList[i].image_category.equals(
                                "Food & Beverages"
                            ) || dataList[i].image_category.equals("E-Commerce") || dataList[i].image_category.equals(
                                "Ecom"
                            )
                        ) {
                            Category = dataList[i].image_category

                            exteriorImageList.add(dataList[i])

                            (imageListWaterMark as ArrayList).add(dataList[i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList[i].output_image_hres_url)

                            imageNameList.add(dataList[i].name)

                            binding.tvYourEmailIdReplaced.text = "Images"

                            if (Utilities.getBool(context, AppConstants.CHECK_QC, false)) {
                                if (dataList[i].qcStatus == "reshoot") {
                                    binding.tvReshoot.visibility = View.VISIBLE
                                }
                            }

                            if (Utilities.getBool(context, AppConstants.CHECK_QC, false)) {
                                if (dataList[i].qcStatus != "approved") {
                                    if (dataList[i].qcStatus != "qc_done") {
                                        if (dataList[i].qcStatus != "in_progress") {
                                            binding.llWatermark.visibility = View.GONE
                                        }
                                    } else {
                                        binding.tvReshoot.visibility = View.INVISIBLE
                                    }
                                } else {
                                    binding.tvReshoot.visibility = View.INVISIBLE
                                }
                            }

                            hideData(0)
                        } else {
                            Category = dataList[i].image_category
                            exteriorImageList.add(dataList[i])

                            (listHdQuality as ArrayList).add(dataList[i].output_image_hres_url)
                            (imageListWaterMark as ArrayList).add(dataList[i].output_image_lres_wm_url)
                            imageNameList.add(dataList[i].name)

                            if (Utilities.getBool(context, AppConstants.CHECK_QC, false)) {
                                if (dataList[i].qcStatus == "reshoot") {
                                    binding.tvReshoot.visibility = View.VISIBLE
                                }
                            }

                            if (Utilities.getBool(context, AppConstants.CHECK_QC, false)) {
                                if (dataList[i].qcStatus != "approved") {
                                    if (dataList[i].qcStatus != "qc_done") {
                                        if (dataList[i].qcStatus != "in_progress") {
                                            binding.llWatermark.visibility = View.GONE
                                        }
                                    } else {
                                        binding.tvReshoot.visibility = View.INVISIBLE
                                    }
                                } else {
                                    binding.tvReshoot.visibility = View.INVISIBLE
                                }
                            }

                            hideData(1)
                        }
                    }


                    //show 360 view
                    if (intent.getBooleanExtra(
                            AppConstants.IS_360,
                            false
                        ) && imageListNerf.size != 0
                    ) {
                        binding.switchEdited?.isChecked = true
                        AppConstants.IS_NERF_AVAILABLE = true
                        showThreeSixtyNerfView()
                    } else if (intent.getBooleanExtra(
                            AppConstants.IS_360,
                            false
                        ) && frontFramesList.size != 0
                    ) {
                        binding.switchEdited?.isChecked = false
                        AppConstants.IS_NERF_AVAILABLE = false
                        showThreeSixtyView()
                    }

                    if (::showReplacedImagesAdapter.isInitialized)
                        showReplacedImagesAdapter.notifyDataSetChanged()




                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { fetchBulkUpload() }
                }
                else -> {}
            }
        }
    }

    private fun hideData(i: Int) {

        if (i == 0) {
            binding.tvYourEmailIdReplaced.visibility = View.VISIBLE
            // binding.tvViewGif.visibility = View.GONE
            binding.tvInterior.visibility = View.GONE
            binding.tvFocused.visibility = View.GONE
//            llDownloads.visibility = View.VISIBLE
        } else {
            binding.tvYourEmailIdReplaced.visibility = View.GONE
            // binding.tvViewGif.visibility = View.GONE
            //binding.tvInterior.visibility = View.GONE
            //binding.tvFocused.visibility = View.GONE
//            llDownloads.visibility = View.GONE
        }
    }

    private fun setListeners() {

        binding.ivBackShowImages?.setOnClickListener(View.OnClickListener {
            requireActivity().onBackPressed()
        })

        binding.ivHomeShowImages.setOnClickListener(View.OnClickListener {
            requireActivity().gotoHome()

            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                requireContext(),
                AppConstants.FRAME_LIST, updateSkuResponseList
            )
        })

//        if (getString(R.string.app_name) == AppConstants.SWEEP){
//            binding.tvDownloadFree.visibility = View.GONE
//        }else {
        binding.llWatermark.setOnClickListener {

            if (Utilities.getBool(requireContext(), AppConstants.NEW_ENTERPRISE_USER, false)) {
                downloadImage()
            } else {
                CreditCalculationFragment().show(
                    requireActivity().supportFragmentManager,
                    "PaymentFragmentDialog"
                )
            }
        }


        binding.llDownloadHdImages.setOnClickListener {

            Utilities.savePrefrence(
                requireContext(),
                AppConstants.NO_OF_IMAGES,
                listHdQuality.size.toString()
            )
            Utilities.savePrefrence(requireContext(), AppConstants.DOWNLOAD_TYPE, "hd")
            val orderIntent = Intent(requireContext(), OrderSummary2Activity::class.java)
            orderIntent.putExtra(AppConstants.LIST_WATERMARK, imageListWaterMark)
            orderIntent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
            orderIntent.putExtra(
                AppConstants.CATEGORY_ID,
                intent.getStringExtra(AppConstants.CATEGORY_ID)
            )
            orderIntent.putExtra(AppConstants.LIST_IMAGE_NAME, imageNameList)
            orderIntent.putExtra("is_paid", intent.getBooleanExtra("is_paid", false))

            var skuId = Utilities.getPreference(requireContext(), AppConstants.SKU_ID)
                .toString()

            var skuName = Utilities.getPreference(requireContext(), AppConstants.SKU_ID)
                .toString()

            orderIntent.putExtra(AppConstants.SKU_ID, skuId)
            orderIntent.putExtra(AppConstants.SKU_NAME, skuName)
            orderIntent.putExtra(
                AppConstants.IMAGE_TYPE,
                intent.getStringExtra(AppConstants.IMAGE_TYPE)
            )
            startActivity(orderIntent)
        }

        binding.ivShare.setOnClickListener(this)
        binding.ivEmbed.setOnClickListener(this)



        binding.switchEdited?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppConstants.IS_NERF_AVAILABLE = true
                showThreeSixtyNerfView()
            } else {
                AppConstants.IS_NERF_AVAILABLE = false
                showThreeSixtyView()
            }
        }


    }


    private fun setBulkImages() {
        Utilities.showProgressDialog(requireContext())
        showReplacedImagesAdapter = ShowReplacedImagesAdapter(requireContext(),
            exteriorImageList,
            object : ShowReplacedImagesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    showImagesDialog(position)
                    Log.e("position preview", position.toString())
                }
            })



        binding.rvImagesBackgroundRemoved.layoutManager = ScrollingLinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        binding.rvInteriors.layoutManager = GridLayoutManager(
            requireContext(),
            2
        )

        binding.rvFocused.layoutManager = GridLayoutManager(
            requireContext(),
            2
        )

        binding.rvImagesBackgroundRemoved.adapter = showReplacedImagesAdapter

        fetchBulkUpload()
    }

    //Fetch bulk data
    private fun fetchBulkUpload() {
        //Utilities.showProgressDialog(requireContext())

        shootId = requireActivity().intent.getStringExtra(AppConstants.SKU_ID).toString()

        getSkuImages()
    }

    private fun showThreeSixtyView() {
        binding.llThreeSixtyView.visibility = View.VISIBLE
        binding.threeSixtyView?.visibility = View.VISIBLE
        binding.nerfThreeSixtyView?.visibility = View.INVISIBLE
        binding.threeSixtyView?.init(frontFramesList)

    }

    private fun showThreeSixtyNerfView() {
        binding.llThreeSixtyView.visibility = View.VISIBLE
        binding.threeSixtyView?.visibility = View.INVISIBLE
        binding.nerfThreeSixtyView?.visibility = View.VISIBLE
        binding.nerfThreeSixtyView?.init(imageListNerf)
    }

    fun showImagesDialog(position: Int) {
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

        carouselViewImages.pageCount = exteriorImageList.size;
        carouselViewImages.setViewListener(viewListener);
        carouselViewImages.currentItem = position

        dialog.show()
    }

    var viewListener = object : ViewListener {
        override fun setViewForPosition(position: Int): View? {
            val customView: View = layoutInflater.inflate(R.layout.view_images, null)
            val customBiding = ViewImagesBinding.bind(customView)

            Glide.with(requireContext())
                .load(
                    if (exteriorImageList[position].input_image_lres_url.isNullOrEmpty())
                        exteriorImageList[position].input_image_hres_url
                    else
                        exteriorImageList[position].input_image_lres_url
                )
                .thumbnail(Glide.with(requireContext()).load(R.drawable.placeholder_gif))
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

//            Glide.with(this@ShowImagesActivity) // replace with 'this' if it's in activity
//                .load(imageListAfter[position])
//                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
//                .into(customView.ivAfter)

            Glide.with(requireContext())
                .load(
                    if (exteriorImageList[position].output_image_lres_url.isNullOrEmpty())
                        exteriorImageList[position].output_image_hres_url
                    else
                        exteriorImageList[position].output_image_lres_url
                )
                .thumbnail(Glide.with(requireContext()).load(R.drawable.placeholder_gif))
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


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivEmbed -> {
                embed(getCode(0))
            }

            R.id.ivShare -> {
                share(getLink())
            }

            R.id.tv_go_to_home -> {
                requireActivity().gotoHome()
            }
        }
    }

    private fun embed(code: String) {
        var args = Bundle()
        args.putString("code", code)

        var dialogCopyEmbeddedCode = DialogEmbedCode()
        dialogCopyEmbeddedCode.arguments = args
        dialogCopyEmbeddedCode.show(requireActivity().supportFragmentManager, "DialogEmbedCode")
    }

    private fun share(code: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, code)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun getCode(type: Int): String {
        return "<iframe \n" +
                "  src=\"https://www.spyne.ai/shoots/shoot?skuId=" + shootId + "&type=exterior" +
                "  style=\"border:0; height: 100%; width: 100%;\" framerborder=\"0\"></iframe>"

    }

    private fun getLink() =
        "https://www.spyne.ai/category/hotspot-view?skuId=" + shootId + "&type=exterior"


    private fun getSkuImages() {
        // Utilities.showProgressDialog(requireContext())

        viewModel.getImages(
            requireActivity().intent.getStringExtra(AppConstants.SKU_ID),
            requireActivity().intent.getStringExtra(AppConstants.PROJECT_UUIID).toString(),
            requireActivity().intent.getStringExtra(AppConstants.SKU_UUID).toString(),
            requireActivity().intent.getStringExtra(AppConstants.SKU_NAME)
        )
    }




    fun downloadImage() {
        Utilities.savePrefrence(
            requireContext(),
            AppConstants.NO_OF_IMAGES,
            listHdQuality.size.toString()
        )
        Utilities.savePrefrence(requireContext(), AppConstants.DOWNLOAD_TYPE, "hd")
        val downloadIntent = Intent(requireContext(), DownloadingActivity::class.java)
        downloadIntent.putExtra(AppConstants.LIST_WATERMARK, imageListWaterMark)
        downloadIntent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
        downloadIntent.putExtra(AppConstants.LIST_IMAGE_NAME, imageNameList)
        downloadIntent.putExtra(
            AppConstants.CATEGORY_ID,
            intent.getStringExtra(AppConstants.CATEGORY_ID)
        )
        downloadIntent.putExtra("is_paid", intent.getBooleanExtra("is_paid", false))

        var skuId = Utilities.getPreference(requireContext(), AppConstants.SKU_ID)
            .toString()

        var skuName = Utilities.getPreference(requireContext(), AppConstants.SKU_ID)
            .toString()

        downloadIntent.putExtra(AppConstants.SKU_ID, skuId)
        downloadIntent.putExtra(AppConstants.SKU_NAME, skuName)
        downloadIntent.putExtra(
            AppConstants.IMAGE_TYPE,
            intent.getStringExtra(AppConstants.IMAGE_TYPE)
        )

        startActivity(downloadIntent)
    }

    override fun getViewModel() = ProcessedViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProcessedImagesBinding.inflate(inflater, container, false)




}