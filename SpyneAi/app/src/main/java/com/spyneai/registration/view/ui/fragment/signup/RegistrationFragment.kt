package com.spyneai.registration.view.ui.fragment.signup

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.spyneai.R
import com.spyneai.TAG
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.dashboard.ui.enable
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentChooseYourPlanBinding
import com.spyneai.databinding.FragmentMobileOtpBinding
import com.spyneai.gotoHomeSignUp
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import com.spyneai.posthog.captureIdentity
import com.spyneai.registration.viewmodels.RegistrationViewModel
import kotlinx.android.synthetic.main.fragment_mobile_otp.*


class RegistrationFragment : Fragment() {
    lateinit var binding: FragmentMobileOtpBinding
    var mGoogleSignInClient: GoogleSignInClient? = null
    var personName = ""
    var personGivenName = ""
    var personFamilyName = ""
    var personEmail = ""
    var personId = ""
    var personPhoto = ""
    private val RC_SIGN_IN = 100
    private val viewModel: RegistrationViewModel by viewModels()
    private var isEnterPrise = false
    private var isStandard = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMobileOtpBinding.inflate(inflater, container, false)

        binding.tvCountryCode.registerPhoneNumberTextView(binding.edtPhoneNumber)
        val drawable = binding.rlPhone.background as GradientDrawable

        if(binding.tvCountryCode.selectedCountryCode!="91") {
            binding.tvNotAvailable.visibility = View.VISIBLE
            binding.ivCountryWarning.visibility = View.VISIBLE

            drawable.setStroke(4, ContextCompat.getColor(requireContext(), R.color.not_available))
            binding.view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.not_available))
        }
        else {
            binding.ivCountryWarning.visibility = View.GONE
            binding.tvNotAvailable.visibility = View.GONE
            drawable.setStroke(4, ContextCompat.getColor(requireContext(), R.color.line_view))
            binding.view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.line_view))
        }



        initView()
        return binding.root
    }

    private fun initView() {
        getGoogleSignInClient()
        clickListener()

    }

    private fun getGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clickListener() {
        binding.apply {
            btSentOtp.setOnClickListener {
                val bundle  =  Bundle()
                if (!edtPhoneNumber.text.isNullOrEmpty()) {
                    when {
                        edtPhoneNumber.text.isNullOrEmpty() ->
                            edtPhoneNumber.error = "Please Enter Phone Number"

                        TextUtils.isDigitsOnly(binding.edtPhoneNumber.text) -> {

                            if (!tvCountryCode.isValid) {
                                edtPhoneNumber.error = "Please Enter Valid Phone Number"
                            } else {
                                Utilities.savePrefrence(
                                    requireContext(), AppConstants.ID,
                                    binding.edtPhoneNumber.text.toString()
                                )
                                Utilities.saveBool(
                                    requireContext(), AppConstants.RESEND_OTP,
                                    false
                                )

                                bundle.putString("reqOTP","sign_up_flow")
                                reqOtp(edtPhoneNumber.text.toString().trim(),bundle)
                            }
                        }
                        !TextUtils.isDigitsOnly(binding.edtPhoneNumber.text) -> {
                            binding.edtPhoneNumber.error = "Please Enter Valid Phone Number"
                        }
                    }
                }
                else if (!edtEmail.text.isNullOrEmpty()
                    && Utilities.isValidEmailNew(edtEmail.text.toString().trim())
                    && rlEmail.visibility==View.VISIBLE) {
                    Utilities.savePrefrence(
                        requireContext(), AppConstants.ID,
                        binding.edtEmail.text.toString()
                    )
                    Utilities.saveBool(
                        requireContext(), AppConstants.RESEND_OTP,
                        false
                    )
                    Utilities.savePrefrence(
                        requireContext(),AppConstants.EMAIL_ID_REG_,
                        binding.edtEmail.text.toString()
                    )
                    bundle.putString("reqOTP","sign_up_flow")
                    reqOtp(edtEmail.text.toString().trim(),bundle)
                }
                else if(!edtEmail.text.isNullOrEmpty()
                    && rlEmail.visibility==View.VISIBLE
                    && !Utilities.isValidEmailNew(edtEmail.text.toString().trim())){
                    Toast.makeText(requireContext(), "Please enter Valid email !!",
                        Toast.LENGTH_SHORT)
                        .show()
                }else if(edtEmail.text.isNullOrEmpty() && rlEmail.visibility==View.VISIBLE){
                    Toast.makeText(requireContext(), "Please enter email !!",
                        Toast.LENGTH_SHORT)
                        .show()
                }
                else{
                    Toast.makeText(requireContext(), "Please enter Mobile Number !!",Toast.LENGTH_SHORT)
                        .show()
                }

            }
            btGoogle.setOnClickListener {
                Utilities.showProgressDialog(requireContext())
                mGoogleSignInClient!!.signOut()
                signIn()
            }
            tvCreateYour.setOnClickListener {
                startDialogue()
//                Navigation.findNavController(binding.root)
//                    .navigate(R.id.action_fragment_mobile_otp_to_choose_your_plan_fragment)
            }

            rbAsBuniness.setOnClickListener {
                llButtons.visibility=View.GONE
                btSentOtp.enable(true)
                rbAsBuniness.isChecked = true
                rbAsStandard.isChecked = false
                rbAsBuniness.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_light_dark))
                rbAsStandard.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvCountryCode.resetToDefaultCountry()
                tvPhoneNumber.visibility = View.GONE
                edtPhoneNumber.setText("")
                rlPhone.visibility = View.GONE
                rlEmail.visibility = View.VISIBLE
                tvEmail.visibility = View.VISIBLE
                tvValiate.visibility = View.GONE

            }
            rbAsStandard.setOnClickListener {
                if(!rbAsStandard.isChecked){
                    tvCountryCode.resetToDefaultCountry()
                    btSentOtp.enable(true)
                }
                llButtons.visibility=View.VISIBLE
                btSentOtp.enable(true)
                rbAsBuniness.isChecked = false
                rbAsStandard.isChecked = true
                rbAsStandard.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_light_dark))
                rbAsBuniness.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvPhoneNumber.visibility = View.VISIBLE
                rlPhone.visibility = View.VISIBLE
                edtEmail.setText("")
                rlEmail.visibility = View.GONE
                tvEmail.visibility = View.GONE
                tvValiate.visibility = View.VISIBLE
                btEmail.text=getString(R.string.email_signup)
            }

            binding.tvCountryCode.setOnCountryChangeListener { selectedCountry ->

                val drawable = binding.rlPhone.background as GradientDrawable
                if(selectedCountry.phoneCode!="91") {
                    btSentOtp.enable(false)
                    tvNotAvailable.visibility=View.VISIBLE
                    ivCountryWarning.visibility=View.VISIBLE
                    view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.not_available))
                    drawable.setStroke(4, ContextCompat.getColor(requireContext(), R.color.not_available))

                    Toast.makeText(
                        context,
                        "Please Signup with Email, Mobile Otp service not working outside India",
                        Toast.LENGTH_SHORT
                    ).show()


                }else{
                    tvNotAvailable.visibility=View.GONE
                    ivCountryWarning.visibility=View.GONE
                    drawable.setStroke(4, ContextCompat.getColor(requireContext(), R.color.line_view))
                    view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.line_view))
                    btSentOtp.enable(true)
                }
            }

            btEmail.setOnClickListener {
                if(btEmail.text!=getString(R.string.email_signup)) {
                    btEmail.text=getString(R.string.email_signup)
                    tvPhoneNumber.visibility = View.VISIBLE
                    rlPhone.visibility = View.VISIBLE
                    edtPhoneNumber.setText("")
                    btSentOtp.enable(true)
                    binding.tvCountryCode.resetToDefaultCountry()
                    rlEmail.visibility = View.GONE
                    tvEmail.visibility = View.GONE
                    tvValiate.visibility = View.VISIBLE
                }else{
                    btEmail.text=getString(R.string.mobile_signup)
                    tvPhoneNumber.visibility = View.GONE
                    edtPhoneNumber.setText("")
                    btSentOtp.enable(true)
                    binding.tvCountryCode.resetToDefaultCountry()
                    rlPhone.visibility = View.GONE
                    rlEmail.visibility = View.VISIBLE
                    tvEmail.visibility = View.VISIBLE
                    tvValiate.visibility = View.GONE
                }
            }

//            btEmail.setOnClickListener {
//                findNavController().navigate(R.id.action_fragment_mobile_otp_to_email_with_otp)
//            }

            tvSignIn.setOnClickListener {
                Utilities.saveBool(requireContext(),AppConstants.NEW_LOGIN_API,true)
                findNavController().navigate(R.id.action_fragment_sign_up_fragment_to_login_fragment)
            }


        }
    }

    private fun reqOtp(id: String, bundle: Bundle) {
        binding.btSentOtp.isClickable = false
        val properties = HashMap<String, Any?>()
            .apply {
                this["email/phone"] = id
            }

        requireContext().captureEvent(Events.OTP_LOGIN_INTIATED, properties)
        Utilities.showProgressDialog(requireContext())
        viewModel.reqOtp("sign_up_flow")
        observeReqOtp(bundle)
    }

    private fun observeReqOtp(bundle: Bundle) {
        val properties = HashMap<String, Any?>()
        viewModel.reqOtpResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    if( it.value.newUser !=null&&it.value.newUser==1) {
                        properties.apply {
                            this["email/phone"] = binding.edtPhoneNumber.text
                        }
                        requireContext().captureEvent(Events.OTP_LOGIN_SUCCEED, properties)
                        requireContext().captureIdentity(it.value.userId, properties)
                        binding.btSentOtp.isClickable = true
                        Toast.makeText(requireContext(), "OTP sent!", Toast.LENGTH_SHORT).show()

                        if (TextUtils.isDigitsOnly(binding.edtPhoneNumber.text))
                            Utilities.savePrefrence(
                                requireContext(),
                                AppConstants.PHONE_NUMBER, binding.edtPhoneNumber.text.toString()
                            )
                        else
                            Utilities.savePrefrence(
                                requireContext(),
                                AppConstants.EMAIL_ID, binding.edtEmail.text.toString()
                            )


                        if(binding.rbAsBuniness.isChecked){
                            bundle.putString("reqOTP","enterprise_signup")
                        }

                        // send user to enter otp fragment
                        findNavController().navigate(
                            R.id.action_fragment_mobile_otp_to_verify_otp,
                            bundle
                        )
                        viewModel.reqOtpSuccess.value = true
                    }else{
                        if(binding.edtPhoneNumber.text.isNullOrEmpty())
                            Toast.makeText(requireContext(), "Your Email is already registered!! Please Enter Otp", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(requireContext(), "Your Mobile Number is already registered!! Please Enter Otp", Toast.LENGTH_SHORT).show()

                        bundle.putString("reqOTP","login_flow")
                        findNavController().navigate(
                            R.id.action_fragment_mobile_otp_to_verify_otp,
                            bundle
                        )

                    }
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                    requireContext().captureEvent(Events.OTP_LOGIN_FAILED, properties)
                    binding.btSentOtp.isClickable = true
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
        }
    }

    private fun startDialogueMoveToLogin() {
        Toast.makeText(requireContext(), "Your Number is already registered !!", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_fragment_sign_up_fragment_to_login_fragment)
    }

    private fun signupWithGoogle() {
        val properties = java.util.HashMap<String, Any?>()
        properties.apply {
            this["email"] = personEmail
            this["name"] = personName
        }

        val singupData: MutableMap<String, String> = java.util.HashMap()
        singupData["api_key"] = WhiteLabelConstants.API_KEY
        singupData["user_name"] = personName
        singupData["password"] = ""
        singupData["strategy"] = "GOOGLE"
        singupData["country"] = ""
        singupData["source"] = "Android_app"
        singupData["coupon_code"] = ""
        singupData["email_id"] = personEmail
        viewModel.signUp(singupData)
        viewModel.signupResponse.observe(viewLifecycleOwner) {
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
//                    Toast.makeText(
//                        requireContext(),
//                        "Signup successful",
//                        Toast.LENGTH_SHORT
//                    ).show()

                    Utilities.savePrefrence(
                        requireContext(), AppConstants.AUTH_KEY,
                        it.value.auth_token
                    )

                    Utilities.savePrefrence(
                        requireContext(), AppConstants.ENTERPRISE_ID,
                        it.value.enterpriseId
                    )

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
                        requireContext(),
                        AppConstants.USER_ID,
                        it.value.userId
                    )

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_NAME,
                        it.value.userName
                    )

                    requireContext().gotoHomeSignUp()
                    Utilities.hideProgressDialog()
                    requireActivity().finishAffinity()

                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                    Utilities.hideProgressDialog()
                    requireContext().captureFailureEvent(
                        Events.SIGNUP_FAILED,
                        properties,
                        it.errorMessage.toString() + it.errorCode
                    )
                }
                else -> {

                }

            }
        }
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
            Utilities.hideProgressDialog()
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            Toast.makeText(
                requireContext(),
                "Google SignIn failed: " + e.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()
            //updateUI(null)
        }
    }

    private fun startDialogue() {
        val dialogLoader = Dialog(requireActivity(), R.style.DialogTheme)
        dialogLoader.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogLoader.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogLoader.setCancelable(true)
        val inflater =
            context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bind: FragmentChooseYourPlanBinding =
            FragmentChooseYourPlanBinding.inflate(inflater)
        dialogLoader.setContentView(bind.root)
        startDialogue(bind, dialogLoader)
        dialogLoader.show()
    }

    private fun startDialogue(binding: FragmentChooseYourPlanBinding, dialogLoader: Dialog) {
        binding.ivClose.setOnClickListener {
            dialogLoader.cancel()
        }
    }


    override fun onResume() {
        if(binding.rbAsBuniness.isChecked) {
            binding.apply {
                llButtons.visibility = View.GONE
                btSentOtp.enable(true)
                rbAsBuniness.isChecked = true
                rbAsBuniness.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_light_dark))
                rbAsStandard.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                rbAsStandard.isChecked = false
                tvCountryCode.resetToDefaultCountry()
                tvPhoneNumber.visibility = View.GONE
                edtPhoneNumber.setText("")
                rlPhone.visibility = View.GONE
                rlEmail.visibility = View.VISIBLE
                tvEmail.visibility = View.VISIBLE
                tvValiate.visibility = View.GONE
            }
        }else{
            if(!binding.rbAsStandard.isChecked){
                tvCountryCode.resetToDefaultCountry()
                btSentOtp.enable(true)
            }
            binding.apply {
                llButtons.visibility=View.VISIBLE
                btSentOtp.enable(true)
                rbAsBuniness.isChecked = false
                rbAsStandard.isChecked = true
                rbAsStandard.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_light_dark))
                rbAsBuniness.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvPhoneNumber.visibility = View.VISIBLE
                rlPhone.visibility = View.VISIBLE
                edtEmail.setText("")
                rlEmail.visibility = View.GONE
                tvEmail.visibility = View.GONE
                tvValiate.visibility = View.VISIBLE
                btEmail.text=getString(R.string.email_signup)
            }
        }
        super.onResume()
    }




}