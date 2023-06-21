package com.spyneai.shootapp.ui.ecomwithgrid.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.CreateSkuEcomDialogBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shootapp.data.ShootViewModelApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreateSkuEcomDialog : BaseDialogFragment<ShootViewModelApp, CreateSkuEcomDialogBinding>() {

    val TAG = CreateSkuEcomDialog::class.java.simpleName
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: sku bar code scanner")

        when(Utilities.getPreference(requireContext(), AppConstants.SELECTED_CATEGORY_ID)){
            AppConstants.ECOM_CATEGORY_ID, AppConstants.FOOTWEAR_CATEGORY_ID-> {
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

        isCancelable = false

        binding.etSkuName.setText(viewModel.skuApp?.skuName)

        binding.ivBarCode?.setOnClickListener {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES)
            options.setPrompt("Scan a barcode")
            options.setCameraId(0) // Use a specific camera of the device
            options.setBeepEnabled(true)
            options.setOrientationLocked(false)
            barcodeLauncher.launch(options)
        }

        binding.ivClose.setOnClickListener {
            viewModel.isProjectNameEdited.value = false
            dismiss()
        }


        binding.btnProceed.setOnClickListener {
            binding.etSkuName?.let {
                when {
                    it.text.toString().isEmpty() -> {
                        it.error = "Please enter product name"
                    }
                    it.text.toString()
                        .contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex()) -> {
                        it.error = "Special characters not allowed"
                    }
                    else -> {
                        GlobalScope.launch(Dispatchers.IO) {
                            viewModel.updateSkuName(
                                viewModel.skuApp!!.uuid,
                                removeWhiteSpace(it.text.toString())
                            )
                        }
                        GlobalScope.launch(Dispatchers.Main){
                            viewModel.skuApp!!.skuName=removeWhiteSpace(it.text.toString())
                            Toast.makeText(requireContext(),"Sku Name Updated",Toast.LENGTH_SHORT).show()
                            viewModel.isSkuNameAdded.value=true
                        }
                    }
                }
            }
            dismiss()
        }
    }


    private fun removeWhiteSpace(toString: String) = toString.replace("\\s".toRegex(), "")



    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->

        Log.d(TAG, "result: ${result.contents}")

        if (result.contents == null) {
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            binding.etSkuName.setText(result.contents)
        }
    }


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
    ) = CreateSkuEcomDialogBinding.inflate(inflater, container, false)

}
