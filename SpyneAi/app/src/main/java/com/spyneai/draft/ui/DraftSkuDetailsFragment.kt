package com.spyneai.draft.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentAppDraftSkuDetailsBinding

import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.adapter.LocalDraftImagesAdapter
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.setLocale
import com.spyneai.shootapp.data.DraftClickedImages
import com.spyneai.shootapp.repository.model.image.Image
import com.spyneai.shootapp.ui.base.ProcessActivity
import com.spyneai.shootapp.ui.base.ProjectDetailsActivity
import com.spyneai.shootapp.ui.base.ShootPortraitActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class DraftSkuDetailsFragment : BaseFragment<DraftViewModel, FragmentAppDraftSkuDetailsBinding>() {

    private var exterior = ArrayList<Image>()
    private var interiorList = ArrayList<Image>()
    private var miscList = ArrayList<Image>()
    private var threeSixtyInteriorList = ArrayList<Image>()

    private var localExterior = ArrayList<Image>()
    private var localInteriorList = ArrayList<Image>()
    private var localMiscList = ArrayList<Image>()
    private var localThreeSixtyInteriorList = ArrayList<Image>()
    private lateinit var intent: Intent

    val TAG = "DraftSkuDetailsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireContext().setLocale()
        refreshText()

        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        intent = requireActivity().intent

        binding.tvProjectName.text = intent.getStringExtra(AppConstants.PROJECT_NAME)

        fetchImages()

        binding.btnContinueShoot.setOnClickListener {
            onResumeClick()
        }

        viewModel.syncImages.observe(viewLifecycleOwner) {
            fetchImages()
        }
    }

    private fun fetchImages() {
        binding.shimmerCompletedSKU.visibility = View.VISIBLE
        binding.shimmerCompletedSKU.startShimmer()

        lifecycleScope.launch {
            viewModel.getImages(
                requireActivity().intent.getStringExtra(AppConstants.SKU_ID),
                requireActivity().intent.getStringExtra(AppConstants.PROJECT_UUIID).toString(),
                requireActivity().intent.getStringExtra(AppConstants.SKU_UUID).toString(),
                requireActivity().intent.getStringExtra(AppConstants.SKU_NAME),
            )
        }

        viewModel.imagesOfSkuRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.fromLocal && requireActivity().intent.getIntExtra(
                            AppConstants.IMAGE_COUNT,
                            0
                        ) > it.value.data.size
                    ) {
                        //sync draft images data dialog
                        binding.shimmerCompletedSKU.stopShimmer()
                        binding.shimmerCompletedSKU.visibility = View.GONE

                        ImageNotSyncedDialog().show(
                            requireActivity().supportFragmentManager,
                            "ImageNotSyncedDialog"
                        )
                        return@observe
                    }

                    setData(it.value.data as ArrayList<Image>)
                }

                is Resource.Failure -> {
                    handleApiError(it) { fetchImages() }
                    if (it.errorCode == 404) {
                        setData(ArrayList())
                    } else {

                    }
                }
                else -> {

                }
            }
        }
    }

    private fun setData(list: ArrayList<Image>) {
        binding.shimmerCompletedSKU.stopShimmer()
        binding.shimmerCompletedSKU.visibility = View.GONE
        binding.nsv.visibility = View.VISIBLE

        binding.tvTotalSku.text = list.size.toString()

        if (list != null) {
            if (intent.getStringExtra(AppConstants.CATEGORY_ID) == AppConstants.CARS_CATEGORY_ID
                || intent.getStringExtra(AppConstants.CATEGORY_ID) == AppConstants.BIKES_CATEGORY_ID
            ) {
                localExterior = list?.filter {
                    it.image_category == "Exterior"
                } as ArrayList
            } else {
                localExterior = list as ArrayList<Image>
            }

            if (localExterior.size > 0) {
                if (intent.getStringExtra(AppConstants.CATEGORY_ID) == AppConstants.CARS_CATEGORY_ID
                    || intent.getStringExtra(AppConstants.CATEGORY_ID) == AppConstants.BIKES_CATEGORY_ID
                )
                    binding.tvExterior.visibility = View.VISIBLE

                binding.rvExteriorImage.visibility = View.VISIBLE

                binding.rvExteriorImage.apply {
                    adapter = LocalDraftImagesAdapter(
                        requireContext(),
                        localExterior,
                        intent.getStringExtra(AppConstants.CATEGORY_ID)!!
                    )
                }
            }

            localInteriorList = list?.filter {
                it.image_category == "Interior"
            } as ArrayList

            if (localInteriorList.size > 0) {
                binding.tvInterior.visibility = View.VISIBLE
                binding.rvInteriors.visibility = View.VISIBLE
                binding.rvInteriors.apply {
                    adapter = LocalDraftImagesAdapter(
                        requireContext(),
                        localInteriorList,
                        intent.getStringExtra(AppConstants.CATEGORY_ID)!!
                    )
                }
            }

            localMiscList = list?.filter {
                it.image_category == "Focus Shoot"
            } as ArrayList

            if (localMiscList.size > 0) {
                binding.tvFocused.visibility = View.VISIBLE
                binding.rvFocused.visibility = View.VISIBLE
                binding.rvFocused.apply {
                    adapter = LocalDraftImagesAdapter(
                        requireContext(),
                        localMiscList,
                        intent.getStringExtra(AppConstants.CATEGORY_ID)!!
                    )
                }
            }

            if (getString(R.string.app_name) == AppConstants.OLA_CABS) {
                localThreeSixtyInteriorList = list?.filter {
                    it.image_category == "360int"
                } as ArrayList
            }
        }

        binding.flContinueShoot.visibility = View.VISIBLE
    }

    private fun onResumeClick() {
        GlobalScope.launch(Dispatchers.IO) {
            var status = viewModel.getSkuStatus(requireActivity().intent.getStringExtra(AppConstants.SKU_UUID).toString())
            GlobalScope.launch(Dispatchers.Main) {
                if (!status.equals("draft")){
                    val intent =
                        Intent(requireContext(), ProjectDetailsActivity::class.java)
                    intent.apply {
                        putExtra(AppConstants.CATEGORY_ID, requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID))
                        putExtra(AppConstants.PROJECT_UUIID, requireActivity().intent.getStringExtra(AppConstants.PROJECT_UUIID))
                        putExtra(AppConstants.SKU_UUID, requireActivity().intent.getStringExtra(AppConstants.SKU_UUID))
                        putExtra(AppConstants.FROM_DRAFTS, true)
                        startActivity(intent)
                    }
                }else{
                    var shootIntent: Intent = Intent(
                        context,
                        ShootPortraitActivity::class.java
                    )

                    if (intent.getBooleanExtra(AppConstants.FROM_VIDEO, false)) {
                        shootIntent?.apply {
                            putExtra(AppConstants.FROM_VIDEO, true)
                            putExtra(AppConstants.TOTAL_FRAME, intent.getIntExtra(AppConstants.TOTAL_FRAME, 0))
                        }
                    }

                    shootIntent?.apply {
                        putExtra(AppConstants.FROM_DRAFTS, true)
                        putExtra(AppConstants.CATEGORY_NAME, intent.getStringExtra(AppConstants.CATEGORY_NAME))
                        putExtra(AppConstants.CATEGORY_ID, intent.getStringExtra(AppConstants.CATEGORY_ID))
                        putExtra(AppConstants.SUB_CAT_NAME, intent.getStringExtra(AppConstants.SUB_CAT_NAME))
                        putExtra(AppConstants.SUB_CAT_ID, intent.getStringExtra(AppConstants.SUB_CAT_ID))
                        putExtra(AppConstants.PROJECT_ID, intent.getStringExtra(AppConstants.PROJECT_ID))
                        putExtra(AppConstants.PROJECT_UUIID, intent.getStringExtra(AppConstants.PROJECT_UUIID))
                        putExtra(AppConstants.SKU_NAME, intent.getStringExtra(AppConstants.SKU_NAME))
                        putExtra(AppConstants.SKU_COUNT, intent.getIntExtra(AppConstants.SKU_COUNT, 0))
                        putExtra(AppConstants.SKU_CREATED, true)
                        putExtra(AppConstants.SKU_ID, intent.getStringExtra(AppConstants.SKU_ID))
                        putExtra(AppConstants.SKU_UUID, intent.getStringExtra(AppConstants.SKU_UUID))
                        putExtra(
                            AppConstants.EXTERIOR_ANGLES,
                            intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0)
                        )
                        putExtra(AppConstants.RESUME_EXTERIOR, resumeExterior())
                        putExtra(AppConstants.RESUME_INTERIOR, resumeInterior())
                        putExtra(AppConstants.RESUME_MISC, resumeMisc())
                        putExtra("is_paid", false)
                        putExtra(AppConstants.IMAGE_TYPE, intent.getStringExtra(AppConstants.IMAGE_TYPE))
                        putExtra(AppConstants.IS_360, intent.getBooleanExtra(AppConstants.IS_360, false))
                    }

                    if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)) {
                        shootIntent?.apply {
                            putExtra(AppConstants.FROM_LOCAL_DB, true)
                            putExtra(AppConstants.EXTERIOR_SIZE, localExterior.size)
                            putExtra(AppConstants.INTERIOR_SIZE, localInteriorList.size)
                            putExtra(AppConstants.MISC_SIZE, localMiscList.size)
                        }
                    } else {

                        val extPathList = exterior?.map {
                            it.input_image_hres_url
                        } as ArrayList<String>

                        val imageNameList = exterior?.map {
                            it.name
                        } as ArrayList<String>

                        shootIntent?.apply {
                            putExtra(AppConstants.FROM_LOCAL_DB, false)
                            putExtra(AppConstants.EXTERIOR_SIZE, exterior.size)
                            putStringArrayListExtra(AppConstants.EXTERIOR_LIST, extPathList)
                            putStringArrayListExtra(AppConstants.SHOOT_IMAGE_NAME_LIST, imageNameList)
                            putExtra(AppConstants.INTERIOR_SIZE, interiorList.size)
                            putExtra(AppConstants.MISC_SIZE, miscList.size)
                        }
                    }

                    setDraftImage()

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.CATEGORY_NAME,
                        intent.getStringExtra(AppConstants.CATEGORY_NAME)
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.CATEGORY_ID,
                        intent.getStringExtra(AppConstants.CATEGORY_ID)
                    )


                    if (getString(R.string.app_name) == AppConstants.OLA_CABS) {
                        if (threeSixtyIntSelected()) {
                            startProcessActivty(
                                shootIntent!!
                            )
                        } else {
                            if (resumeMisc()) {
                                checkMiscSize(shootIntent!!)
                                observeMisc(shootIntent)
                            } else {
                                startActivity(shootIntent)
                            }
                        }
                    } else {
                        if (resumeMisc()) {
                            checkMiscSize(shootIntent!!)
                            observeMisc(shootIntent)
                        } else {
                            startActivity(shootIntent)
                        }
                    }}
            }
        }
    }

    private fun setDraftImage() {
        val fromLocalDb =
            requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)
        val map = HashMap<String, String>()

        when {
            resumeExterior() -> {
                if (fromLocalDb) {
                    localExterior.forEach {
                        map.put(it.overlayId.toString(), it.path!!)
                    }
                } else {
                    exterior.forEach {
                        map.put(it.overlayId.toString(), it.input_image_lres_url)
                    }
                }
            }

            resumeInterior() -> {
                if (fromLocalDb) {
                    localInteriorList.forEach {
                        map.put(it.overlayId.toString(), it.path!!)
                    }
                } else {
                    interiorList.forEach {
                        map.put(it.overlayId.toString(), it.input_image_lres_url)
                    }
                }
            }

            resumeMisc() -> {
                if (fromLocalDb) {
                    localMiscList.forEach {
                        map.put(it.overlayId.toString(), it.path!!)
                    }
                } else {
                    miscList.forEach {
                        map.put(it.overlayId.toString(), it.input_image_lres_url)
                    }
                }
            }
        }

        DraftClickedImages.clickedImagesMap = map

    }

    private fun checkMiscSize(intent: Intent) {
        Utilities.showProgressDialog(requireContext())
        viewModel.getSubCategories(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )
    }

    private fun observeMisc(intent: Intent) {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    if (requireActivity().intent.getBooleanExtra(
                            AppConstants.FROM_LOCAL_DB,
                            false
                        )
                    ) {
                        if (requireActivity().intent.getStringExtra(AppConstants.CATEGORY_NAME) == "Bikes") {
                            val filteredList: List<NewSubCatResponse.Miscellaneous> =
                                it.value.miscellaneous.filter {
                                    it.prod_sub_cat_id == requireActivity().intent.getStringExtra(
                                        AppConstants.SUB_CAT_ID
                                    )
                                }

                            it.value.miscellaneous = filteredList
                        }

                        if (it.value.miscellaneous.size == localMiscList.size) {
                            //start procss activity
                            startProcessActivty(intent)
                        } else {
                            startActivity(intent)
                        }
                    } else {
                        if (it.value.miscellaneous.size == miscList.size) {
                            startProcessActivty(intent)
                        } else {
                            startActivity(intent)
                        }
                    }

                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { checkMiscSize(intent) }
                }

                else -> {

                }
            }
        }
    }

    private fun startProcessActivty(intent: Intent) {
        val processIntent = Intent(requireContext(), ProcessActivity::class.java)

        Log.d("DraftSKUDetail", "ProcessStart"+"DraftSKUDetail")

        processIntent.apply {
            putExtra(AppConstants.CATEGORY_NAME, intent.getStringExtra(AppConstants.CATEGORY_NAME))
            putExtra(AppConstants.CATEGORY_ID, intent.getStringExtra(AppConstants.CATEGORY_ID))
            putExtra(AppConstants.SKU_UUID, intent.getStringExtra(AppConstants.SKU_UUID))
            putExtra("sku_id", intent.getStringExtra(AppConstants.SKU_ID))
            putExtra(AppConstants.PROJECT_UUIID, intent.getStringExtra(AppConstants.PROJECT_UUIID))
            putExtra("project_id", intent.getStringExtra(AppConstants.PROJECT_ID))
            putExtra("exterior_angles", intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0))
        }

        when (intent.getStringExtra(AppConstants.CATEGORY_NAME)) {
            "Automobiles" -> processIntent.putExtra("process_sku", true)
            else -> processIntent.putExtra("process_sku", false)
        }

        startActivity(processIntent)
    }


    private fun resumeMisc(): Boolean {
        return if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)) {
            localMiscList.size > 0
        } else {
            miscList.size > 0
        }
    }

    private fun threeSixtyIntSelected(): Boolean {
        return if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)) {
            localThreeSixtyInteriorList.size > 0
        } else {
            threeSixtyInteriorList.size > 0
        }
    }


    private fun resumeInterior() = !resumeExterior() && !resumeMisc()

    private fun resumeExterior(): Boolean {
        return if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)) {
            (localExterior.size != requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0) &&
                    localExterior.size <= requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0))
                    && (localInteriorList.isEmpty() && localMiscList.isEmpty())

        } else {
            exterior.size != requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0)
                    && (interiorList.isEmpty() && miscList.isEmpty())
        }
    }


    fun refreshText() {
        binding.tvExterior.text = getString(R.string.exterior)
        binding.tvInterior.text = getString(R.string.interior)
        binding.tvFocused.text = getString(R.string.focused)
        binding.btnContinueShoot.text = getString(R.string.resume_shoot)
    }


    override fun getViewModel() = DraftViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAppDraftSkuDetailsBinding.inflate(inflater, container, false)

}