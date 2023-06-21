package com.spyneai.onboardingv2.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentReferralBinding
import com.spyneai.loginsignup.models.GetCountriesResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboarding.data.viewmodels.OnBoardingViewModel
import com.spyneai.onboardingv2.ui.intro.ChooseCategoryActivity
import java.util.HashMap

class ReferralFragment : BaseDialogFragment<OnBoardingViewModel, FragmentReferralBinding>() {

    private val TAG = ReferralFragment::class.java.simpleName
    var countriesList = ArrayList<String>()
    lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        //set countries spinner
        countriesList.add(getString(R.string.select_country))
        spinnerAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            countriesList
        )

        binding.countiesSpinner.adapter = spinnerAdapter

        setSpinner()

        binding.btnSubmit.setOnClickListener {
            when{
                binding.etBusinessName.text.isEmpty() && binding.countiesSpinner.selectedItemPosition == 0 -> {
                    Toast.makeText(requireContext(),"Please enter name or select country",Toast.LENGTH_LONG).show()
                }
                else -> {
                    val map = HashMap<String,String>()

                    if (!binding.etBusinessName.text.isNullOrEmpty()){
                        map["user_name"] = binding.etBusinessName.text.toString()
                        Utilities.savePrefrence(requireContext(),AppConstants.USER_NAME,binding.etBusinessName.text.toString())
                    }


                    if (binding.countiesSpinner.selectedItem != 0)
                        map["country"] = binding.countiesSpinner.selectedItem.toString()

                    if (!binding.etReferral.text.isNullOrEmpty())
                        map["referral_code"] = binding.etReferral.text.toString()

                        map["auth_key"] = Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString()

                    updateData(map)
                    observeUpdate(map)
                }
            }
        }

        binding.tvSkip.setOnClickListener {
            chooseCategory()
            dismiss()
        }

    }

    private fun chooseCategory() {
        val intent = Intent(requireContext(), ChooseCategoryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(AppConstants.IS_NEW_USER, false)
        startActivity(intent)
    }

    private fun observeUpdate(map: HashMap<String, String>) {
        viewModel.message.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    Utilities.savePrefrence(requireContext(), AppConstants.AUTH_KEY, it.value.data.sso_token)
                    Utilities.savePrefrence(requireContext(), AppConstants.PRICE_PER_CREDIT, it.value.data.price_per_credit.toString())
                    Utilities.savePrefrence(requireContext(), AppConstants.ENTERPRISE_DISCOUNT, it.value.data.discount.toString())
                    Utilities.hideProgressDialog()
                    chooseCategory()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) {
                        updateData(map)
                    }
                }

                else -> {

                }
            }
        }
    }

    private fun updateData(map: HashMap<String, String>) {
        Utilities.showProgressDialog(requireContext())
        viewModel.updateCountry(map = map)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(null)
    }

    private fun setSpinner() {

        val countriesData = resources.openRawResource(R.raw.countires)
            .bufferedReader().use { it.readText() }

        var response = Gson().fromJson(countriesData, GetCountriesResponse::class.java)

        for (item in response.data) {
            countriesList.add(item.name)
        }

        setData(countriesList)

    }

    private fun setData(countriesList: java.util.ArrayList<String>) {
        binding.countryProgressBar.isVisible = false
        spinnerAdapter.addAll(countriesList)
    }

    override fun getViewModel() = OnBoardingViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReferralBinding.inflate(inflater,container,false)
}