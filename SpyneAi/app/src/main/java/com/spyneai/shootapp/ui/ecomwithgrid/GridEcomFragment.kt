package com.spyneai.shootapp.ui.ecomwithgrid

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.spyneai.InfoDialog
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.FragmentGridEcomBinding
import com.spyneai.service.ServerSyncTypes
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ConfirmReshootEcomDialog
import com.spyneai.shootapp.adapters.ClickedAdapter
import com.spyneai.shootapp.data.OnOverlaySelectionListener
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.data.model.ShootData
import com.spyneai.shootapp.ui.dialogs.ConfirmTagsDialog
import com.spyneai.shootapp.ui.dialogs.ReclickDialog
import com.spyneai.startUploadingService
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class GridEcomFragment : BaseFragment<ShootViewModelApp, FragmentGridEcomBinding>(),
    OnItemClickListener, OnOverlaySelectionListener {

    val TAG = "GridEcomFragment"
    var clickedAdapter: ClickedAdapter? = null
    var position = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.showOverlay.value = false

        viewModel.isSubCategoryConfirmed.observe(viewLifecycleOwner) {
            if (it)
                binding.groupPointAngle.visibility = View.VISIBLE
        }

        viewModel.pointAngle.observe(viewLifecycleOwner) {
            if (it) {
                binding.groupPointAngle.visibility = View.GONE
            }
        }

        viewModel.showGrid.observe(viewLifecycleOwner) {
            if (it) {
                binding.groupGridLines?.visibility = View.VISIBLE
            } else binding.groupGridLines?.visibility = View.INVISIBLE
        }

        viewModel.category?.shootExperience?.let {
            if (it.perspectiveCropping) {
                binding.apply {
                    ivNext.visibility = View.VISIBLE
                    ivEnd.visibility = View.GONE
                }
            } else {
                binding.apply {
                    ivNext.visibility = View.GONE
                    ivEnd.visibility = View.VISIBLE
                }
            }
        }

        binding.ivEnd.setOnClickListener {
            if (viewModel.isStopCaptureClickable)
                viewModel.stopShoot.value = true

            if(viewModel.shootList.value?.size==1){
                GlobalScope.launch(Dispatchers.IO) {
                    viewModel.insertImage(viewModel.shootData.value!!)
                }

                requireContext().startUploadingService(
                    ConfirmTagsDialog::class.java.simpleName,
                    ServerSyncTypes.UPLOAD
                )
            }
        }

        binding.ivNext.setOnClickListener {
            if(viewModel.shootList.value?.size==1){
                InfoDialog().show(
                    requireActivity().supportFragmentManager,
                    "InfoDialog"
                )
            }
            else
                Toast.makeText(context, "Please click atleast one photo", Toast.LENGTH_SHORT).show()

        }

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner) {
            try {
                if (viewModel.showConfirmReshootDialog.value == true && !it.isNullOrEmpty()) {
                    val element = viewModel.getCurrentShoot()
                    showImageConfirmDialog(element!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // set sku name
        viewModel.isSkuCreated.observe(viewLifecycleOwner) {
            if (it) {
                Log.d(TAG, "onViewCreated: " + Gson().toJson(viewModel.skuApp))
                binding.tvSkuName?.text = viewModel.skuApp?.skuName
                binding.tvSkuName.visibility = View.VISIBLE
//                viewModel.isSkuCreated.value = false
            }
        }

        viewModel.hideLeveler.observe(viewLifecycleOwner) {
            if (viewModel.categoryDetails.value?.imageType == "Info") {
                binding.apply {
                    ivNext.visibility = View.GONE
                    ivEnd.visibility = View.VISIBLE
                }
            }
        }

        viewModel.onImageConfirmed.observe(viewLifecycleOwner) {
            viewModel.shootList.value?.let {
                if (viewModel.currentShoot < it.size){
                    binding.tvImageCount.text = it.size.toString()
                    it[viewModel.currentShoot].imageClicked = true
                    it[viewModel.currentShoot].isSelected = false
                    //update captured images
                    if (clickedAdapter == null) {
                        clickedAdapter = ClickedAdapter(it, this, this)
                        binding.rvClicked.apply {
                            layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                            adapter = clickedAdapter
                        }
                    } else {
                        try {
                            if (viewModel.isReclick) {
                                clickedAdapter?.notifyItemChanged(viewModel.currentShoot)
                            } else {
                                clickedAdapter?.notifyDataSetChanged()
                            }
                        } catch (e: Exception) {

                        }
                    }
                    viewModel.overlayId = it.size
                    viewModel.currentShoot = it.size
                    binding.rvClicked.scrollToPosition(it.size.minus(1))
                }
            }
        }

        viewModel.updateSelectItem.observe(viewLifecycleOwner) {
            if (it) {
                val list = clickedAdapter?.listItems as ArrayList<ShootData>
                //update previous selected item if have any
                list.firstOrNull {
                    it.isSelected
                }?.let {
                    it.isSelected = false
                    clickedAdapter?.notifyItemChanged(list.indexOf(it))
                }

                list[viewModel.currentShoot].isSelected = true
                clickedAdapter?.notifyItemChanged(viewModel.currentShoot)
                viewModel.updateSelectItem.value = false
            }
        }

        listners()
    }

    private fun listners() {
        binding.btAngle.setOnClickListener {
            binding.groupPointAngle.visibility = View.INVISIBLE
        }
    }


    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        when (viewModel.categoryDetails.value?.imageType) {
            "Info" -> {
                CropImage.activity(Uri.fromFile(File(shootData.capturedImage)))
                    .start(requireActivity())
            }
            else ->
                ConfirmReshootEcomDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
        }
    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when (data) {
            is ShootData -> {
                if (data.imageClicked) {
                    val bundle = Bundle()
                    bundle.putInt("overlay_id", data.overlayId)
                    bundle.putInt("position", position)
                    bundle.putString("name", data.name)
                    bundle.putString("image_type", data.image_category)
                    val reclickDialog = ReclickDialog()
                    reclickDialog.arguments = bundle
                    reclickDialog.show(requireActivity().supportFragmentManager, "ReclickDialog")
                }
            }
        }
    }

    override fun onOverlaySelected(view: View, position: Int, data: Any?) {
        // viewModel.overlayId = position
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentGridEcomBinding.inflate(inflater, container, false)


}