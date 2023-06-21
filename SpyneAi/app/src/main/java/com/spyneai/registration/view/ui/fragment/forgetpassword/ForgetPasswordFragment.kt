package com.spyneai.registration.view.ui.fragment.forgetpassword

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.databinding.FragmentForgetPasswordBinding
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.registration.viewmodels.RegistrationViewModel


class ForgetPasswordFragment : Fragment() {

    lateinit var binding: FragmentForgetPasswordBinding
    private val viewModel: RegistrationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        clickListener()
        visibilityLogic()
    }

    private fun visibilityLogic() {
        binding.edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length!! >= 0) {
                    binding.btRequestPwd.isClickable = true
                    binding.btRequestPwd.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.primary_light_dark
                        )
                    )
                    binding.tvResetLink.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                } else {
                    binding.btRequestPwd.isClickable = false
                    binding.btRequestPwd.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    binding.tvResetLink.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.black))
                }
            }

        })
    }

    private fun clickListener() {
        binding.apply {
            btRequestPwd.setOnClickListener {
                if (binding.edtEmail.text.toString().trim().isNotEmpty()
                    && Utilities.isValidEmail(binding.edtEmail.text.toString().trim())
                ) {
                    forgotPassword()
                }
            }
            tvBack.setOnClickListener {
                findNavController().navigate(R.id.action_forget_pwd_fragment_to_login_fragment)
            }
            btSentOtp.setOnClickListener {
                findNavController().navigate(R.id.action_forget_pwd_fragment_to_reset_fragment)
            }

        }

    }

    private fun forgotPassword() {
        val properties = HashMap<String, Any?>().apply {
            this["email"] = binding.edtEmail.text.toString().trim()
        }
        requireContext().captureEvent(Events.FORGOT_PASSWORD_INTIATED, properties)
        Utilities.showProgressDialog(requireContext())
        val forgetPwdData: MutableMap<String, String> = java.util.HashMap()
        forgetPwdData["api_key"] = WhiteLabelConstants.API_KEY
        forgetPwdData["email"] = binding.edtEmail.text.toString().trim()
        forgetPwdData["type"] = "reset-password"
        viewModel.forgetPwd(forgetPwdData)
        observeForgetPwd()
    }

    private fun observeForgetPwd() {
        val properties = HashMap<String, Any?>().apply {
            this["email"] = binding.edtEmail.text.toString().trim()
        }
        viewModel.forgetPwdResp.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    showFreeCreditDialog()
                    requireContext().captureEvent(Events.FORGOT_PASSWORD_MAIL_SENT, properties)
                    Handler(Looper.getMainLooper()).postDelayed({
                        findNavController().navigate(R.id.action_forget_pwd_fragment_to_login_fragment)
                    },2000)
                }
                is Resource.Failure -> {
                    requireContext().captureEvent(Events.FORGOT_PASSWORD_FAILED, properties)
                    Utilities.hideProgressDialog()
                    Toast.makeText(
                        requireContext(),
                        "Server not responding!, Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                }
            }
        }
    }

    private fun showFreeCreditDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.email_sent_dialogue, null)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val ivClose: ImageView = dialogView.findViewById(R.id.ivClose)
        ivClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


}