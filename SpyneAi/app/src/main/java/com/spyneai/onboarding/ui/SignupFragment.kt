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
import com.spyneai.databinding.FragmentSignupBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboarding.data.viewmodels.OnBoardingViewModel
import com.spyneai.onboardingv2.ui.intro.ChooseCategoryActivity
import com.spyneai.onboardingv2.ui.onboarding.ReferralFragment
import com.spyneai.onboardingv2.ui.onboarding.SignInV3Activity
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import com.spyneai.posthog.captureIdentity
import java.util.HashMap

class SignupFragment : BaseFragment<OnBoardingViewModel, FragmentSignupBinding>() {

    var displayMessage = ""
    private val RC_SIGN_IN = 100
    var mGoogleSignInClient: GoogleSignInClient? = null
    var personName = ""
    var personGivenName = ""
    var personFamilyName = ""
    var personEmail = ""
    var personId = ""
    var personPhoto = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)


        listeners()
    }



    private fun listeners() {

        binding.flPhone.setOnClickListener {
            Intent(requireContext(), SignInV3Activity::class.java)
                .apply { startActivity(this) }
        }

        binding.tvSingIn.setOnClickListener {
            Intent(requireContext(), SignInV3Activity::class.java)
                .apply { startActivity(this) }
        }

        binding.btSignup.setOnClickListener {
            when {
                binding.etId.text.isNullOrEmpty()
                -> binding.etId.error = "Please enter email or phone number"
                TextUtils.isDigitsOnly(binding.etId.text) -> {
                    if (binding.etId.length() != 10) {
                        binding.etId.error = "Please enter valid phone number"
                    } else signup(
                        binding.etId.text.toString(),
                        binding.etPassword.text.toString()
                    )
                }
                !TextUtils.isDigitsOnly(binding.etId.text) -> {
                    if (!Utilities.isValidEmail(binding.etId.text.toString().trim()))
                        binding.etId.error = "Please enter valid email id"
                    else signup(
                        binding.etId.text.toString(),
                        binding.etPassword.text.toString()
                    )
                }
                binding.etPassword.text.isNullOrEmpty()
                -> binding.etPassword.error = "Please enter password"
                else -> signup(
                    binding.etId.text.toString(),
                    binding.etPassword.text.toString()
                )
            }
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
    }

    private fun signup(
        id: String,
        password: String,
    ) {
        binding.btSignup.isClickable = false

        val properties = HashMap<String,Any?>()
        properties.apply {
            this["email"] = id
        }

        requireContext().captureEvent(Events.SIGNUP_INTIATED, properties)
        Utilities.showProgressDialog(requireContext())

        val singupData: MutableMap<String, String> = HashMap()
        singupData["api_key"] = WhiteLabelConstants.API_KEY
        singupData["password"] = password
        singupData["strategy"] = "PASSWORD"
        singupData["source"] = "Android_app"

        if (TextUtils.isDigitsOnly(binding.etId.text))
            singupData["contact_no"] = id
        else
            singupData["email_id"] = id

        viewModel.signUp(singupData)

        viewModel.signupResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    //posthog
                    properties.apply {
                        this["user_id"] = id
                    }

                    Utilities.savePrefrence(requireContext(), AppConstants.ENTERPRISE_DISCOUNT, it.value.discount.toString())
                    Utilities.savePrefrence(requireContext(), AppConstants.PRICE_PER_CREDIT, it.value.price_per_credit.toString())

                    requireContext().captureEvent(Events.SIGNUP_SUCCEED, properties)
                    requireContext().captureIdentity(it.value.userId, properties)

                    Utilities.hideProgressDialog()
                    binding.btSignup.isClickable = true
                    Toast.makeText(
                        requireContext(),
                        "Signup successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    Utilities.savePrefrence(
                        requireContext(), AppConstants.AUTH_KEY,
                        it.value.auth_token
                    )

                    Utilities.savePrefrence(
                        requireContext(), AppConstants.ENTERPRISE_ID,
                        it.value.enterpriseId
                    )

                    displayMessage = it.value.displayMessage

                    if (it.value.newUser == 1){
                        ReferralFragment().show(requireActivity().supportFragmentManager,"ReferralFragment")
                    }else {
                        val intent = Intent(requireContext(), ChooseCategoryActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra(AppConstants.IS_NEW_USER, false)
                        startActivity(intent)
                    }

                }
                is Resource.Failure -> {
                    handleApiError(it)
                    Utilities.hideProgressDialog()
                    binding.btSignup.isClickable = true
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

        viewModel.signupResponse.observe(this, Observer {
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
                        requireContext(), AppConstants.AUTH_KEY,
                        it.value.auth_token
                    )

                    Utilities.savePrefrence(
                        requireContext(), AppConstants.ENTERPRISE_ID,
                        it.value.enterpriseId
                    )
                    requireContext().gotoHome()
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

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.showSignup.value = false
    }

    override fun getViewModel() = OnBoardingViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSignupBinding.inflate(inflater, container, false)
}