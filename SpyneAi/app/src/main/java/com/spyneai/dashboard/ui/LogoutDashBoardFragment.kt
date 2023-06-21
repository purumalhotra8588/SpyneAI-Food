package com.spyneai.dashboard.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spyneai.R
import com.spyneai.databinding.LogoutDialogBinding
import com.spyneai.loginsignup.activity.SignInV3Activity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.registration.view.ui.activity.RegistrationBaseActivity


class LogoutDashBoardFragment : Fragment() {

    private var _binding: LogoutDialogBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = LogoutDialogBinding.inflate(inflater, container, false)

        if (Utilities.getPreference(requireContext(), AppConstants.USER_EMAIL).toString() != "") {
            binding.tvUserEmail.text =
                "(" + Utilities.getPreference(requireContext(), AppConstants.USER_EMAIL) + ")"
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.llLogout.setOnClickListener {
            requireContext().captureEvent(Events.LOG_OUT, HashMap<String, Any?>())
            Utilities.savePrefrence(requireContext(), AppConstants.TOKEN_ID, "")
            Utilities.savePrefrence(requireContext(), AppConstants.AUTH_KEY, "")
            Utilities.savePrefrence(requireContext(), AppConstants.PROJECT_ID, "")
            Utilities.savePrefrence(requireContext(), AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(requireContext(), AppConstants.SKU_ID, "")
            Utilities.savePrefrence(requireContext(), AppConstants.PRESSED_LOGOUT, "1")
            Intent.FLAG_ACTIVITY_CLEAR_TASK
            val intent = if (getString(R.string.app_name) == AppConstants.SWIGGY) Intent(
                requireContext(),
                SignInV3Activity::class.java
            )
            else Intent(requireContext(), RegistrationBaseActivity::class.java)
            startActivity(intent)
            requireActivity().finishAffinity()
        }
    }


}