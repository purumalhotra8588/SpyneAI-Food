package com.spyneai.reshoot.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.snackbar.Snackbar
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentEcomOverlayReshootBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.reshoot.ReshootAdapter
import com.spyneai.reshoot.data.ReshootOverlaysRes
import com.spyneai.reshoot.data.SelectedImagesHelper
import com.spyneai.shootapp.data.OnOverlaySelectionListener
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.data.model.ShootData
import com.spyneai.shootapp.ui.dialogs.ReclickDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ConfirmReshootEcomDialog
import com.spyneai.shoot.ui.ecomwithoverlays.ConfirmReshootPortraitDialog
import org.json.JSONArray

class EcomOverlayReshootFragment :
    BaseFragment<ShootViewModelApp, FragmentEcomOverlayReshootBinding>(),
    OnItemClickListener,
    OnOverlaySelectionListener {

    var reshootAdapter: ReshootAdapter? = null
    var snackbar: Snackbar? = null
    val TAG = "EcomOverlayReshootFragment"
    var total = 0
    var position = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            binding.tvPreview.isVisible = false
        },4000)


        getOverlayIds()
        observerOverlayIds()

        viewModel.showOverlay.observe(viewLifecycleOwner) {
            if (it) {
                binding.imgOverlay.visibility = View.VISIBLE
            } else binding.imgOverlay.visibility = View.INVISIBLE
        }


        binding.ivReshootComment.setOnClickListener {

            val bundle = Bundle()
            bundle.putInt("position",position)
            bundle.putInt("total",total)

            val dialog = DialogReshootImageComment()
            dialog.arguments = bundle

            dialog.show(requireActivity().supportFragmentManager, "DialogViewReshootImage")
        }

//        binding.apply {
////            tvSkuName.visibility = View.VISIBLE
//            ivBackCompleted.visibility = View.INVISIBLE
////            tvSkuName.text = viewModel.sku?.skuName
//        }

//        binding.ivBackCompleted.setOnClickListener {
//            requireActivity().onBackPressed()
//        }
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

        viewModel.onImageConfirmed.observe(viewLifecycleOwner) {
            if (viewModel.shootList.value != null) {
                var list = reshootAdapter?.listItems as List<ReshootOverlaysRes.Data>

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

        viewModel.updateSelectItem.observe(viewLifecycleOwner) { it ->
            if (it) {
                val list = reshootAdapter?.listItems as List<ReshootOverlaysRes.Data>

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
        viewModel.enableCameraButton.value = true
    }


    private fun getOverlayIds() {
        Utilities.showProgressDialog(requireContext())
        val ids = JSONArray()

        SelectedImagesHelper.selectedOverlayIds.keys.forEach {
            ids.put(it)
        }
        total = ids.length()

        viewModel.getOverlayIds(ids)
    }

    private fun observerOverlayIds() {
        viewModel.reshootOverlaysRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    val list = it.value.data
                    var index = 0

                    list.forEach {
                        it.imageName = SelectedImagesHelper.selectedOverlayIds[it.id]?.imageName!!
                        it.sequenceNumber =
                            SelectedImagesHelper.selectedOverlayIds[it.id]?.sequenceNumber!!
                    }

                    if (viewModel.shootList.value != null) {
                        list.forEach { overlay ->
                            val element = viewModel.shootList.value!!.firstOrNull {
                                it.overlayId == overlay.id
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

                    //show leveler
                    when (viewModel.categoryDetails.value?.categoryId) {
                        AppConstants.FOOTWEAR_CATEGORY_ID,
                        AppConstants.FOOD_AND_BEV_CATEGORY_ID -> {
//                            viewModel.showLeveler.value = true
                            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
                        }
                    }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getOverlayIds() }
                }
                else -> {}
            }
        }
    }

    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        ConfirmReshootPortraitDialog().show(
            requireFragmentManager(),
            "ConfirmReshootPortraitDialog"
        )
    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when (data) {
            is ReshootOverlaysRes.Data -> {

                if (data.imageClicked) {
                    val bundle = Bundle()
                    bundle.putInt("overlay_id", data.id)
                    bundle.putInt("position", position)
                    bundle.putString(
                        "image_type",
                        viewModel.categoryDetails.value?.imageType
                    )
                    val reclickDialog = ReclickDialog()
                    reclickDialog.arguments = bundle
                    reclickDialog.show(requireActivity().supportFragmentManager, "ReclickDialog")
                } else {
                    viewModel.overlayId = data.id

                    val list = reshootAdapter?.listItems as List<ReshootOverlaysRes.Data>

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
        viewModel.hasEcomOverlay = true

        when (data) {
            is ReshootOverlaysRes.Data -> {
                viewModel.reshotImageName = data.imageName
                viewModel.reshootSequence = data.sequenceNumber

                viewModel.displayName = data.displayName
                viewModel.displayThumbanil = data.displayThumbnail
                viewModel.overlayId = data.id
                viewModel.desiredAngle = data.frameAngle.toInt()

                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

                loadOverlay(data.displayName, data.displayThumbnail)

                binding.tvShoot?.text =
                    position.plus(1).toString() + "/" + viewModel.exterirorAngles.value.toString()

                viewModel.categoryDetails.value?.imageType = data.type

                viewModel.overlayId = data.id

                binding.tvShoot?.text =
                    "Angles ${position.plus(1)}/${SelectedImagesHelper.selectedOverlayIds.size}"
            }
        }
    }

    private fun loadOverlay(name: String, overlay: String) {

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .signature(ObjectKey(overlay))

        Glide.with(requireContext())
            .load(overlay)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    val properties = HashMap<String, Any?>()
                    properties["name"] = name
                    properties["error"] = e?.localizedMessage
                    properties["category"] = viewModel.categoryDetails.value?.categoryName

                    requireContext().captureEvent(
                        Events.OVERLAY_LOAD_FIALED,
                        properties
                    )

                    snackbar = Snackbar.make(
                        binding.root,
                        "Overlay Failed to load",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction("Retry") {
                            loadOverlay(name, overlay)
                        }
                        .setActionTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.primary
                            )
                        )

                    snackbar?.show()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {

                    if (snackbar != null)
                        snackbar?.dismiss()

                    val properties = HashMap<String, Any?>()
                    properties["name"] = name
                    properties["category"] = viewModel.categoryDetails.value?.categoryName

                    requireContext().captureEvent(
                        Events.OVERLAY_LOADED,
                        properties
                    )

                    getPreviewDimensions(binding.imgOverlay!!)
                    return false
                }

            })
            .apply(requestOptions)
            .into(binding.imgOverlay!!)

    }

    private fun getPreviewDimensions(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val shootDimensions = viewModel.shootDimensions.value
                shootDimensions?.overlayWidth = view.width
                shootDimensions?.overlayHeight = view.height

                viewModel.shootDimensions.value = shootDimensions
            }
        })
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEcomOverlayReshootBinding.inflate(inflater, container, false)

}