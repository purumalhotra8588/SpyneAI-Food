package com.spyneai.shootapp.ui.ecomwithoverlays

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
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
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.posthog.captureEvent
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.databinding.FragmentOverlayEcomBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shootapp.adapters.OverlaysAdapter
import com.spyneai.shootapp.data.OnOverlaySelectionListener
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.data.model.ShootData
import com.spyneai.shootapp.ui.dialogs.ReclickDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ConfirmReshootEcomDialog
import com.spyneai.shoot.ui.ecomwithoverlays.ConfirmReshootPortraitDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class OverlayEcomFragment : BaseFragment<ShootViewModelApp, FragmentOverlayEcomBinding>(),
    OnOverlaySelectionListener, OnItemClickListener {


    var overlaysAdapter: OverlaysAdapter? = null
    var snackbar: Snackbar? = null

    //var position = 1
    var pos = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.showOverlay.observe(viewLifecycleOwner) {
            if (it) {
                binding.imgOverlay.visibility = View.VISIBLE
            } else binding.imgOverlay.visibility = View.INVISIBLE
        }

        viewModel.showGrid.observe(viewLifecycleOwner) {
            if (it) {
                binding.groupGridLines.visibility = View.VISIBLE
            } else binding.groupGridLines.visibility = View.INVISIBLE
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

        viewModel.isSkuCreated.observe(viewLifecycleOwner) {
            if(it)
            getOverlays()
        }

        viewModel.onImageConfirmed.observe(viewLifecycleOwner) {
            if (viewModel.hideChangeSubcat.value == null)
                viewModel.hideChangeSubcat.value = true

            if (viewModel.shootList.value != null && overlaysAdapter != null) {
                viewModel.setSelectedItem(overlaysAdapter?.listItems!!)
            }

            try {
                val list =
                    overlaysAdapter?.listItems as List<CatAgnosticResV2.CategoryAgnos.SubCategoryV2.OverlayApp>
                viewModel.allEcomOverlyasClicked = list.all {
                    it.imageClicked
                }

            } catch (e: Exception) {

            }
        }

        viewModel.updateSelectItem.observe(viewLifecycleOwner) {
            if (it) {
                val list =
                    overlaysAdapter?.listItems as List<CatAgnosticResV2.CategoryAgnos.SubCategoryV2.OverlayApp>

                val element = list.firstOrNull {
                    it.isSelected
                }

                val data = list[viewModel.currentShoot]

                if (element != null && data != element) {
                    data.isSelected = true
                    element.isSelected = false
                    overlaysAdapter?.notifyItemChanged(viewModel.currentShoot)
                    overlaysAdapter?.notifyItemChanged(list.indexOf(element))
                    binding.rvSubcategories.scrollToPosition(viewModel.currentShoot)
                }
            }
        }

        viewModel.notifyItemChanged.observe(viewLifecycleOwner) {
            overlaysAdapter?.notifyItemChanged(it)
        }

        viewModel.scrollView.observe(viewLifecycleOwner) {
            binding.rvSubcategories.scrollToPosition(it)
        }
    }

    private fun observeOverlays() {
        viewModel.subcategoryV2?.let {
            if (!it.overlayApps.isNullOrEmpty()) {
                Utilities.hideProgressDialog()

                val overlaysList = ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2.OverlayApp>()
                val thumbNailList = ArrayList<String>()

                Log.d("OverlayEcomFragment", "observeOverlays: "+viewModel.exterirorAngles.value)

                //Log.d("OverlayEcomFragment", "observeOverlays: "+Gson().toJson(it.overlays))
                it.overlayApps?.forEach {
                    if (it.angles == viewModel.exterirorAngles.value) {
                        overlaysList.add(it)
                        thumbNailList.add(it.displayThumbnail)
                    }
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.preloadOverlays(thumbNailList)
                }

                //set exterior angle value
                viewModel.exterirorAngles.value = overlaysList.size
                viewModel.skuApp?.apply {
                    initialFrames = overlaysList.size
                    totalFrames = overlaysList.size
                }

                //update exterior angles in local DB
                GlobalScope.launch(Dispatchers.IO) {
                    viewModel.updateSkuExteriorAngles()

                }

                Log.d("OverlayEcomFragment", "observeOverlays: "+Gson().toJson(overlaysList))

                if (overlaysList.isNotEmpty()){
                    viewModel.displayName = overlaysList[0].displayName
                    viewModel.displayThumbanil = overlaysList[0].displayThumbnail
                }

                requireContext().captureEvent(
                    Events.GET_OVERLAYS,
                    HashMap<String, Any?>()
                        .apply {
                            this["angles"] = overlaysList.size
                        }
                )

                if (viewModel.fromDrafts) {
                    if(requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE, 0)>0){
                        viewModel.isProjectNameEdited.value=true
                    }
                    binding.tvShoot?.text = "${
                        requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE, 0)
                            .plus(1)
                    }/${overlaysList.size}"
                } else {
                    binding.tvShoot?.text = "1/${overlaysList.size}"

                }
                var index = 0

                if (viewModel.shootList.value != null) {
                    overlaysList.forEach { overlay ->
                        val element = viewModel.shootList.value!!.firstOrNull {
                            it.overlayId == overlay.id
                        }

                        if (element != null) {
                            overlay.imageClicked = true
                            overlay.imagePath = element.capturedImage
                        }
                    }

                    val element = overlaysList.firstOrNull {
                        !it.isSelected && !it.imageClicked
                    }

                    if (element != null) {
                        element.isSelected = true
                        viewModel.displayName = element.displayName
                        viewModel.displayThumbanil = element.displayThumbnail

                        index = overlaysList.indexOf(element)
                    }
                } else {
                    //set overlays
                    overlaysList[0].isSelected = true
                    viewModel.displayName = overlaysList[0].displayName
                    viewModel.displayThumbanil = overlaysList[0].displayThumbnail
                }


                overlaysAdapter = OverlaysAdapter(
                    overlaysList,
                    this@OverlayEcomFragment,
                    this@OverlayEcomFragment
                )

                binding.rvSubcategories.apply {
                    visibility = View.VISIBLE
                    layoutManager = LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    adapter = overlaysAdapter
                }

                binding.rvSubcategories.scrollToPosition(index)

                requireContext().captureEvent(
                    Events.GET_OVERLAYS,
                    HashMap<String, Any?>()
                        .apply {
                            this.put("angles", overlaysList.size)
                        }
                )

                showViews()
            } else {

            }
        }
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


    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        ConfirmReshootPortraitDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
    }


    private fun getOverlays() {
        //Utilities.showProgressDialog(requireContext())

        viewModel.exterirorAngles.value = 0
        viewModel.subcategoryV2?.let {
            requireContext().captureEvent(
                Events.GET_OVERLAYS_INTIATED,
                HashMap<String, Any?>()
                    .apply {
                        this.put("angles", viewModel.exterirorAngles.value.toString())
                        this.put("prod_sub_cat_id", it.prodSubCatId!!)
                    }
            )
        }

        observeOverlays()
    }


    private fun showViews() {
        viewModel.category?.shootExperience?.let {

        }

        binding.apply {
            tvSkuName?.visibility = View.VISIBLE
            llProgress?.visibility = View.VISIBLE
            rvSubcategories.visibility = View.VISIBLE
            tvSkuName?.text = viewModel.skuApp?.skuName
        }

        viewModel.subcategoryV2?.cameraSettings?.showGyro?.let {
            if (it) {
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

            }
        }
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOverlayEcomBinding.inflate(inflater, container, false)

    override fun onOverlaySelected(view: View, position: Int, data: Any?) {
        viewModel.currentShoot = position
        viewModel.hasEcomOverlay=true

        when (data) {
            is CatAgnosticResV2.CategoryAgnos.SubCategoryV2.OverlayApp -> {

                if (data.frameAngle != "")
                    viewModel.desiredAngle = data.frameAngle.toInt()

                viewModel.displayName = data.displayName
                viewModel.displayThumbanil = data.displayThumbnail
                viewModel.overlayId = data.id


//                if (Utilities.getPreference(requireContext(), AppConstants.SELECTED_CATEGORY_ID)
//                    != AppConstants.FOOD_AND_BEV_CATEGORY_ID
//                )
                    loadOverlay(data.angleName, data.displayThumbnail)


                binding.tvShoot?.text =
                    position.plus(1).toString() + "/" + viewModel.exterirorAngles.value.toString()

            }

        }
    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        viewModel.currentShoot = position

        when (data) {
            is CatAgnosticResV2.CategoryAgnos.SubCategoryV2.OverlayApp -> {
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

                    val list =
                        overlaysAdapter?.listItems as List<CatAgnosticResV2.CategoryAgnos.SubCategoryV2.OverlayApp>

                    val element = list.firstOrNull {
                        it.isSelected
                    }

                    if (element != null && data != element) {
                        data.isSelected = true
                        element.isSelected = false
                        overlaysAdapter?.notifyItemChanged(position)
                        overlaysAdapter?.notifyItemChanged(list.indexOf(element))
                        binding.rvSubcategories.scrollToPosition(position)
                    }
                }

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



}