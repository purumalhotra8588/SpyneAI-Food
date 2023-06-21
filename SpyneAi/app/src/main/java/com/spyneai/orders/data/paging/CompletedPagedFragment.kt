package com.spyneai.orders.data.paging

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentOngoingProjectsBinding
import com.spyneai.handleFirstPageError
import com.spyneai.isInternetActive
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class CompletedPagedFragment : BaseFragment<MyOrdersViewModel, FragmentOngoingProjectsBinding>() {

    val TAG = "PagedFragment"
    lateinit var adapter: ProjectPagedAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProjectPagedAdapter(
            requireContext(),
            arguments?.getString("status").toString()
        )

        viewModel.viewType.observe(
            viewLifecycleOwner
        ) {
            if (it && isAdded) {
                binding.rvMyOngoingProjects.layoutManager =
                    GridLayoutManager(
                        requireContext(), 2, GridLayoutManager.VERTICAL,
                        false
                    )
                val loaderStateAdapter = LoaderStateAdapter { adapter.retry() }
                binding.rvMyOngoingProjects.adapter =
                    adapter.withLoadStateFooter(loaderStateAdapter)
            } else {
                binding.rvMyOngoingProjects.layoutManager =
                    LinearLayoutManager(
                        requireContext(), LinearLayoutManager.VERTICAL,
                        false
                    )
                val loaderStateAdapter = LoaderStateAdapter { adapter.retry() }
                binding.rvMyOngoingProjects.adapter =
                    adapter.withLoadStateFooter(loaderStateAdapter)
            }
        }

        val loaderStateAdapter = LoaderStateAdapter { adapter.retry() }
        binding.rvMyOngoingProjects.adapter = adapter.withLoadStateFooter(loaderStateAdapter)

        adapter.addLoadStateListener { loadState ->
            when {
                adapter.itemCount == 0 -> {
                    val error = handleFirstPageError(loadState) { adapter.retry() }
                    if (error || loadState.append.endOfPaginationReached)
                        stopLoader()
                }

                adapter.itemCount > 0 -> stopLoader()

                loadState.append.endOfPaginationReached -> stopLoader()

                else -> stopLoader()
            }

        }

        binding.swiperefreshOngoing.setOnRefreshListener {
            fetchProjects(arguments?.getString("status").toString(), true)
        }

        fetchProjects(arguments?.getString("status").toString(), true)

//        if (arguments?.getString("status").toString() != "draft")
//            refreshData()
    }

    private fun refreshData() {
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            fetchProjects(arguments?.getString("status").toString(), false)
            refreshData()
        }, 30000)
    }

    private fun stopLoader() {
        binding.shimmerCompletedSKU.stopShimmer()
        binding.shimmerCompletedSKU.visibility = View.GONE
        binding.rvMyOngoingProjects.visibility = View.VISIBLE
    }

    private fun fetchProjects(status: String, showShimmer: Boolean = true) {
        if (showShimmer) {
            binding.shimmerCompletedSKU.startShimmer()
            binding.shimmerCompletedSKU.visibility = View.VISIBLE
            binding.rvMyOngoingProjects.visibility = View.GONE
        }

        if (viewModel.moveToZero) {
            binding.rvMyOngoingProjects.layoutManager?.scrollToPosition(0)
            viewModel.moveToZero = false
        }

        try {
            viewModel.updateCompletedProjects.observe(viewLifecycleOwner) {
                if (it) {
                    viewModel.updateCompletedProjects.value = false
                    fetchProjects(arguments?.getString("status").toString(), false)
                }
            }
        } catch (e: Exception) {

        }

        getProjects(status)
    }

    private fun getCompletedProjects() {
        binding.shimmerCompletedSKU.startShimmer()
        binding.shimmerCompletedSKU.visibility = View.VISIBLE
        binding.rvMyOngoingProjects.visibility = View.GONE

        viewModel.getPagedCompletedProjects()
    }

    private fun observeCompletedProjects() {
        viewModel.getCompletedPagedResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    //save project to data base and fetch projects again
                    if (it.value.data.isNotEmpty()) {
                        GlobalScope.launch(Dispatchers.IO) {
                            viewModel.savetoDB(it.value)

                            GlobalScope.launch(Dispatchers.Main) {
                                fetchProjects(arguments?.getString("status").toString(), false)
                            }
                        }
                    } else {
                        fetchProjects(arguments?.getString("status").toString(), false)
                    }

                }

                is Resource.Failure -> {
                    binding.shimmerCompletedSKU.stopShimmer()
                    binding.shimmerCompletedSKU.visibility = View.GONE
                    handleApiError(it) { getCompletedProjects() }
                }

                else -> {

                }
            }
        }
    }


    private fun getProjects(status: String) {
        lifecycleScope.launch {
            viewModel.getAllProjects(status).distinctUntilChanged().collectLatest {
                adapter.submitData(it)
                binding.swiperefreshOngoing.isRefreshing = false
            }
        }
    }

    override fun getViewModel() = MyOrdersViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOngoingProjectsBinding.inflate(inflater, container, false)
}