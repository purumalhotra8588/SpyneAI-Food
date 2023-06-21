package com.spyneai.registration.view.ui.activity



import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.ExperimentalPagingApi
import com.spyneai.base.BaseFragment
import com.spyneai.credits.TransactionWalletActivity
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.FragmentPreferenceV2Binding
import com.spyneai.logout.LogoutDialog
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.setLocale


@ExperimentalPagingApi
class PreferenceFragmentV2 : BaseFragment<DashboardViewModel, FragmentPreferenceV2Binding>(){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().setLocale()
        listeners()
        setText()
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPreferenceV2Binding.inflate(inflater, container, false)


    override fun getViewModel() = DashboardViewModel::class.java



    private fun setText() {
        binding.tvAappVersion.text = Utilities.getPreference(
            requireContext(),
            AppConstants.APP_VERSION
        )
        binding.walletCredits.text = Utilities.getPreference(
            requireContext(),
            AppConstants.CREDITS_USER
        )+" Credits"


        //Empty UserName, EmailId, Phone number Handled
        if (!Utilities.getPreference(requireContext(), AppConstants.USER_NAME)
                .isNullOrEmpty() && Utilities.getPreference(
                requireContext(),
                AppConstants.USER_NAME
            ) != "null"
        ) {
            binding.tvUserName.text =
                Utilities.getPreference(requireContext(), AppConstants.USER_NAME)
        } else if (!Utilities.getPreference(requireContext(), AppConstants.PHONE_NUMBER)
                .isNullOrEmpty()
        ) {
            Utilities.getPreference(
                requireContext(),
                AppConstants.USER_EMAIL
            )?.let {
                if (!it.isNullOrEmpty() && it != "null" && !it.contains("dummy") && !it.contains("Dummy")) {
                    binding.tvUserName.text =
                        Utilities.getPreference(requireContext(), AppConstants.PHONE_NUMBER)
                } else if (!Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID).isNullOrEmpty()) {
                    Utilities.getPreference(
                        requireContext(),
                        AppConstants.EMAIL_ID
                    )?.let {
                        if (!it.isNullOrEmpty() && it != "null" && !it.contains("dummy") && !it.contains("Dummy")) {
                            binding.tvUserName.text =
                                Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID)
                        }
                        else {
                            binding.tvUserName.text ="Welcome User!!"
                        }
                    }
                }

            }
        }
    }


    private fun listeners() {
        binding.LogoutContainer.setOnClickListener {
            LogoutDialog().show(requireActivity().supportFragmentManager, "LogoutDialog")
        }

        binding.tvLogout.setOnClickListener {
            LogoutDialog().show(requireActivity().supportFragmentManager, "LogoutDialog")
        }
        binding.tvmanage.setOnClickListener {
            viewModel.replacemanagepref.value=true
        }
        binding.rectangleNew.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.rectangle5.setOnClickListener {
            var intent = Intent(requireContext(), TransactionWalletActivity::class.java)
            requireContext().startActivity(intent)
        }
        binding.myWallet.setOnClickListener {
            var intent = Intent(requireContext(), TransactionWalletActivity::class.java)
            requireContext().startActivity(intent)
        }


        binding.profileTab.setOnClickListener {

            viewModel.prefrencefrag.value = true
        }

    }

}
