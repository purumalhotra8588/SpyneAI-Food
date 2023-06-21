package com.spyneai.shootapp.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.CreateSkuDialogBinding
import com.spyneai.shootapp.data.ShootViewModelApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreateSkuDialog: BaseDialogFragment<ShootViewModelApp, CreateSkuDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)
        binding.etSkuName.setText(viewModel.skuApp?.skuName)


        binding.ivClose.setOnClickListener {
            requireActivity().onBackPressed()
        }


        binding.btnProceed.setOnClickListener {
            binding.etSkuName.let {
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
    ) = CreateSkuDialogBinding.inflate(inflater, container, false)

}
