package com.spyneai.onboarding.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.spyneai.*
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentPhoneOtpBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboarding.data.viewmodels.OnBoardingViewModel
import com.spyneai.onboardingv2.ui.intro.ChooseCategoryActivity
import com.spyneai.onboardingv2.ui.onboarding.LoginInWithEmailActivity
import com.spyneai.onboardingv2.ui.onboarding.ReferralFragment
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import com.spyneai.posthog.captureIdentity
import java.util.HashMap

class PhoneOtpFragment :
    BaseFragment<OnBoardingViewModel, FragmentPhoneOtpBinding>() {

    override fun getViewModel() = OnBoardingViewModel::class.java
    private val RC_SIGN_IN = 100
    var mGoogleSignInClient: GoogleSignInClient? = null
    var personName = ""
    var personGivenName = ""
    var personFamilyName = ""
    var personEmail = ""
    var personId = ""
    var personPhoto = ""

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPhoneOtpBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)


        listeners()
    }

    private fun listeners() {
        binding.tvDontHaveAccount.setOnClickListener {
            Intent(requireContext(), LoginInWithEmailActivity::class.java)
                .apply { startActivity(this) }
        }

//        binding.tvSignIN.setOnClickListener {
//            Intent(requireContext(), SignUpV2Activity::class.java)
//                .apply { startActivity(this) }
//        }

        binding.flEmail.setOnClickListener {
            Intent(requireContext(), LoginInWithEmailActivity::class.java)
                .apply { startActivity(this) }
        }

        binding.flGoogle.setOnClickListener {
            val acct = GoogleSignIn.getLastSignedInAccount(requireContext())
            if (acct != null) {
                personName = acct.displayName.toString()
                personGivenName = acct.givenName.toString()
                personFamilyName = acct.familyName.toString()
                personEmail = acct.email.toString()
                personId = acct.id.toString()
                personPhoto = acct.photoUrl.toString()
                signupWithGoogle()
            } else {
                signIn()
//                val signInIntent = mGoogleSignInClient?.signInIntent
//                googleStartForResult.launch(signInIntent)
            }
        }


//        binding.tvUsePassword.setOnClickListener {
//            viewModel.usePassword.value = true
//        }

        binding.btnRequestOtp.setOnClickListener {
            when {
                binding.etId.text.isNullOrEmpty()
                -> binding.etId.error = "Please enter phone number"

                TextUtils.isDigitsOnly(binding.etId.text) -> {
                    if (binding.etId.length() != 10) {
                        binding.etId.error = "Please enter valid phone number"
                    } else {
                        reqOtp(binding.etId.text.toString().trim())
                    }
                }
                !TextUtils.isDigitsOnly(binding.etId.text) -> {
                    binding.etId.error = "Please enter valid phone number"
//                    if (!Utilities.isValidEmail(binding.etId.text.toString().trim()))
//                        binding.etId.error = "Please enter valid email id"
//                    else {
//                        reqOtp(binding.etId.text.toString().trim())
//                    }
                }
                else -> {
                    reqOtp(binding.etId.text.toString().trim())
                }
            }
        }
    }

    private fun reqOtp(id: String) {
        binding.btnRequestOtp.isClickable = false
        val properties = HashMap<String, Any?>()
            .apply {
                this.put("email/phone", id)
            }

        requireContext().captureEvent(Events.OTP_LOGIN_INTIATED, properties)
        Utilities.showProgressDialog(requireContext())

        Utilities.savePrefrence(requireContext(),AppConstants.ID,binding.etId.text.toString())
        viewModel.reqOtp()
        observeReqOtp()
    }

    private fun observeReqOtp() {

        val properties = HashMap<String, Any?>()
        viewModel.reqOtpResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    properties.apply {
                        this["email/phone"] = binding.etId.text
                    }
                    requireContext().captureEvent(Events.OTP_LOGIN_SUCCEED, properties)
                    requireContext().captureIdentity(it.value.userId, properties)

                    binding.btnRequestOtp.isClickable = true
                    Toast.makeText(requireContext(), "OTP sent!", Toast.LENGTH_SHORT).show()

                    if (TextUtils.isDigitsOnly(binding.etId.text))
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.PHONE_NUMBER, binding.etId.text.toString()
                        )
                    else
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.EMAIL_ID, binding.etId.text.toString()
                        )

                    // send user to enter otp fragment
                    viewModel.reqOtpSuccess.value = true
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                    requireContext().captureEvent(Events.OTP_LOGIN_FAILED, properties)
                    binding.btnRequestOtp.isClickable = true
//                    handleApiError(it)
                    Toast.makeText(
                        requireContext(),
                        "OTP sent failed: " + it.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {

                }
            }
        })
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            personName = account.displayName.toString()
            personGivenName = account.givenName.toString()
            personFamilyName = account.familyName.toString()
            personEmail = account.email.toString()
            personId = account.id.toString()
            personPhoto = account.photoUrl.toString()

            Utilities.savePrefrence(requireContext(), AppConstants.USER_EMAIL, personEmail)


            val properties = HashMap<String, Any?>()
            properties.apply {
                this["Name"] = personName
                this["Email"] = personEmail
                this["Id"] = personId
            }
            requireContext().captureEvent(Events.SIGNUP_SUCCEED, properties)
            requireContext().captureIdentity(personId, properties)
            signupWithGoogle()

        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            Toast.makeText(requireContext(), "Google signup failed: "+e.localizedMessage, Toast.LENGTH_SHORT).show()
            //updateUI(null)
        }
    }

    private fun signupWithGoogle(){
        val properties = HashMap<String,Any?>()
        properties.apply {
            this["email"] = personEmail
            this["name"] = personName
        }

        val singupData: MutableMap<String, String> = HashMap()
        singupData["api_key"] = WhiteLabelConstants.API_KEY
        singupData["user_name"] = personName
        singupData["password"] = ""
        singupData["strategy"] = "GOOGLE"
        singupData["country"] = ""
        singupData["source"] = "Android_app"
        singupData["coupon_code"] = ""
        singupData["email_id"] = personEmail

        viewModel.signUp(singupData)

        viewModel.signupResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    //posthog
                    properties.apply {
                        this["user_id"] = personId
                        this["email"] = personEmail
                        this["name"] = personName
                    }

                    requireContext().captureEvent(Events.SIGNUP_SUCCEED, properties)
                    requireContext().captureIdentity(it.value.userId, properties)

                    Utilities.hideProgressDialog()
                    Toast.makeText(
                        requireContext(),
                        "Signup successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.ENTERPRISE_DISCOUNT,
                        it.value.discount.toString()
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.PRICE_PER_CREDIT,
                        it.value.price_per_credit.toString()
                    )

                    Utilities.savePrefrence(
                        requireContext(), AppConstants.AUTH_KEY,
                        it.value.auth_token
                    )

                    Utilities.savePrefrence(
                        requireContext(), AppConstants.ENTERPRISE_ID,
                        it.value.enterpriseId
                    )
                    if (it.value.newUser == 1) {
                        ReferralFragment().show(
                            requireActivity().supportFragmentManager,
                            "ReferralFragment"
                        )
                    } else {
                        val intent = Intent(requireContext(), ChooseCategoryActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra(AppConstants.IS_NEW_USER, false)
                        startActivity(intent)
                    }
                    Utilities.hideProgressDialog()

                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                    Utilities.hideProgressDialog()
                    requireContext().captureFailureEvent(
                        Events.SIGNUP_FAILED,
                        properties,
                        it.errorMessage.toString()+it.errorCode)
                }
                else -> {

                }
            }
        })
    }


}