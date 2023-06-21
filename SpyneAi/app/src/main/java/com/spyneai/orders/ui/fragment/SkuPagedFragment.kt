package com.spyneai.orders.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.FragmentSkuPagedBinding
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.ImageNotSyncedDialog
import com.spyneai.draft.ui.adapter.SkuPagedAdapter
import com.spyneai.gotoHome
import com.spyneai.handleFirstPageError
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.paging.LoaderStateAdapter
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.shootapp.ui.base.ProjectDetailsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class SkuPagedFragment : BaseFragment<DraftViewModel, FragmentSkuPagedBinding>(),
    OnItemClickListener {

    lateinit var intent: Intent
    var position = 0
    lateinit var adapter: SkuPagedAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (getString(R.string.app_name) == AppConstants.SWIGGY) {
            binding.tvProject.text = "Restaurant Name"
            binding.tvSku.text = "Total Dishes"
        }

        intent = requireActivity().intent

        Utilities.savePrefrence(
            requireContext(),
            AppConstants.TAB_ID,
            intent.getStringExtra(AppConstants.TAB_ID)
        )

        intent.getStringExtra(AppConstants.STATUS)?.let {
            adapter = SkuPagedAdapter(
                requireContext(),
                it,
                this
            )
        }


        if (!Utilities.getBool(BaseApplication.getContext(), "viewTypeGrid", false)) {
            binding.rvSkus.layoutManager =
                LinearLayoutManager(
                    requireContext(), LinearLayoutManager.VERTICAL,
                    false
                )
        } else {
            binding.rvSkus.layoutManager =
                GridLayoutManager(
                    requireContext(), 2, GridLayoutManager.VERTICAL,
                    false
                )
        }


        val loaderStateAdapter = LoaderStateAdapter { adapter.retry() }
        binding.rvSkus.adapter = adapter.withLoadStateFooter(loaderStateAdapter)

        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        position = intent.getIntExtra("position", 0)!!

        binding.tvProjectName.text = intent.getStringExtra(AppConstants.PROJECT_NAME)
        binding.tvTotalSku.text = intent.getIntExtra(AppConstants.SKU_COUNT, 0).toString()

        binding.shimmerCompletedSKU.startShimmer()

        fetchSkus()

        viewModel.syncImages.observe(viewLifecycleOwner) {
            fetchSkus()
        }

        adapter.addLoadStateListener { loadState ->
            when {
                adapter.itemCount == 0 -> {
                    val error = handleFirstPageError(loadState) { adapter.retry() }
                    if (error || loadState.append.endOfPaginationReached)
                        stopLoader()
                }

                adapter.itemCount > 0 -> stopLoader()

                loadState.append.endOfPaginationReached -> {
                    stopLoader()
                    if (!requireContext().isInternetActive() && (adapter.itemCount == 0 && intent.getIntExtra(
                            AppConstants.SKU_COUNT,
                            0
                        ) > adapter.itemCount)
                    ) {
                        ImageNotSyncedDialog()
                            .apply {
                                arguments = Bundle().apply {
                                    putBoolean("sku_sync", true)
                                }
                            }
                            .show(
                                requireActivity().supportFragmentManager,
                                "ImageNotSyncedDialog"
                            )
                    }
                }
            }

            adapter.addLoadStateListener { loadState ->
                if (adapter.itemCount == 0) {
                    handleFirstPageError(loadState) { adapter.retry() }
                }
            }
        }
    }

    private fun stopLoader() {
        binding.shimmerCompletedSKU.stopShimmer()
        binding.shimmerCompletedSKU.visibility = View.GONE
        binding.rvSkus.visibility = View.VISIBLE
    }

    private fun fetchSkus() {
        binding.shimmerCompletedSKU.startShimmer()
        binding.shimmerCompletedSKU.visibility = View.VISIBLE
        binding.rvSkus.visibility = View.GONE

        GlobalScope.launch(Dispatchers.IO) {
            getSkus()
        }
    }


    @ExperimentalPagingApi
    private fun getSkus() {
        lifecycleScope.launch {
            viewModel.getSkus(
                intent.getStringExtra(AppConstants.PROJECT_ID),
                intent.getStringExtra(AppConstants.PROJECT_UUIID)!!,
                1
            ).distinctUntilChanged().collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun getViewModel() = DraftViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSkuPagedBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
        if (data is Sku) {
//            data?.let {
//                val builder = it.subcategoryId?.let { it1 ->
//                    Spyne.ShootBuilder(
//                        context = requireContext(),
//                        userId = Utilities.getPref(context, AppConstants.USER_ID).toString(),
//                        this,
//                        it.initialFrames!!
//                    )
//                        .uniqueShootId(it.skuName.toString())
//                        .spyneSkuId(it.uuid)
//                        .shootType(if (it.shootType == "upload_flow") ShootType.UPLOAD else ShootType.SHOOT)
//                        .classifier(Classifier.RESTRICTIVE)
//                        .gyroMeter(Gyrometer.RESTRICTIVE)
//                        .environment(if (AppConstants.BASE_URL == "https://beta-api.spyne.xyz/") "TESTING" else "PRODUCTION")
//                        .syncProject(false)
//                        .outputPreview(OutputPreview.OFF)
//                        .subcategoryId(it1)
//                }
//
//                Utilities.getPreference(requireContext(), AppConstants.TEAM_ID)?.let { teamId ->
//                    builder?.metaData(teamId)
//                }
//
//
//                it.processDataMap?.let {
//                    builder?.processData(it)
//                }
//
//                val spyne = builder?.build()
//
//                if (data.subcategoryId == data.categoryId) {
//                    spyne?.startVideoThreeSixty()
//                } else
//                    spyne?.start()
//            }
        }
    }


}