package com.spyneai.reshoot.ui

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.InfoDialog
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.FragmentEcomGridReshootBinding
import com.spyneai.reshoot.ReshootAdapter
import com.spyneai.reshoot.data.SelectedImagesHelper

import com.spyneai.service.ServerSyncTypes
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ConfirmReshootEcomDialog
import com.spyneai.shootapp.data.OnOverlaySelectionListener
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.data.model.ShootData
import com.spyneai.shootapp.repository.model.image.Image
import com.spyneai.shootapp.ui.dialogs.ConfirmTagsDialog
import com.spyneai.shootapp.ui.dialogs.ReclickDialog
import com.spyneai.startUploadingService
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class EcomGridReshootFragment : BaseFragment<ShootViewModelApp, FragmentEcomGridReshootBinding>(),
    OnItemClickListener,
    OnOverlaySelectionListener {

    var reshootAdapter: ReshootAdapter? = null

    val TAG = "ReshootFragment"
    var total = 0
    var position = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            binding.tvPreview.isVisible = false
        },4000)

        setReshootData()

//        binding.apply {
//            tvSkuName.visibility = View.GONE
//            ivBackCompleted.visibility = View.GONE
//            tvSkuName.text = viewModel.sku?.skuName
//        }
//
//        binding.ivBackCompleted.setOnClickListener {
//            requireActivity().onBackPressed()
//        }

        binding.ivReshootComment.setOnClickListener {

            val bundle = Bundle()
            bundle.putInt("position",position)
            bundle.putInt("total",total)

            val dialog = DialogReshootImageComment()
            dialog.arguments = bundle

            dialog.show(requireActivity().supportFragmentManager, "DialogViewReshootImage")
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

        //observe new image clicked
        viewModel.onImageConfirmed.observe(viewLifecycleOwner) {
            if (viewModel.shootList.value != null) {
                var list = reshootAdapter?.listItems as List<Image>

                val position = viewModel.currentShoot

                list[position].isSelected = false
                list[position].imageClicked = true
                list[position].imagePath = viewModel.getCurrentShoot()!!.capturedImage
                reshootAdapter?.notifyItemChanged(position)

                if (position != list.size.minus(1)) {
                    var foundNext = false

                    for (i in position..list.size.minus(1)) {
                        if (!list[i].isSelected && !list[i].imageClicked) {
                            foundNext = true
                            list[i].isSelected = true
                            reshootAdapter?.notifyItemChanged(i)
                            binding.rvImages.scrollToPosition(i.plus(2))
                            break
                        }
                    }

                    if (!foundNext) {
                        val element = list.firstOrNull {
                            !it.isSelected && !it.imageClicked
                        }

                        if (element != null) {
                            element?.isSelected = true
                            reshootAdapter?.notifyItemChanged(list.indexOf(element))
                            binding.rvImages.scrollToPosition(viewModel.currentShoot)
                        }
                    }
                } else {
                    val element = list.firstOrNull {
                        !it.isSelected && !it.imageClicked
                    }

                    if (element != null) {
                        element?.isSelected = true
                        reshootAdapter?.notifyItemChanged(list.indexOf(element))
                        binding.rvImages.scrollToPosition(viewModel.currentShoot)
                    }
                }

                viewModel.allReshootClicked = list.all { it.imageClicked }
            }
        }

//        viewModel.onImageConfirmed.observe(viewLifecycleOwner) {
//            if (viewModel.shootList.value != null) {
//                var list = reshootAdapter?.listItems as List<Image>
//
//                val position = viewModel.currentShoot
//
//                list[position].isSelected = false
//                list[position].imageClicked = true
//                list[position].imagePath = viewModel.getCurrentShoot()!!.capturedImage
//                reshootAdapter?.notifyItemChanged(position)
//
//                if (position != list.size.minus(1)) {
//                    var foundNext = false
//
//                    for (i in position..list.size.minus(1)) {
//                        if (!list[i].isSelected && !list[i].imageClicked) {
//                            foundNext = true
//                            list[i].isSelected = true
//                            reshootAdapter?.notifyItemChanged(i)
//                            binding.rvImages.scrollToPosition(i.plus(2))
//                            break
//                        }
//                    }
//
//                    if (!foundNext) {
//                        val element = list.firstOrNull {
//                            !it.isSelected && !it.imageClicked
//                        }
//
//                        if (element != null) {
//                            element?.isSelected = true
//                            reshootAdapter?.notifyItemChanged(list.indexOf(element))
//                            binding.rvImages.scrollToPosition(viewModel.currentShoot)
//                        }
//                    }
//                } else {
//                    val element = list.firstOrNull {
//                        !it.isSelected && !it.imageClicked
//                    }
//
//                    if (element != null) {
//                        element?.isSelected = true
//                        reshootAdapter?.notifyItemChanged(list.indexOf(element))
//                        binding.rvImages.scrollToPosition(viewModel.currentShoot)
//                    }
//                }
//
//                val s = ""
//                viewModel.allReshootClicked = list.all { it.imageClicked }
//            }
//        }

        viewModel.updateSelectItem.observe(viewLifecycleOwner) { it ->
            if (it) {
                val list = reshootAdapter?.listItems as List<Image>

                val element = list.firstOrNull {
                    it.isSelected
                }
                val data = list[viewModel.currentShoot]

                if (element != null && data != element) {
                    data.isSelected = true
                    element.isSelected = false
                    reshootAdapter?.notifyItemChanged(viewModel.currentShoot)
                    reshootAdapter?.notifyItemChanged(list.indexOf(element))
                    binding.rvImages.scrollToPosition(viewModel.currentShoot)
                }
            }
        }

        viewModel.notifyItemChanged.observe(viewLifecycleOwner) {
            reshootAdapter?.notifyItemChanged(it)
        }

        viewModel.scrollView.observe(viewLifecycleOwner) {
            binding.rvImages.scrollToPosition(it)
        }

        viewModel.isCameraButtonClickable = true



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
            InfoDialog().show(
                requireActivity().supportFragmentManager,
                "InfoDialog"
            )
        }
    }

    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        when (viewModel.categoryDetails.value?.imageType) {
            "Info" -> {

//                    binding.apply {
//                        ivNext.visibility = View.GONE
//                        ivEnd.visibility = View.VISIBLE
//                    }

                CropImage.activity(Uri.fromFile(File(shootData.capturedImage)))
                    .start(requireActivity())
            }
            else ->
                ConfirmReshootEcomDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
        }
    }

    private fun setReshootData() {
        val list = SelectedImagesHelper.selectedImages
        var index = 0

        if (viewModel.shootList.value != null) {
            list.forEach { overlay ->
                val element = viewModel.shootList.value!!.firstOrNull {
                    it.overlayId == overlay.overlayId.toInt()
                }

                if (element != null) {
                    overlay.imageClicked = true
                    overlay.imagePath = element.capturedImage
                }
            }

            val element = list.first {
                !it.isSelected && !it.imageClicked
            }

            element.isSelected = true
            index = list.indexOf(element)

        } else {
            //set overlays
            list[index].isSelected = true
//            if(list[index].image_category == "Info"){
//                viewModel.imageTypeInfo.value=true
//            }
        }

        //set recycler view
        reshootAdapter = ReshootAdapter(
            list,
            this,
            this
        )

        binding.rvImages.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = reshootAdapter
            scrollToPosition(index)
        }

//        viewModel.showLeveler.value = true
        viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
        viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
        viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEcomGridReshootBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
            when(data){
                is Image -> {
                    if (data.imageClicked){
                        val bundle = Bundle()
                        bundle.putInt("overlay_id",data.overlayId.toInt())
                        bundle.putInt("position",position)
                        bundle.putString("image_type",data.image_category)
                        val reclickDialog = ReclickDialog()
                        reclickDialog.arguments = bundle
                        reclickDialog.show(requireActivity().supportFragmentManager,"ReclickDialog")
                    }else {
                        val list = reshootAdapter?.listItems as List<Image>

                        val element = list.firstOrNull {
                            it.isSelected
                        }

                        if (element != null && data != element) {
                            data.isSelected = true
                            element.isSelected = false
                            reshootAdapter?.notifyItemChanged(position)
                            reshootAdapter?.notifyItemChanged(list.indexOf(element))
                            binding.rvImages.scrollToPosition(position)
                        }
                    }

                }
            }
    }

    override fun onOverlaySelected(view: View, position: Int, data: Any?) {
        viewModel.currentShoot = position

       when(data){
           is Image -> {
               viewModel.reshotImageName = data.name
               viewModel.reshootSequence = data.sequence
               viewModel.categoryDetails.value?.imageType = data.image_category
               viewModel.overlayId = data.overlayId.toInt()


               if(viewModel.categoryDetails.value?.imageType == "Info"){
                   viewModel.imageTypeInfo.value=true
                   viewModel.showLeveler.value = false
                   viewModel.showOverlay.value = false

               }
           }
       }
    }
}