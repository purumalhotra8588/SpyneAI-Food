package com.spyneai.registration.view.ui.fragment.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentUserDetailBinding
import com.spyneai.loginsignup.models.GetCountriesResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.data.SelectCategoryRes
import com.spyneai.onboardingv2.data.UpdateUserDetailBody
import com.spyneai.onboardingv2.ui.intro.ChooseCategoryActivity
import com.spyneai.registration.viewmodels.RegistrationViewModel
import kotlinx.coroutines.launch
import java.util.*


class UserDetailFragment : Fragment() {

    lateinit var binding: FragmentUserDetailBinding
    private val viewModel: RegistrationViewModel by viewModels()
    var countriesList = ArrayList<String>()
    private var loginType: String? = ""
    lateinit var spinnerAdapter: ArrayAdapter<String>
    lateinit var spCategoryAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.arguments?.apply {
            loginType = this.getString("reqOTP", "")
            Log.d("LOGINTYPE", "onViewCreated: $loginType")
        }
        if (loginType == "sign_up_enterprise_flow") {
            binding.etEnterpriseCode.visibility = View.GONE
            binding.etReferral.visibility = View.GONE
            binding.btSkip.visibility = View.GONE
            binding.signupCompanyName.visibility = View.VISIBLE
            binding.llCompanyName.visibility = View.VISIBLE
            binding.edtCompanyName.visibility = View.VISIBLE
            binding.flCategory.visibility = View.VISIBLE
            binding.tvSelectCategory.visibility = View.VISIBLE
        } else {
            binding.etEnterpriseCode.visibility = View.VISIBLE
            binding.etReferral.visibility = View.VISIBLE
            binding.etReferral.text =
                Utilities.getPreference(requireContext(), AppConstants.ENTERPRISE_CODE)
            //binding.btSkip.visibility = View.VISIBLE
            binding.signupCompanyName.visibility = View.GONE
            binding.llCompanyName.visibility = View.GONE
            binding.edtCompanyName.visibility = View.GONE
            binding.flCategory.visibility = View.GONE
            binding.tvSelectCategory.visibility = View.GONE
        }
        initView()


        val categoryData = resources.openRawResource(R.raw.select_categoires)
            .bufferedReader().use { it.readText() }

        var response = Gson().fromJson(categoryData, SelectCategoryRes::class.java)
        val categorylist = response.data.sortedBy {
            it.priority
        }

        var categoryNameList = ArrayList<String>()
        categorylist.forEach {
            categoryNameList.add(it.categoryName)
        }

        spCategoryAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categoryNameList
        )
        binding.categorySpinner.adapter = spCategoryAdapter

        Utilities.savePrefrence(
            requireContext(),
            AppConstants.USER_CATEGORY,
            categoryNameList[0]
        )


        binding.categorySpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                Utilities.savePrefrence(
                    requireContext(),
                    AppConstants.USER_CATEGORY,
                    categoryNameList[position]
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun initView() {
        countriesList.add(getString(R.string.select_country))
        spinnerAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            countriesList
        )
        binding.countiesSpinner.adapter = spinnerAdapter
        setSpinner()
        clickListener()
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

    private fun setData(countriesList: ArrayList<String>) {
        binding.countryProgressBar.isVisible = false
        spinnerAdapter.addAll(countriesList)
        val loc = Locale("", checkCurrentCounty())
        binding.countiesSpinner.setSelection(getIndex(binding.countiesSpinner, loc.displayCountry))

    }


    private fun getIndex(spinner: Spinner, checkCurrentCounty: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == checkCurrentCounty) {
                return i
            }
        }

        return 0
    }

    private fun clickListener() {
        binding.ivBgPermission.setOnClickListener {
            val loc = Locale("", checkCurrentCounty())
            //  binding.tvCountry.setText(loc.displayCountry)
            binding.ivBgPermissionSelected.visibility = View.VISIBLE
            binding.ivBgPermission.visibility = View.GONE
            Log.d("Country", "clickListener: " + loc.displayCountry)
        }

        binding.btContinue.setOnClickListener {
            // SERVICE need to implement
            if (binding.countiesSpinner.selectedItemPosition == 0) {
                Toast.makeText(
                    requireContext(),
                    "Please select a valid country",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID_REG_)
                    .equals("")
            ) {

//                if (binding.edtCompanyName.text.isNullOrEmpty()) {
//                    binding.edtCompanyName.error = "Company name is mandatory for Business"
//                } else {
                val map = HashMap<String, String>()
                if (!binding.edtName.text.isNullOrEmpty()) {
                    map["name"] = binding.edtName.text.toString()
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_NAME,
                        binding.edtName.text.toString()
                    )
                    if (binding.countiesSpinner.selectedItem != 0)
                        map["countryName"] = binding.countiesSpinner.selectedItem.toString()

                    if (!binding.etReferral.text.isNullOrEmpty())
                        map["referral_code"] = binding.etReferral.text.toString()
//

                    if (loginType == "sign_up_enterprise_flow") {
                        updateDataEnterprise()
                    } else {
                        val updateUserDetailBody = UpdateUserDetailBody(
                            name = binding.edtName.text.toString(),
                            auth_key = Utilities.getPreference(
                                requireContext(),
                                AppConstants.AUTH_KEY
                            ).toString(),
                            countryName = binding.countiesSpinner.selectedItem.toString(),
                            referral_code = binding.etReferral.text.toString()
                        )
                        updateDataStandard(updateUserDetailBody)
                        observeUpdateStandard(updateUserDetailBody)
                    }
                } else {
                    binding.edtName.error = "Please enter a name"
                }
//                }
            } else {
                val map = HashMap<String, String>()

                if (!binding.edtName.text.isNullOrEmpty()) {
                    map["name"] = binding.edtName.text.toString()
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_NAME,
                        binding.edtName.text.toString()
                    )
                    if (binding.countiesSpinner.selectedItem != 0)
                        map["countryName"] = binding.countiesSpinner.selectedItem.toString()

                    if (!binding.etReferral.text.isNullOrEmpty())
                        map["referral_code"] = binding.etReferral.text.toString()
                    binding.etReferral.text =
                        Utilities.getPreference(requireContext(), AppConstants.ENTERPRISE_CODE)


                    if (loginType == "sign_up_enterprise_flow") {
                        updateDataEnterprise()
                    } else {
                        val updateUserDetailBody = UpdateUserDetailBody(
                            name = binding.edtName.text.toString(),
                            auth_key = Utilities.getPreference(
                                requireContext(),
                                AppConstants.AUTH_KEY
                            ).toString(),
                            countryName = binding.countiesSpinner.selectedItem.toString(),
                            referral_code = binding.etReferral.text.toString()
                        )
                        updateDataStandard(updateUserDetailBody)
                        observeUpdateStandard(updateUserDetailBody)
                    }
                } else {
                    binding.edtName.error = "Please enter a name"
                }

            }
        }
        binding.btSkip.setOnClickListener {
            startActivity(Intent(requireContext(), ChooseCategoryActivity::class.java))
            Utilities.savePrefrence(requireContext(), AppConstants.COMPANY_NAME, "abc124")
            requireActivity().finishAffinity()
        }
    }

    private fun observeUpdateStandard(body: UpdateUserDetailBody) {
        viewModel.updateUserDetail.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.AUTH_KEY,
                        it.value.data.sso_token
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.PRICE_PER_CREDIT,
                        it.value.data.price_per_credit.toString()
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.ENTERPRISE_DISCOUNT,
                        it.value.data.discount.toString()
                    )
                    chooseCategory()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) {
                        updateDataStandard(body)
                    }
                }
                else -> {}
            }
        }
    }

    private fun chooseCategory() {
        startActivity(Intent(requireContext(), ChooseCategoryActivity::class.java))
        Utilities.savePrefrence(
            requireContext(),
            AppConstants.COMPANY_NAME,
            binding.edtCompanyName.text.toString()
        )
        requireActivity().finishAffinity()
    }

    private fun updateDataStandard(body: UpdateUserDetailBody) {
        Utilities.showProgressDialog(requireContext())
        viewModel.updateUserDetail(body)
    }

    private fun updateDataEnterprise() {
        val map = HashMap<String, String>()
        map["name"] = binding.edtName.text.toString()
        map["company_name"] = binding.edtCompanyName.text.toString()
        map["country"] = binding.countiesSpinner.selectedItem.toString()
        map["category"] =
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.USER_CATEGORY)
                .toString()
        map["phone"] =
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.PHONE_NUMBER)
                .toString()
        lifecycleScope.launch {
            viewModel.createEnterPrise(map)
            Utilities.showProgressDialog(requireContext())
        }
        viewModel.createEnterPrise.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_ENTERPRISE,
                        it.value.data?.enterpriseId
                    )
                    chooseCategory()
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { updateDataEnterprise() }
                }

                else -> {}
            }
        }

    }

    private fun checkCurrentCounty(): String {
        try {
            val tm =
                requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simCountry = tm.simCountryIso
            if (simCountry != null && simCountry.length == 2) { // SIM country code is available
                return simCountry.lowercase(Locale.US)
            } else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                val networkCountry = tm.networkCountryIso
                if (networkCountry != null && networkCountry.length == 2) { // network country code is available
                    return networkCountry.lowercase(Locale.US)
                }
            }
        } catch (e: Exception) {
        }
        return ""
    }
}
