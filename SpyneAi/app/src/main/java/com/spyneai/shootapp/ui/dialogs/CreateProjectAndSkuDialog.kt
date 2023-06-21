package com.spyneai.shootapp.ui.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.spyneai.*
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogCreateProjectAndSkuBinding
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.utils.shoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreateProjectAndSkuDialog : BaseDialogFragment<ShootViewModelApp, DialogCreateProjectAndSkuBinding>() {

    val TAG = CreateProjectAndSkuDialog::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.etProjectName.setText(viewModel.projectApp?.projectName)
        binding.etSkuName.setText(viewModel.skuApp?.skuName)

        binding.ivClose.setOnClickListener {
            dismiss()
        }




        binding.btnProceed.setOnClickListener {
            when {
                binding.etProjectName.text.toString().isEmpty() -> {
                    binding.etProjectName.error =
                        "Please enter project name"
                }
                binding.etProjectName.text.toString()
                    .contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex()) -> {
                    binding.etProjectName.error = "Special characters not allowed"
                }
                binding.etSkuName.text.toString().isEmpty() -> {
                    binding.etSkuName.error = "Please enter product name"
                }
                binding.etSkuName.text.toString()
                    .contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex()) -> {
                    binding.etSkuName.error = "Special characters not allowed"
                }
                else -> {
                    GlobalScope.launch(Dispatchers.IO) {
                        viewModel.updateSkuName(
                            viewModel.skuApp!!.uuid,
                            removeWhiteSpace(binding.etSkuName.text.toString())
                        )
                        viewModel.updateProjectName(
                            viewModel.projectApp!!.uuid,
                            removeWhiteSpace(binding.etProjectName.text.toString())
                        )

                    GlobalScope.launch(Dispatchers.Main){
                        viewModel.projectApp!!.projectName=removeWhiteSpace(binding.etProjectName.text.toString())
                        viewModel.skuApp!!.skuName=removeWhiteSpace(binding.etSkuName.text.toString())
                        Toast.makeText(requireContext(),"Project and Sku Name Updated",Toast.LENGTH_SHORT).show()
                        viewModel.isSkuNameAdded.value=true
                    }
                        dismiss()
                    }

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.createProjectDialogShown = true
    }


//    private fun createProject(projectName: String,skuName: String) {
//
//        val project = com.spyneai.shoot.repository.model.project.Project(
//            uuid = getUuid(),
//            categoryId = viewModel.categoryDetails.value?.categoryId!!,
//            categoryName = viewModel.categoryDetails.value?.categoryName!!,
//            projectName = projectName
//        )
//
//        viewModel.project = project
//
//        //update shoot session
//        Utilities.savePrefrence(requireContext(),AppConstants.SESSION_ID,project.uuid)
//
//        if (viewModel.sku == null){
//            val sku = Sku(
//                uuid = getUuid(),
//                projectUuid = project.uuid,
//                categoryId = project.categoryId,
//                categoryName = project.categoryName,
//                skuName = skuName
//            )
//            viewModel.sku = sku
//
//            GlobalScope.launch(Dispatchers.IO) {
//                viewModel.insertProject()
//                viewModel.insertSku()
//            }
//        }
//
//        viewModel.projectId.value = project.uuid
//        //notify project created
//        viewModel.isProjectCreated.value = true
//        viewModel.getSubCategories.value = true
//
//        dismiss()
//    }


    override fun onDestroy() {
        super.onDestroy()
        shoot("onDestroy called(shootHintDialog)")
        dismissAllowingStateLoss()
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissAllowingStateLoss()
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogCreateProjectAndSkuBinding.inflate(inflater, container, false)
}




