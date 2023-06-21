package com.spyneai.shootapp.ui.ecomwithgrid.dialogs

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.forEachIndexed
import androidx.viewbinding.ViewBinding
import com.google.android.material.chip.Chip
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.dashboard.repository.model.LayoutHolder
import com.spyneai.databinding.ItemProjectChipedittextBinding
import com.spyneai.databinding.ItemProjectEdittextBinding
import com.spyneai.databinding.ProjectTagDialogBinding
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shootapp.repository.model.project.Project
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.service.ServerSyncTypes
import com.spyneai.shootapp.data.ShootViewModelApp

import com.spyneai.startUploadingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class ProjectTagDialog : BaseDialogFragment<ShootViewModelApp, ProjectTagDialogBinding>() {

    private val bindingList = ArrayList<ViewBinding>()
    private lateinit var inflator: LayoutInflater
    private val data = JSONObject()
    private var shortAnimationDuration: Int = 0
    private val TAG = ProjectTagDialog::class.java.simpleName

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window
                ?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(Utilities.getPreference(requireContext(), AppConstants.SELECTED_CATEGORY_ID)){
            AppConstants.ECOM_CATEGORY_ID, AppConstants.FOOTWEAR_CATEGORY_ID-> {
                binding.tvEnterProjectName.text = "Enter Project name"
                binding.tvEnterSkuName.text = "Enter Sku name"
            }
        }

        when(Utilities.getPreference(requireContext(), AppConstants.SELECTED_CATEGORY_ID)){
            AppConstants.ECOM_CATEGORY_ID-> {
                binding.ivBarCode.visibility=View.VISIBLE
            }
            else ->{
                binding.ivBarCode.visibility=View.GONE
            }
        }


        binding.etProjectName.setText(viewModel.projectApp?.projectName)
        binding.etSkuName.setText(viewModel.skuApp?.skuName)

//        inflator = LayoutInflater.from(requireContext())

        isCancelable = false

        binding.ivClose.setOnClickListener {
            viewModel.isProjectNameEdited.value = false
            dismiss()
        }

        binding.llContainer.visibility = View.GONE
        binding.btnProceed.visibility = View.GONE

//        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

//        binding.etProjectName.setText(viewModel.dafault_project.value)
//        binding.etSkuName.setText(viewModel.dafault_sku.value)


//        setTagsData()

        binding.ivBarCode.setOnClickListener {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES)
            options.setPrompt("Scan a barcode")
            options.setCameraId(0) // Use a specific camera of the device
            options.setBeepEnabled(true)
            options.setOrientationLocked(false)
            barcodeLauncher.launch(options)
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
                            com.spyneai.removeWhiteSpace(binding.etSkuName.text.toString())
                        )
                        viewModel.updateProjectName(
                            viewModel.projectApp!!.uuid,
                            com.spyneai.removeWhiteSpace(binding.etProjectName.text.toString())
                        )

                        GlobalScope.launch(Dispatchers.Main){
                            viewModel.projectApp!!.projectName=
                                com.spyneai.removeWhiteSpace(binding.etProjectName.text.toString())
                            viewModel.skuApp!!.skuName=
                                com.spyneai.removeWhiteSpace(binding.etSkuName.text.toString())
                            Toast.makeText(requireContext(),"Project and Sku Name Updated",Toast.LENGTH_SHORT).show()
                            viewModel.isSkuNameAdded.value=true
//                            viewModel.isSubCategoryConfirmed.value = true
                            dialog?.dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun setTagsData() {
        val data = LayoutHolder.data
        try {
            if (data!![LayoutHolder.categoryPosition].dynamic_layout?.project_dialog.isNullOrEmpty()) {
                return
            }
        } catch (e: Exception) {
            return
        }

        val layout = data!![LayoutHolder.categoryPosition].dynamic_layout?.project_dialog

        layout?.forEach {
            when (it.field_name) {
                "Restaurant ID", "Restaurant Name" -> {
                    val layout = inflator.inflate(R.layout.item_project_edittext, null)
                    val itemBinding = ItemProjectEdittextBinding.bind(layout)
//                    itemBinding.et.hint = it.hint
                    itemBinding.llProjectName.hint = it.hint
                    val dip = 10f
                    val r: Resources = resources
                    val px = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dip,
                        r.displayMetrics
                    )
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.topMargin = px.toInt()



                    layout.layoutParams = params

                    binding.llContainer.addView(layout)
                    bindingList.add(itemBinding)
                }
                "Child Restaurant ID" -> {
                    val layout = inflator.inflate(R.layout.item_project_chipedittext, null)
                    val itemBinding = ItemProjectChipedittextBinding.bind(layout)
//                    itemBinding.et.hint = it.hint
                    itemBinding.llProjectName.hint = it.hint

                    itemBinding.et.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            if (s != null && s.isEmpty()) {
                                return
                            }
                            if (s?.last() == ',' || s?.last() == ' ') {
                                val childRID = s.toString().substringBefore(",")
                                addChip(itemBinding, childRID)
                                itemBinding.et.text!!.clear()

//                            mainTagAutoCompleteTextView.text = null
                                // mainTagAutoCompleteTextView.removeTextChangedListener(this)
                            }
                        }
                    })

                    val dip = 10f
                    val r: Resources = resources
                    val px = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dip,
                        r.displayMetrics
                    )
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.topMargin = px.toInt()



                    layout.layoutParams = params

                    binding.llContainer.addView(layout)
                    bindingList.add(itemBinding)
                }
            }
        }
    }

    private fun addChip(binding: ItemProjectChipedittextBinding, text: String) {
        binding.scrollView.visibility = View.VISIBLE
        val chip = Chip(requireContext())
        chip.text = text
        chip.isChipIconVisible = false
        chip.isCloseIconVisible = true
        chip.isClickable = true
        if (chip.text.length > 1)
            binding.chipGroup.addView(chip as View)
        chip.setOnCloseIconClickListener {
            binding.chipGroup.removeView(chip as View)
            if (binding.chipGroup.childCount == 0)
                binding.scrollView.visibility = View.GONE
        }
    }

    private fun isValid(): Boolean {
        var isValid = true

        try {
            if (LayoutHolder.data!![LayoutHolder.categoryPosition].dynamic_layout?.project_dialog.isNullOrEmpty())
                return isValid
        } catch (e: Exception) {
            return isValid
        }


        val layout = LayoutHolder.data!![0].dynamic_layout?.project_dialog

        bindingList.forEachIndexed { index, it ->
           layout?.let { layout ->
               when (it) {
                   is ItemProjectEdittextBinding -> {
                       if (layout[index].is_required) {
                           if (it.et.text.toString().isEmpty()) {
                               requiredError(it.et, layout[index].field_name)
                               return !isValid
                           } else {
                               if (!layout[index].all_caps) {
                                   var text = it.et.text.toString()
                                   text = text.lowercase()
                                   data.put(layout[index].field_id, text)
                               } else {
                                   data.put(layout[index].field_id, it.et.text.toString())
                               }

                           }
                       } else {
                           data.put(layout[index].field_id, it.et.text.toString())
                       }
                   }
                   is ItemProjectChipedittextBinding -> {
                       if (layout[index].is_required) {
                           if (it.et.text.toString().isEmpty()) {
                               requiredError(it.et, layout[index].field_name)
                               return !isValid
                           } else if (it.chipGroup.childCount == 0) {
                               if (!layout[index].all_caps) {
                                   var text = it.et.text.toString()
                                   text = text.lowercase()
                                   data.put(layout[index].field_id, text)
                               } else {
                                   data.put(layout[index].field_id, it.et.text.toString())
                               }
                           } else {

                               var array = JSONArray()

                               it.chipGroup.forEachIndexed { index, view ->
                                   val chip = it.chipGroup.getChildAt(index) as Chip
                                   array.put(chip.text)
                               }
                               data.put(layout[index].field_id, array)
                           }
                       } else if (it.chipGroup.childCount == 0) {
                           if (!layout[index].all_caps) {
                               var text = it.et.text.toString()
                               text = text.lowercase()
                               data.put(layout[index].field_id, text)
                           } else {
                               data.put(layout[index].field_id, it.et.text.toString())
                           }
                       } else {
                           var array = JSONArray()

                           it.chipGroup.forEachIndexed { index, view ->
                               val chip = it.chipGroup.getChildAt(index) as Chip
                               array.put(chip.text)
                           }
                           data.put(layout[index].field_id, array)
                       }
                   }
                   else -> {

                   }
               }
           }
        }

        return isValid
    }

    private fun createProject() {
        val project = Project(
            uuid = getUuid(),
            userId = Utilities.getPreference(requireContext(),AppConstants.USER_ID).toString(),
            categoryId = viewModel.categoryDetails.value?.categoryId!!,
            categoryName = viewModel.categoryDetails.value?.categoryName!!,
            projectName = removeWhiteSpace(binding.etProjectName.text.toString()),
            dynamicLayout = data.toString(),
            locationData = viewModel.location_data.value.toString()
        )

        viewModel.projectApp = project

        GlobalScope.launch(Dispatchers.IO) {
            val id = viewModel.insertProject()
            Log.d(TAG, "createProject: "+id)
        }

        Utilities.savePrefrence(requireContext(),AppConstants.SESSION_ID,project.projectId)
        //notify project created
        viewModel.isProjectCreated.value = true

        val sku = Sku(
            uuid = getUuid(),
            projectUuid = project.uuid,
            categoryId = project.categoryId,
            subcategoryId = "subcategoryId",
            categoryName = project.categoryName,
            skuName = removeWhiteSpace(binding.etSkuName.text.toString()),
            initialFrames = 0
        )


        viewModel.category?.shootExperience?.let {
            if (!it.hasSubcategories){
                sku.isSelectAble = true
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
            }
        }

        viewModel.skuApp = sku
        viewModel.isSubCategoryConfirmed.value = true
        viewModel.isSkuCreated.value = true
        viewModel.getSubCategories.value = true

        //add sku to local database
        GlobalScope.launch {
            val id = viewModel.insertSku()
            Log.d(TAG, "createProject: "+id)

            //start sync service
            GlobalScope.launch(Dispatchers.Main) {
                if (sku.isSelectAble){
                    requireContext().startUploadingService(
                        ProjectTagDialog::class.java.simpleName,
                        ServerSyncTypes.CREATE
                    )
                }

                dismiss()
            }

        }

    }

    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            binding.etSkuName.setText(result.contents)
        }
    }

    private fun requiredError(editText: EditText, fieldName: String) {
        editText.error = "please enter " + fieldName
    }

    private fun removeWhiteSpace(toString: String) = toString.replace("\\s".toRegex(), "")

    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = ProjectTagDialogBinding.inflate(inflater, container, false)
}