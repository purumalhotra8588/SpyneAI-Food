package com.spyneai.shootapp.ui.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogShootHintBinding
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shootapp.repository.model.project.Project
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.shootapp.data.ShootViewModelApp

import com.spyneai.shootapp.utils.shoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ShootHintDialog : BaseDialogFragment<ShootViewModelApp, DialogShootHintBinding>() {

     var projectCount =0
    var skuCount =0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalScope.launch(Dispatchers.IO) {
            projectCount=viewModel.getAllProjectsSize()
            skuCount=viewModel.getSkusCountByProjectUuid(viewModel.projectApp?.uuid ?: getUuid())
        }

        dialog?.setCancelable(false)

//        when(viewModel.categoryDetails.value?.categoryName){
//            "Bikes" ->  Glide.with(this).asGif().load(R.raw.bikes_intro).into(binding.ivBeforeShootGif)
//            else ->   Glide.with(this).asGif().load(R.raw.before_shoot).into(binding.ivBeforeShootGif)
//        }

        binding.btContinue.setOnClickListener {
//            viewModel.showVin.value = true
            when {
                viewModel.projectApp == null -> {
                    createProject("prj","sku")
                }

                viewModel.fromVideo -> {
                    if (!viewModel.createProjectDialogShown && viewModel.isProjectCreated.value == false)
                        createProject("prj","sku")
                }

                viewModel.fromDrafts -> {
                    if (viewModel.isSkuCreated.value == null
                        && viewModel.isSubCategoryConfirmed.value == null
                    )
                        createSku("sku")
                }

                else -> {
                    if (!viewModel.createProjectDialogShown && viewModel.projectApp == null)
                        createProject("prj","sku")
                    else if (viewModel.isSkuCreated.value == null)
                        createSku("sku")
                }
            }

            if (viewModel.fromVideo)
                viewModel.getSubCategories.value = true

            dismiss()
        }
    }


    private fun createProject(projectName: String,skuName: String) {
        val project = Project(
            uuid = getUuid(),
            userId = Utilities.getPreference(requireContext(),AppConstants.USER_ID).toString(),
            categoryId = viewModel.categoryDetails.value?.categoryId!!,
            categoryName = viewModel.categoryDetails.value?.categoryName!!,
            projectName = projectName+(projectCount.plus(1))
        )

        viewModel.projectApp = project

        //update shoot session
        Utilities.savePrefrence(requireContext(), AppConstants.SESSION_ID,project.uuid)

        if (viewModel.skuApp == null){
            val sku = Sku(
                uuid = getUuid(),
                projectUuid = project.uuid,
                categoryId = project.categoryId,
                categoryName = project.categoryName,
                skuName = Utilities.getPreference(requireContext(),AppConstants.REGISTRATION_NUMBER),
                initialFrames = if (viewModel.exterirorAngles.value == null) 0 else viewModel.exterirorAngles.value
            )
            viewModel.skuApp = sku
            viewModel.isSkuNameAdded.value=true


            GlobalScope.launch(Dispatchers.IO) {
                viewModel.insertProject()
                viewModel.insertSku()
            }
        }

        viewModel.projectId.value = project.uuid
        //notify project created
        viewModel.isProjectCreated.value = true
        viewModel.getSubCategories.value = true

        dismiss()
    }

    private fun createSku(skuName: String) {
        val sku = Sku(
            uuid = getUuid(),
            skuName = Utilities.getPreference(requireContext(), AppConstants.REGISTRATION_NUMBER),
            projectUuid = viewModel.projectApp?.uuid,
            projectId = viewModel.projectApp?.projectId,
            categoryId = viewModel.categoryDetails.value?.categoryId,
            categoryName = viewModel.categoryDetails.value?.categoryName,
            subcategoryName = viewModel.subcategoryV2?.subCatName,
            subcategoryId = if (viewModel.subcategoryV2?.prodSubCatId == null) "subcategoryId" else viewModel.subcategoryV2?.prodSubCatId,
            initialFrames = if (viewModel.exterirorAngles.value == null) 0 else viewModel.exterirorAngles.value
        )

        viewModel.skuApp = sku
        viewModel.isSkuNameAdded.value=true

        //notify project created
        viewModel.isProjectCreated.value = true
        //viewModel.isSkuCreated.value = true

        viewModel.category?.shootExperience?.let {
            if (it.hasSubcategories)
                viewModel.getSubCategories.value = true
            else{
//                sku.isSelectAble = true
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
            }
        }

        //add sku to local database
        GlobalScope.launch {
            viewModel.insertSku()

            GlobalScope.launch(Dispatchers.IO) {

                viewModel.skuCount=viewModel.getSkusCountByProjectUuid(viewModel.projectApp?.uuid ?: getUuid())
//            Log.d("Shootmodellllll", "skuCount: ${viewModel.skuCount}")
            }

            //start sync service
//            GlobalScope.launch(Dispatchers.Main) {
//                if (sku.isSelectAble){
//                    requireContext().startUploadingService(
//                        ProjectTagDialog::class.java.simpleName,
//                        ServerSyncTypes.CREATE
//                    )
//                }

                dismiss()
            }
        }

    override fun onResume() {
        super.onResume()
        viewModel.gifDialogShown = true
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissAllowingStateLoss()
    }

    override fun onDestroy() {
        super.onDestroy()
        shoot("onDestroy called(shootHintDialog)")
        dismissAllowingStateLoss()
        dismiss()
    }

    override fun onStop() {
        super.onStop()
        shoot("onStop called(shootHintDialog)")
//        dismissAllowingStateLoss()
    }


    override fun getViewModel() = ShootViewModelApp::class.java


    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogShootHintBinding.inflate(inflater, container, false)
}