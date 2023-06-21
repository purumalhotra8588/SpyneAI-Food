package com.spyneai.shootapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.enable
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSelectBackgroundBinding
import com.spyneai.databinding.ItemProcessCheckboxBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import com.spyneai.service.ServerSyncTypes
import com.spyneai.shootapp.adapters.NewCarMarketplaceAdapter
import com.spyneai.shootapp.data.ProcessViewModelApp
import com.spyneai.shootapp.data.model.CarsBackgroundRes
import com.spyneai.shootapp.data.model.MarketplaceRes
import com.spyneai.shootapp.ui.dialogs.ShootExitDialog
import com.spyneai.startUploadingService
import com.spyneai.threesixty.VideoBackgroundAdapter
import kotlinx.android.synthetic.main.fragment_select_background.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SelectBackgroundFragment :
    BaseFragment<ProcessViewModelApp, FragmentSelectBackgroundBinding>() {

    val TAG = "Background Fragment"
    private val bindingMap = HashMap<String, ViewBinding>()
    lateinit var carBackgroundAppGifList: ArrayList<CarsBackgroundRes.BackgroundApp>
    lateinit var carBackgroundAppFilteredList: ArrayList<CarsBackgroundRes.BackgroundApp>
    lateinit var marketplaceList: ArrayList<MarketplaceRes.Marketplace>
    lateinit var finalMarketPlaceList: ArrayList<MarketplaceRes.Marketplace>
    lateinit var carbackgroundsAdapter: VideoBackgroundAdapter
    lateinit var carMarketplaceAdapter: NewCarMarketplaceAdapter
    var noRecentBg = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rlMultiWall.isVisible = Utilities.getPreference(
            requireContext(),
            AppConstants.SELECTED_CATEGORY_ID
        ) == AppConstants.CARS_CATEGORY_ID



        binding.clFoodAiOutput.isVisible = (Utilities.getPreference(
            requireContext(),
            AppConstants.SELECTED_CATEGORY_ID
        ) == AppConstants.FOOD_AND_BEV_CATEGORY_ID)

        carBackgroundAppGifList = ArrayList()
        carBackgroundAppFilteredList = ArrayList()
        marketplaceList = ArrayList()
        finalMarketPlaceList = ArrayList()

        if (viewModel.category?.fetchMarketplace == true) {
            binding.tvSample.visibility = View.GONE
            binding.imageViewGif.visibility = View.GONE
            binding.tvToolbar.text = "Marketplace"
            binding.tvMarketPlace.text = "Where do you want to list"
        }

        initSelectBackground()

        if (viewModel.categoryId == null) {
            arguments?.let {
                viewModel.categoryId = it.getString(AppConstants.CATEGORY_ID)
                val projectUuid = it.getString(AppConstants.PROJECT_UUIID)!!
                val skuUUid = it.getString(AppConstants.SKU_UUID)!!

                GlobalScope.launch(Dispatchers.IO) {
                    viewModel.setProjectAndSkuData(
                        projectUuid,
                        skuUUid
                    )
                }
            }
        }

        if (viewModel.categoryId == AppConstants.CARS_CATEGORY_ID) {
            binding.tvSample.text = "Sample 360° Output"
        } else if(viewModel.categoryId == AppConstants.FOOD_AND_BEV_CATEGORY_ID) {
            binding.tvSample.text = "Background Preview"
        }else{
            binding.tvSample.text = "Sample Output"
        }

        //set processing options
        viewModel.category?.processParams?.let { list ->
            list.forEach {
                addBinding(
                    it.fieldId,
                    getItemBinding(it.fieldName, it.fieldType, it.fieldId, it.defaultValue)
                )
            }
        }

        binding.ivBackGif.setOnClickListener {
            ShootExitDialog().show(requireFragmentManager(), "ShootExitDialog")
        }

        binding.tvGenerateGif.setOnClickListener {
            //update total frame if user clicked interior and misc
            processRequest()
        }
        listners()
    }

    private fun listners() {


        binding.clFoodAiOutput.setOnClickListener {
            viewModel.getImageUrl()
        }


        binding.tvSingleWall.setOnClickListener {

            carBackgroundAppFilteredList.clear()
            carBackgroundAppFilteredList.addAll(carBackgroundAppGifList.filter { it.backgroundType == "SINGLEWALL" } as ArrayList<CarsBackgroundRes.BackgroundApp>)
            carbackgroundsAdapter.notifyDataSetChanged()

            binding.tvSingleWall.setBackgroundResource(com.spyneai.R.drawable.bg_multi_wall_selected)
            binding.tvMultiWall.setBackgroundResource(com.spyneai.R.drawable.bg_multi_wall)
            binding.tvSingleWall.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    com.spyneai.R.color.primary
                )
            )
            binding.tvMultiWall.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    com.spyneai.R.color.categories_text
                )
            )

            if (carBackgroundAppFilteredList.size > 0)
                viewModel.backgroundAppSelect = carBackgroundAppFilteredList[0]
        }



        binding.tvMultiWall.setOnClickListener {

            carBackgroundAppFilteredList.clear()
            carBackgroundAppFilteredList.addAll(carBackgroundAppGifList.filter { it.backgroundType == "MULTIWALL" } as ArrayList<CarsBackgroundRes.BackgroundApp>)

            if (carBackgroundAppFilteredList.size == 0) {
                Toast.makeText(
                    requireContext(),
                    "Multi Wall Backgrounds not Available!!",
                    Toast.LENGTH_SHORT
                ).show()
                carBackgroundAppFilteredList.clear()
                carBackgroundAppFilteredList.addAll(carBackgroundAppGifList.filter { it.backgroundType == "SINGLEWALL" } as ArrayList<CarsBackgroundRes.BackgroundApp>)
                carbackgroundsAdapter.notifyDataSetChanged()
                binding.tvSingleWall.setBackgroundResource(R.drawable.bg_multi_wall_selected)
                binding.tvMultiWall.setBackgroundResource(R.drawable.bg_multi_wall)
                binding.tvSingleWall.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.primary
                    )
                )
                binding.tvMultiWall.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.categories_text
                    )
                )

                if (carBackgroundAppFilteredList.size > 0)
                    viewModel.backgroundAppSelect = carBackgroundAppFilteredList[0]


            } else {
                carbackgroundsAdapter.notifyDataSetChanged()
                binding.tvMultiWall.setBackgroundResource(com.spyneai.R.drawable.bg_multi_wall_selected)
                binding.tvSingleWall.setBackgroundResource(com.spyneai.R.drawable.bg_multi_wall)
                binding.tvMultiWall.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.spyneai.R.color.primary
                    )
                )
                binding.tvSingleWall.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.spyneai.R.color.categories_text
                    )
                )

                if (carBackgroundAppFilteredList.size > 0)
                    viewModel.backgroundAppSelect = carBackgroundAppFilteredList[0]
            }
        }
    }

    private fun addBinding(filedId: String, binding: ViewBinding) {
        bindingMap[filedId] = binding
    }

    private fun processRequest() {
        //get process params data
        val processDataMap = HashMap<String, Any>()

        viewModel.category?.processParams?.let { list ->
            list.forEach {
                processDataMap[it.fieldId] =
                    (bindingMap[it.fieldId] as ItemProcessCheckboxBinding).cb.isChecked
            }
        }
        viewModel.processDataMap = processDataMap
        processSku(processDataMap)
    }

    private fun getBackground() {
        val map = HashMap<String, Any>()

        map["prodCatId"] = viewModel.category?.categoryId.toString()

        if (viewModel.category?.fetchBackgroundsBy == "prodSubCatId" || viewModel.category?.fetchMarketplaceBy == "prodSubCatId")
            map["prodSubCatId"] = viewModel.skuApp?.subcategoryId.toString()

        map["auth_key"] =
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString()
        map["marketPlace"] = viewModel.category?.fetchMarketplace!!


        if (viewModel.category?.fetchBackgroundsBy == "prodCatId" && viewModel.category?.fetchMarketplaceBy == "prodCatId") {
            viewModel.categoryId?.let { viewModel.getBackgroundGifCars(it, map) }
        } else
            viewModel.skuApp?.subcategoryId?.let { viewModel.getBackgroundGifCars(it, map, true) }

    }

    private fun initSelectBackground() {
        getBackground()

        viewModel.carGifRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (noRecentBg) {
                        requireContext().captureEvent(Events.GET_BACKGROUND, HashMap())
                        binding.shimmer.stopShimmer()
                        binding.shimmer.visibility = View.GONE
                        binding.rvBackgroundsCars.visibility = View.VISIBLE
                        binding.tvGenerateGif.enable(true)
                        val response = it.value

                        // Add Data to List
                        carBackgroundAppGifList.clear()
                        marketplaceList.clear()
                        for (element in response.data) {
                            carBackgroundAppGifList.add(element)
                        }
                        for (element in response.marketPlace) {
                            marketplaceList.add(element)
                        }

                        marketplaceList = if (viewModel.category?.fetchMarketplaceBy == "prodCatId")
                            marketplaceList
                        else
                            marketplaceList.filter {
                                it.prod_sub_cat_id == viewModel.skuApp?.subcategoryId
                            } as ArrayList<MarketplaceRes.Marketplace>

                        // Load First background gif url
                        if (!viewModel.category!!.fetchMarketplace && carBackgroundAppGifList.size > 0) {
                            Glide.with(requireContext()) // replace with 'this' if it's in activity
                                .load(carBackgroundAppGifList[0].gifUrl)
                                .error(com.spyneai.R.mipmap.defaults) // show error drawable if the image is not a gif
                                .into(binding.imageViewGif)

                            viewModel.backgroundAppSelect = carBackgroundAppGifList[0]

                            setBackgroundsCar()
                        }

                        if (viewModel.category!!.fetchMarketplace && marketplaceList.size > 0) {
                            Glide.with(requireContext()) // replace with 'this' if it's in activity
                                .load(marketplaceList[0].market_place_img)
                                .error(com.spyneai.R.mipmap.defaults) // show error drawable if the image is not a gif
                                .into(binding.imageViewGif)

                            viewModel.marketplaceSelect = marketplaceList[0]

                            setMarketplaceCar()

                        }
                    }
                    tvMultiWall.enable(true)
                }

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_BACKGROUND_FAILED, HashMap<String, Any?>(),
                        it.errorMessage!!
                    )
                    handleApiError(it) { getBackground() }
                }

                is Resource.Loading -> binding.shimmer.startShimmer()
            }
        }
    }

    private fun setBackgroundsCar() {

        carBackgroundAppFilteredList.clear()
        carBackgroundAppFilteredList.addAll(carBackgroundAppGifList.filter { it.backgroundType == "SINGLEWALL" } as ArrayList<CarsBackgroundRes.BackgroundApp>)

        carbackgroundsAdapter = VideoBackgroundAdapter(requireContext(),
            carBackgroundAppFilteredList, 0,
            object : VideoBackgroundAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    //if (position<carBackgroundList.size)
                    viewModel.backgroundAppSelect = carBackgroundAppFilteredList[position]

                    carbackgroundsAdapter.notifyDataSetChanged()

                    Glide.with(requireContext()) // replace with 'this' if it's in activity
                        .load(carBackgroundAppFilteredList[position].gifUrl)
                        .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                        .into(binding.imageViewGif)

                }
            })
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL, false
            )

        binding.rvBackgroundsCars.layoutManager = layoutManager
        binding.rvBackgroundsCars.adapter = carbackgroundsAdapter
    }

    private fun setMarketplaceCar() {
        carMarketplaceAdapter = NewCarMarketplaceAdapter(requireContext(),
            marketplaceList, 0,
            object : NewCarMarketplaceAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    //if (position<carBackgroundList.size)
                    viewModel.marketplaceSelect = marketplaceList[position]

                    carMarketplaceAdapter.notifyDataSetChanged()

                    Glide.with(requireContext()) // replace with 'this' if it's in activity
                        .load(marketplaceList[position].market_place_img)
                        .error(com.spyneai.R.mipmap.defaults) // show error drawable if the image is not a gif
                        .into(binding.imageViewGif)

                }
            })
        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(
                requireContext(),
                3
            )

        binding.rvBackgroundsCars.layoutManager = layoutManager
        binding.rvBackgroundsCars.adapter = carMarketplaceAdapter
    }

    private fun processSku(map: HashMap<String, Any>) {

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.updateBackground(map)
            lifecycleScope.launch(Dispatchers.Main) {
                Utilities.hideProgressDialog()

                requireContext().captureEvent(
                    Events.PROCESS,
                    HashMap<String, Any?>()
                        .apply {
                            this.put("sku_id", viewModel.skuApp?.uuid!!)
                            this.put("background_id", viewModel.backgroundAppSelect?.imageId)
                            this.put("marketplace_id", viewModel.marketplaceSelect?.marketPlace_id)
                        }
                )

                //start sync service
                requireContext().startUploadingService(
                    SelectBackgroundFragment::class.java.simpleName,
                    ServerSyncTypes.PROCESS
                )

                viewModel.startTimer.value = true
            }
        }
    }

    fun getItemBinding(
        fieldName: String,
        fieldType: String,
        fieldId: String,
        default: Any
    ): ViewBinding {

        var tempBinding: ViewBinding? = null

        when (fieldType) {
            "checkbox" -> {
                val layout = LayoutInflater.from(requireContext())
                    .inflate(com.spyneai.R.layout.item_process_checkbox, null)
                val itemBinding = ItemProcessCheckboxBinding.bind(layout)

                itemBinding.tvTitle.text = fieldName
                itemBinding.cb.isChecked = default as Boolean


                tempBinding = itemBinding
                binding.llProcessParamsContainer.addView(layout)
            }
        }

        return tempBinding!!
    }


    override fun getViewModel() = ProcessViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectBackgroundBinding.inflate(inflater, container, false)

}