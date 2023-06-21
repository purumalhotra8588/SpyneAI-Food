package com.spyneai.logout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentInvalidAuthDialogBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.registration.view.ui.activity.RegistrationBaseActivity
import com.spyneai.shootapp.data.ShootViewModelApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class InvalidAuthDialog : BaseDialogFragment<ShootViewModelApp, FragmentInvalidAuthDialogBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.btnLogin.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                viewModel.deleteCategoryData()

                GlobalScope.launch(Dispatchers.Main) {


                        Utilities.savePrefrence(requireContext(), AppConstants.PRESSED_LOGOUT, "1")
                        Utilities.savePrefrence(requireContext(), AppConstants.TOKEN_ID, "")
                        Utilities.savePrefrence(requireContext(), AppConstants.AUTH_KEY, "")
                        Utilities.savePrefrence(requireContext(), AppConstants.PROJECT_ID, "")
                        Utilities.savePrefrence(requireContext(), AppConstants.SHOOT_ID, "")
                        Utilities.savePrefrence(requireContext(), AppConstants.SKU_ID, "")
                        Utilities.saveBool(requireContext(), AppConstants.VERIFICATION, false)
                        Utilities.savePrefrence(requireContext(), AppConstants.USER_ID, "")
                        Utilities.savePrefrence(requireContext(), AppConstants.EMAIL_ID_REG_, "")
                        val intent = Intent(requireContext(), RegistrationBaseActivity::class.java)
                        startActivity(intent)
                        requireActivity().finishAffinity()
                        dismiss()

                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentInvalidAuthDialogBinding.inflate(inflater, container, false)

}