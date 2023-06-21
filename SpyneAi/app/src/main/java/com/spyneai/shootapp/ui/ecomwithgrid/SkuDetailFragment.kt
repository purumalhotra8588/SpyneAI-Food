package com.spyneai.shootapp.ui.ecomwithgrid

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.spyneai.SelectAnotherImagetypeDialog
import com.spyneai.base.BaseFragment
import com.spyneai.posthog.captureEvent
import com.spyneai.databinding.FragmentSkuDetailBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shootapp.adapters.SkuImageAdapter
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.ui.base.ShootPortraitActivity
import com.spyneai.shootapp.ui.ecomwithgrid.dialogs.EndProjectDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SkuDetailFragment : BaseFragment<ShootViewModelApp, FragmentSkuDetailBinding>() {

    lateinit var skuImageAdapter: SkuImageAdapter
    var totalSkuImages = 0
    var endProject = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.category?.shootExperience?.addMoreAngles?.let {
            if (it){
                binding.ivAddAngle.visibility = View.VISIBLE
                binding.tvAddAngle.visibility = View.VISIBLE
            }
        }


        GlobalScope.launch(Dispatchers.IO) {
            //update sku
            viewModel.setProjectAndSkuData(
                viewModel.projectApp?.uuid!!,
                viewModel.skuApp?.uuid!!
            )

            GlobalScope.launch(Dispatchers.Main) {
                viewModel.totalSkuCaptured.value = viewModel.projectApp?.skuCount.toString()
                viewModel.totalImageCaptured.value = viewModel.projectApp?.imagesCount

                binding.tvTotalSkuCaptured.text = viewModel.projectApp?.skuCount.toString()
            }
        }



        viewModel.getImagesPathBySkuId()?.observe(viewLifecycleOwner,{
            it?.let {
                totalSkuImages = it.size
                binding.tvTotalImageCaptured.text = it.size.toString()

                skuImageAdapter = SkuImageAdapter(
                    requireContext(),
                    it
                )

                binding.rvSkuImages.apply {
                    this?.layoutManager =
                        GridLayoutManager(requireContext(), 3)
                    this?.adapter = skuImageAdapter
                }
            }
        })

        binding.btNextSku.setOnClickListener {
            endProject = false
            processRequest()
        }

        if (Utilities.getPreference(requireContext(),AppConstants.ENTERPRISE_ID)
            != AppConstants.FLIPKART_ENTERPRISE_ID){
            if (binding.tvAddAngle.visibility == View.VISIBLE){
                binding.ivAddAngle.setOnClickListener {
                    if (viewModel.categoryDetails.value?.categoryName.equals("E-Commerce") ||
                        viewModel.categoryDetails.value?.categoryName.equals("Food & Beverages")||
                        viewModel.categoryDetails.value?.categoryName.equals("Photo Box"))

                        viewModel.addMoreAngle.value = true
                }

                binding.tvAddAngle.setOnClickListener {
                    if (viewModel.categoryDetails.value?.categoryName.equals("E-Commerce") ||
                        viewModel.categoryDetails.value?.categoryName.equals("Food & Beverages")||
                        viewModel.categoryDetails.value?.categoryName.equals("Photo Box"))

                            viewModel.addMoreAngle.value = true
                }
            }

        }else {
            if (binding.tvAddAngle.visibility == View.VISIBLE){
                binding.ivAddAngle.setOnClickListener {
                    SelectAnotherImagetypeDialog().show(
                        requireActivity().supportFragmentManager,
                        "Select_another_image_dialog"
                    )
                }
                binding.tvAddAngle.setOnClickListener {
                    SelectAnotherImagetypeDialog().show(
                        requireActivity().supportFragmentManager,
                        "Select_another_image_dialog"
                    )

                }
            }
        }


        binding.tvEndProject.setOnClickListener {
            endProject = true
            processRequest()
        }


        binding.ivBackGif.setOnClickListener {
            requireActivity().onBackPressed()
        }

    }





    private fun processRequest() {
        if (endProject) {
            EndProjectDialog().show(requireFragmentManager(), "EndProjectDialog")
        } else {
            nextSku()
        }
    }

    private fun nextSku() {

        GlobalScope.launch {
            viewModel.updateTotalFrames()

            GlobalScope.launch(Dispatchers.Main) {

                requireContext().captureEvent(Events.PROJECT_ADDED_TO_CART, HashMap<String, Any?>().apply {
                    this.put( viewModel.skuApp?.uuid!!, viewModel.skuApp?.imagesCount!!)
                })

                viewModel.shootList.value?.clear()
                val intent = Intent(activity, ShootPortraitActivity::class.java)

                intent.putExtra(AppConstants.PROJECT_UUIID, viewModel.projectApp?.uuid)
                intent.putExtra(
                    AppConstants.CATEGORY_NAME,
                    viewModel.category?.name
                )
                intent.putExtra(
                    AppConstants.CATEGORY_ID,
                    viewModel.category?.categoryId
                )

                startActivity(intent)
            }
        }
    }



    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSkuDetailBinding.inflate(inflater, container, false)

}