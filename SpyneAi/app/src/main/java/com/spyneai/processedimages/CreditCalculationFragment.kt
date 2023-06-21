package com.spyneai.processedimages

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.credits.model.CreditResourceBody
import com.spyneai.dashboard.ui.enable
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentCreditCalculationBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.processedimages.data.ProcessedViewModelApp

class CreditCalculationFragment : BaseDialogFragment<ProcessedViewModelApp, FragmentCreditCalculationBinding>(){

    var creditDeficiets = 0
    var availableCreditBalance = 0
    val TAG = "CreditCalculationFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.tvTotalImageCount.text= Utilities.getPreference(requireContext(),AppConstants.TOTAL_IMAGES)+" Images"

        calculateCredits()
        observeCalculateCredits()


        binding.btBuyCredit.setOnClickListener {

        }

        binding.btDownload.setOnClickListener {
            val body = CreditResourceBody(
                resource_id = Utilities.getPreference(requireContext(), AppConstants.SKU_ID).toString(),
                resource_type = "sku",
                source = "android")


            viewModel.deductCredits(body)

            viewModel.deductCreditRes.observe(this) {
                when (it) {
                    is Resource.Success -> {
                        Utilities.hideProgressDialog()
                        Toast.makeText(requireContext(),"Credit Deducted Successfully",Toast.LENGTH_SHORT).show()
                        viewModel.downloadImage.value=true
                        dismiss()

                    }

                    is Resource.Failure -> {
                        Utilities.hideProgressDialog()
                        handleApiError(it) { viewModel.deductCredits(body) }
                    }
                    else -> {

                    }
                }
            }

        }



    }


    private fun calculateCredits(){
        Utilities.showProgressDialog(requireContext())

        val body = CreditResourceBody(
            resource_id = Utilities.getPreference(requireContext(), AppConstants.SKU_ID).toString(),
            resource_type = "sku",
            source = "android")

        viewModel.calculateCredits(body)
    }

    @SuppressLint("SetTextI18n")
    private fun observeCalculateCredits() {
        viewModel.calculateCreditsRes.observe(this) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    availableCreditBalance = it.value.data.user_available_credits
                    binding.tvCreditAvailableValue.text=it.value.data.user_available_credits.toString()+" Credits"
                    binding.tvTotalCostValue.text=it.value.data.total_credits.minus(it.value.data.paid_credits).toString()+" Credits"

                    if(it.value.data.user_available_credits<(it.value.data.total_credits-it.value.data.paid_credits)){
                        binding.btDownload.enable(false)
                        creditDeficiets=it.value.data.total_credits.minus(it.value.data.paid_credits).minus(it.value.data.user_available_credits)
                    }else{
                        binding.btDownload.enable(true)
                        creditDeficiets = 0

                    }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { calculateCredits() }
                }
                else -> {}
            }
        }
    }



    override fun getViewModel() = ProcessedViewModelApp::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCreditCalculationBinding.inflate(inflater, container, false)

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }
}