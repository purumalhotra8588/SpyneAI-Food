package com.spyneai.shoot.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentCreateProjectBinding
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.shootapp.repository.model.project.Project
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.ui.dialogs.CreateProjectAndSkuDialog
import com.spyneai.shootapp.ui.dialogs.CreateSkuDialog
import com.spyneai.shootapp.ui.dialogs.ShootHintDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateProjectFragment : BaseFragment<ShootViewModelApp, FragmentCreateProjectBinding>() {
    val TAG = CreateProjectFragment::class.java.simpleName

    var skuCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val shootExperience = viewModel.category?.shootExperience

        if (shootExperience != null && shootExperience.showShootInstructions) {
            if (!viewModel.gifDialogShown)
                initShootHint()
        } else {
            if (viewModel.category?.orientation != "portrait")
                initShootHint()
            else
                viewModel.showVin.value = true
        }

        viewModel.showVin.observe(viewLifecycleOwner) {
            when {
                viewModel.projectApp == null -> initProjectDialog()

                viewModel.fromVideo -> {
                    if (!viewModel.createProjectDialogShown && viewModel.isProjectCreated.value == false)
                        initProjectDialog()
                }

                viewModel.fromDrafts -> {
                    if (viewModel.isSkuCreated.value == null
                        && viewModel.isSubCategoryConfirmed.value == null
                    )
                        initSkuDialog()
                }

                else -> {
                    if (!viewModel.createProjectDialogShown && viewModel.projectApp == null)
                        initProjectDialog()
                    else if (viewModel.isSkuCreated.value == null)
                        initSkuDialog()
                }
            }

//            if (shootExperience?.isMultiSku!!){
//                if (viewModel.project == null){
//                    initProjectDialog()
//                }
//                else {
//                    if (viewModel.fromDrafts){
//                        if (viewModel.isSkuCreated.value == null
//                            && viewModel.isSubCategoryConfirmed.value == null)
//                            initSkuDialog()
//                    }else {
//                        if (viewModel.isSkuCreated.value == null)
//                            initSkuDialog()
//
//                    }
//                }
//            }else {
//                if (viewModel.fromVideo){
//                    if (!viewModel.createProjectDialogShown && viewModel.isProjectCreated.value == false)
//                        initProjectDialog()
//                }else {
//                    if (!viewModel.createProjectDialogShown)
//                        initProjectDialog()
//                }
//            }
        }
    }

    private fun initShootHint() {
        requireContext().captureEvent(Events.SHOW_HINT, HashMap<String, Any?>())
        ShootHintDialog().show(requireActivity().supportFragmentManager, "ShootHintDialog")
    }

    private fun initProjectDialog() {
        if (viewModel.category?.orientation == "portrait")
//            ProjectTagDialog().show(requireFragmentManager(), "CreateProjectEcomDialog")
            createProject()
        else
            CreateProjectAndSkuDialog().show(
                requireActivity().supportFragmentManager,
                "CreateProjectAndSkuDialog"
            )
    }

    private fun initSkuDialog() {
        viewModel.category?.let {
            if (it.orientation == "portrait") {
//                CreateSkuEcomDialog().show(requireActivity().supportFragmentManager, "CreateSkuEcomDialog")
                createSku("sku")
            } else
                CreateSkuDialog().show(requireActivity().supportFragmentManager, "CreateSkuDialog")

        }
    }


    private fun createProject() {
        val project = Project(
            uuid = getUuid(),
            userId = Utilities.getPreference(requireContext(), AppConstants.USER_ID).toString(),
            categoryId = viewModel.categoryDetails.value?.categoryId,
            categoryName = viewModel.categoryDetails.value?.categoryName,
            projectName = "prj" + (viewModel.projectCount.plus(1)),
//            dynamicLayout = data.toString(),
            locationData = viewModel.location_data.value.toString()
        )

        viewModel.projectApp = project

        GlobalScope.launch(Dispatchers.IO) {
            val id = viewModel.insertProject()
//            viewModel.skuCount=viewModel.getSkusCountByProjectUuid(project.uuid)
            Log.d(TAG, "createProject: " + id)
        }

        Utilities.savePrefrence(requireContext(), AppConstants.SESSION_ID, project.projectId)
        //notify project created
        viewModel.isProjectCreated.value = true

        val sku = Sku(
            uuid = getUuid(),
            projectUuid = project.uuid,
            categoryId = project.categoryId,
            subcategoryId = "subcategoryId",
            categoryName = project.categoryName,
            skuName = "sku" + skuCount.plus(1),
            initialFrames = 0
        )


        viewModel.category?.shootExperience?.let {
            if (!it.hasSubcategories) {
//                sku.isSelectAble = true
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
            }
        }

        viewModel.skuApp = sku
        viewModel.isSubCategoryConfirmed.value = true
        viewModel.isSkuCreated.value = true
        viewModel.getSubCategories.value = true
        viewModel.isSkuNameAdded.value = true

        //add sku to local database
        GlobalScope.launch {
            viewModel.insertSku()
        }

    }

    private fun createSku(skuName: String) {
        val sku = Sku(
            uuid = getUuid(),
            skuName = skuName + viewModel.skuCount.plus(1),
            projectUuid = viewModel.projectApp?.uuid,
            projectId = viewModel.projectApp?.projectId,
            categoryId = viewModel.categoryDetails.value?.categoryId,
            categoryName = viewModel.categoryDetails.value?.categoryName,
            subcategoryName = viewModel.subcategoryV2?.subCatName,
            subcategoryId = if (viewModel.subcategoryV2?.prodSubCatId == null) "subcategoryId" else viewModel.subcategoryV2?.prodSubCatId,
            initialFrames = if (viewModel.exterirorAngles.value == null) 0 else viewModel.exterirorAngles.value
        )

        viewModel.skuApp = sku
        viewModel.isSkuNameAdded.value = true

        //notify project created
        viewModel.isProjectCreated.value = true
        viewModel.isSkuCreated.value = true

        viewModel.category?.shootExperience?.let {
            if (it.hasSubcategories)
                viewModel.getSubCategories.value = true
            else {
//                sku.isSelectAble = true
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
            }
        }

        //add sku to local database
        lifecycleScope.launch {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.insertSku()
                viewModel.skuCount =
                    viewModel.getSkusCountByProjectUuid(viewModel.projectApp?.uuid ?: getUuid())
            }
        }


    }


    private fun removeWhiteSpace(toString: String) = toString.replace("\\s".toRegex(), "")


    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCreateProjectBinding.inflate(inflater, container, false)
}
