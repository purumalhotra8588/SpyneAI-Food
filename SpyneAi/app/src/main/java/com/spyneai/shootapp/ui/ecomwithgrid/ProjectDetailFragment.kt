package com.spyneai.shootapp.ui.ecomwithgrid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.*
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentProjectDetailBinding
import com.spyneai.service.ServerSyncTypes
import com.spyneai.shootapp.adapters.ProjectDetailAdapter
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.repository.model.project.ProjectWithSkuAndImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProjectDetailFragment : BaseFragment<ShootViewModelApp, FragmentProjectDetailBinding>() {

    lateinit var projectDetailAdapter: ProjectDetailAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //update sku
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.setProjectAndSkuData(
                viewModel.projectApp?.uuid!!,
                viewModel.skuApp?.uuid!!
            )
        }

        binding.btHome.setOnClickListener {
            //mark project selectable
            GlobalScope.launch(Dispatchers.IO) {
                viewModel.updateProject()

                GlobalScope.launch(Dispatchers.Main) {
                    //start submission project sync
                    requireContext().startUploadingService(
                        ProjectDetailFragment::class.java.simpleName,
                        ServerSyncTypes.SUBMIT_PROJECT
                    )
                    requireContext().gotoHome()
                }
            }
        }

        binding.ivBackGif.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.tvTotalSkuCaptured.text = viewModel.projectApp?.skuCount.toString()
        binding.tvTotalImageCaptured.text = viewModel.projectApp?.imagesCount.toString()

        val dataList = ArrayList<ProjectWithSkuAndImages>()

        GlobalScope.launch(Dispatchers.IO) {
            val list = viewModel.getProjectSkus()

            list.forEach {
                dataList.add(
                    ProjectWithSkuAndImages(
                        it,
                        viewModel.getImagesbySkuId(it.uuid)
                    )
                )
            }

            GlobalScope.launch(Dispatchers.Main) {
                projectDetailAdapter = ProjectDetailAdapter(
                    requireContext(),
                    dataList
                )

                binding.rvParentProjects.apply {
                    this?.layoutManager =
                        LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                    this?.adapter = projectDetailAdapter
                }
            }
        }
    }


    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProjectDetailBinding.inflate(inflater, container, false)

}