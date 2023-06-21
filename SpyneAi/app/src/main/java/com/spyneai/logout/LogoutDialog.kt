package com.spyneai.logout

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.dashboardV2.data.model.LogoutBody
import com.spyneai.databinding.DialogLogoutBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.registration.view.ui.activity.RegistrationBaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogoutDialog : com.spyneai.dashboard.ui.base.BaseDialogFragment<DashboardViewModel, DialogLogoutBinding>() {

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )  = DialogLogoutBinding.inflate(inflater, container, false)

    override fun getViewModelClass() = DashboardViewModel::class.java


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.llLogout.setOnClickListener {
            if(Utilities.getBool(requireContext(),AppConstants.NEW_ENTERPRISE_USER,false)){
                newUserLogout()
            }else{
                onLogout()
            }
        }
    }

    private fun newUserLogout(){
        Utilities.showProgressDialog(requireContext())
        val body = LogoutBody(
            deviceIdList = listOf(Utilities.getPreference(requireContext(),AppConstants.LOGIN_UUID).toString())
        )
        viewModel.newUserLogout(body)

        observeLogout()
    }


    private fun observeLogout(){
        viewModel.logoutResponse.observe(viewLifecycleOwner){
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    onLogout()
                }
                is Resource.Loading -> {}

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) {
                        newUserLogout()
                    }
                }
            }
        }
    }


    private fun onLogout(){
        GlobalScope.launch(Dispatchers.IO) {

            viewModel.deleteCategoryData()



            GlobalScope.launch(Dispatchers.Main){


                requireContext().captureEvent(Events.LOG_OUT, HashMap<String, Any?>())
                Utilities.savePrefrence(requireContext(), AppConstants.PRESSED_LOGOUT, "1")
                Utilities.savePrefrence(requireContext(), AppConstants.TOKEN_ID, "")
                Utilities.savePrefrence(requireContext(), AppConstants.AUTH_KEY, "")
                Utilities.savePrefrence(requireContext(), AppConstants.PROJECT_ID, "")
                Utilities.savePrefrence(requireContext(), AppConstants.SHOOT_ID, "")
                Utilities.savePrefrence(requireContext(), AppConstants.SKU_ID, "")
                Utilities.saveBool(requireContext(), AppConstants.VERIFICATION, false)
                Utilities.saveBool(requireContext(), AppConstants.NEW_ENTERPRISE_USER, false)
                Utilities.savePrefrence(requireContext(), AppConstants.USER_ID, "")
                Utilities.savePrefrence(requireContext(), AppConstants.EMAIL_ID_REG_, "")
                Utilities.savePrefrence(requireContext(), AppConstants.USER_NAME_NEW, "")
                Intent.FLAG_ACTIVITY_CLEAR_TASK
                val intent = if (getString(R.string.app_name) == AppConstants.SWIGGY) Intent(
                    requireContext(),
                    RegistrationBaseActivity::class.java
                ) else
                    Intent(requireContext(), RegistrationBaseActivity::class.java)
                startActivity(intent)
                requireActivity().finishAffinity()
                dismiss()
            }
        }
    }


    override fun onResume() {
        super.onResume()

        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

}