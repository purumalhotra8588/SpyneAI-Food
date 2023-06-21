package com.spyneai.registration.view.ui.fragment.signin

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
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
import com.spyneai.databinding.LoginFragmentBinding
import com.spyneai.getUuid
import com.spyneai.gotoHomeSignUp
import com.spyneai.loginsignup.data.LoginBodyV2
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import com.spyneai.posthog.captureIdentity
import com.spyneai.registration.viewmodels.RegistrationViewModel
import com.spyneai.shootapp.utils.objectToString


class LoginFragment : Fragment() {
    lateinit var binding: LoginFragmentBinding
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var personName = ""
    private var personGivenName = ""
    private var personFamilyName = ""
    private var personEmail = ""
    private var personId = ""
    private var personPhoto = ""
    private val rcSignIn = 100
    var showPassword = false
    var usePassword = true
    private val viewModel: RegistrationViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginFragmentBinding.inflate(inflater, container, false)

        binding.tvCountryCode.registerPhoneNumberTextView(binding.edtPhoneNumber)
        val drawable = binding.rlPhone.background as GradientDrawable

        val uuid = getUuid()
        Utilities.savePrefrence(requireContext(),AppConstants.LOGIN_UUID, uuid)

        Utilities.savePrefrence(
            requireContext(), AppConstants.MOBILE_NUMBER,
            ""
        )
        Utilities.savePrefrence(
            requireContext(), AppConstants.EMAIL_ID_REG_,
            ""
        )

        Utilities.saveBool(
            requireContext(), AppConstants.NEW_LOGIN_API,
            true
        )

        binding.edtPhoneNumber.setText("")
        binding.edtEmail.setText("")


        if(!showPassword) {
            binding.edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.edtPassword.typeface =
                ResourcesCompat.getFont(requireContext(), R.font.poppins_medium)
        }else {
            binding.edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_CLASS_TEXT
            binding.edtPassword.typeface =
                ResourcesCompat.getFont(requireContext(), R.font.poppins_medium)
        }

        if(!usePassword) {
            binding.tvUsePassword.text = "Use Password?"
            binding.btSentOtp.text = "Send OTP"
            binding.rlPassword.visibility=View.GONE
        }else {
            binding.tvUsePassword.text = "Use OTP?"
            binding.btSentOtp.text = "Login"
            binding.rlPassword.visibility=View.GONE
        }

        if(binding.tvCountryCode.selectedCountryCode!="91") {
            binding.tvNotAvailable.visibility = View.VISIBLE
            drawable.setStroke(4, ContextCompat.getColor(requireContext(), R.color.not_available))
            binding.view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.not_available))
        }
        else {
            binding.tvNotAvailable.visibility = View.GONE
            drawable.setStroke(4, ContextCompat.getColor(requireContext(), R.color.line_view))
            binding.view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.line_view))
        }

        initView()



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginWithEmail()
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
                                Utilities.savePrefrence(
                                    requireContext(), AppConstants.MOBILE_NUMBER,
                                    binding.edtPhoneNumber.text.toString()
                                )
                                Utilities.saveBool(
                                    requireContext(), AppConstants.RESEND_OTP,
                                    false
                                )

                                bundle.putString("reqOTP","login_flow")
                                reqOtp()

                                val properties = HashMap<String,Any?>().apply {
                                    put("Phone Number", binding.edtPhoneNumber.text)
                                }
                                requireContext().captureEvent(
                                    Events.LOGIN_WITH_PHONE_NUMBER_CLICKED,
                                    properties
                                )

                            }
                        }
                        !TextUtils.isDigitsOnly(binding.edtPhoneNumber.text) -> {
                            binding.edtPhoneNumber.error = "Please Enter Valid Phone Number"
                        }
                    }
                }
                else if (!edtEmail.text.isNullOrEmpty()
                    && Utilities.isValidEmailNew(edtEmail.text.toString().trim())
                    && rlEmail.visibility==View.VISIBLE
                ) {
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
                    bundle.putString("reqOTP","login_flow")
                    reqOtp()

                    val properties = HashMap<String,Any?>().apply {
                        put("Email", binding.edtPhoneNumber.text)
                    }
                    requireContext().captureEvent(
                        Events.LOGIN_WITH_EMAIL_CLICKED,
                        properties
                    )

                } else if(!edtEmail.text.isNullOrEmpty()
                    && Utilities.isValidEmailNew(edtEmail.text.toString().trim())
                    && rlEmail.visibility==View.VISIBLE
                    && rlPassword.visibility==View.VISIBLE)
                {
                    if(edtPassword.text.isNullOrEmpty()){
                        Toast.makeText(requireContext(), "Please enter Password !!",
                            Toast.LENGTH_SHORT)
                            .show()
                    }else{
                        loginWithPassword()
                    }
                } else if(!edtEmail.text.isNullOrEmpty()
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

            tvCountryCode.setOnCountryChangeListener { selectedCountry ->

                val drawable = binding.rlPhone.background as GradientDrawable
                if(selectedCountry.phoneCode!="91") {
                    btSentOtp.enable(false)
                    tvNotAvailable.visibility=View.VISIBLE
                    view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.not_available))
                    drawable.setStroke(4, ContextCompat.getColor(requireContext(), R.color.not_available))

                    Toast.makeText(
                        context,
                        "Please Signup with Email, Mobile Otp service not working outside India",
                        Toast.LENGTH_SHORT
                    ).show()


                }else{
                    tvNotAvailable.visibility=View.GONE
                    drawable.setStroke(4, ContextCompat.getColor(requireContext(), R.color.line_view))
                    view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.line_view))
                    btSentOtp.enable(true)
                }
            }

            btGoogle.setOnClickListener {
                Utilities.showProgressDialog(requireContext())
                mGoogleSignInClient!!.signOut()
                signIn()

                val properties = HashMap<String,Any?>().apply {
                }
                requireContext().captureEvent(
                    Events.LOGIN_WITH_GOOGLE_CLICKED,
                    properties
                )

            }
            tvCreateYour.setOnClickListener {
                startDialogue()
            }

            rbAsStandard.setOnClickListener {
                rbAsStandard.isChecked = true
                rbAsIndividual.isChecked = false
                tvPhoneNumber.visibility = View.GONE
                rlPhone.visibility = View.GONE
                rlEmail.visibility = View.VISIBLE
                tvEmail.visibility = View.VISIBLE
            }

            rbAsIndividual.setOnClickListener {
                rbAsStandard.isChecked = false
                rbAsIndividual.isChecked = true
                tvPhoneNumber.visibility = View.VISIBLE
                rlPhone.visibility = View.VISIBLE
                rlEmail.visibility = View.GONE
                tvEmail.visibility = View.GONE

            }

            btEmail.setOnClickListener {
                if(btEmail.text!=getString(R.string.email_login)) {
                    Utilities.savePrefrence(
                        requireContext(), AppConstants.MOBILE_NUMBER,
                        ""
                    )
                    Utilities.savePrefrence(
                        requireContext(), AppConstants.EMAIL_ID_REG_,
                        ""
                    )
                    btEmail.text=getString(R.string.email_login)
                    tvPhoneNumber.visibility = View.VISIBLE
                    rlPhone.visibility = View.VISIBLE
                    rlPassword.visibility = View.GONE
                    edtPhoneNumber.setText("")
                    edtEmail.setText("")
                    tvUsePassword.text = "Use Password?"
                    btSentOtp.enable(true)
                    btSentOtp.text = "Send OTP"
                    tvUsePassword.visibility=View.GONE
                    tvCountryCode.resetToDefaultCountry()
                    rlEmail.visibility = View.GONE
                    tvEmail.visibility = View.GONE
                    tvValiate.visibility = View.GONE

                }else{
                    loginWithEmail()
                }
            }

//            btEmail.setOnClickListener {
//                findNavController().navigate(R.id.action_fragment_mobile_otp_to_email_with_otp)
//            }

            tvSignIn.setOnClickListener {
                Utilities.saveBool(requireContext(),AppConstants.NEW_LOGIN_API,false)
                findNavController().navigate(R.id.action_fragment_login_phone_number_fragment)
            }
            tvForgetPwd.setOnClickListener {
                findNavController().navigate(R.id.action_fragment_login_forget_pwd)
            }

            ivShowPassword.setOnClickListener {
                if(!showPassword){
                    edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_CLASS_TEXT
                    showPassword=true
                    ivShowPassword.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_light_dark))
                    edtPassword.setText(edtPassword.text.toString())
                    binding.edtPassword.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.poppins_medium)
                }else{
                    edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    showPassword=false
                    ivShowPassword.setColorFilter(ContextCompat.getColor(requireContext(), R.color.eye_grey))
                    edtPassword.setText(edtPassword.text.toString())
                    binding.edtPassword.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.poppins_medium)
                }
            }

            tvUsePassword.setOnClickListener{
                if(!usePassword){
                    tvUsePassword.text = "Use OTP?"
                    btSentOtp.text = "Login"
                    rlPassword.visibility=View.VISIBLE
                    usePassword = true
                }else{
                    tvUsePassword.text = "Use Password?"
                    btSentOtp.text = "Send OTP"
                    rlPassword.visibility=View.GONE
                    usePassword = false

                }
            }
        }
    }

    private fun loginWithEmail() {
        Utilities.savePrefrence(
            requireContext(), AppConstants.MOBILE_NUMBER,
            ""
        )
        Utilities.savePrefrence(
            requireContext(), AppConstants.EMAIL_ID_REG_,
            ""
        )
        binding.apply {
            btEmail.text=getString(R.string.mobile_login)
            tvPhoneNumber.visibility = View.GONE
            edtPhoneNumber.setText("")
            edtPhoneNumber.setText("")
            btSentOtp.enable(true)
            tvCountryCode.resetToDefaultCountry()
            rlPhone.visibility = View.GONE
            rlEmail.visibility = View.VISIBLE
            rlPassword.visibility = View.GONE
            btSentOtp.text = "Send OTP"
            tvUsePassword.text = "Use OTP?"
            tvUsePassword.visibility=View.GONE
            tvEmail.visibility = View.VISIBLE
            tvValiate.visibility = View.GONE
        }

    }

    private fun reqOtp() {
        binding.btSentOtp.isClickable = false
        val properties = HashMap<String, Any?>()
            .apply {
                this["email/phone"] = id
            }
        requireContext().captureEvent(Events.OTP_LOGIN_INTIATED, properties)
        Utilities.showProgressDialog(requireContext())

        if(!Utilities.getPreference(requireContext(),AppConstants.EMAIL_ID_REG_)
                .toString().isNullOrEmpty()){
            Utilities.savePrefrence(requireContext(),AppConstants.RESOURCE_TYPE,"emailId")
        }else if(!Utilities.getPreference(requireContext(),AppConstants.MOBILE_NUMBER)
                .toString().isNullOrEmpty()){
            Utilities.savePrefrence(requireContext(),AppConstants.RESOURCE_TYPE,"contactNumber")
        }else if(!binding.edtEmail.text.toString().isNullOrEmpty()){
            Utilities.savePrefrence(requireContext(),AppConstants.RESOURCE_TYPE,"emailId")
        }


        if(Utilities.getBool(requireContext(),AppConstants.NEW_LOGIN_API,false)){
            viewModel.loginBody = LoginBodyV2(
                strategy = "generateOTP",
                apiKey = WhiteLabelConstants.API_KEY,
                emailId = Utilities.getPreference(requireContext(),AppConstants.EMAIL_ID_REG_)
                    .toString(),
                contactNumber = Utilities.getPreference(requireContext(),AppConstants.MOBILE_NUMBER).toString(),
                resourceType =  Utilities.getPreference(requireContext(),AppConstants.RESOURCE_TYPE)
                    .toString(),
                deviceId = Utilities.getPreference(requireContext(),AppConstants.LOGIN_UUID).toString()
            )
            viewModel.reqOtpV2(viewModel.loginBody!!)
            observeReqOtpV2()
        }else {
            //old api to generate otp
            viewModel.reqOtp("login_flow")
            observeReqOtp()
        }

    }

    private fun observeReqOtpV2() {
        val bundle = Bundle()
        bundle.putString("reqOTP", "login_flow")
        val properties = HashMap<String, Any?>()
        viewModel.reqOtpResponseV2.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    properties.apply {
                        this["email/phone"] = binding.edtEmail.text
                    }
                    requireContext().captureEvent(Events.OTP_LOGIN_SUCCEED, properties)
//                        requireContext().captureIdentity(it.value.data.userId, properties)

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


                    // send user to enter otp fragment
                    try {
                        findNavController().navigate(
                            R.id.action_fragment_login_mobile_otp_to_verify_otp,
                            bundle
                        )
                        viewModel.reqOtpSuccess.value = true
                    }catch (e : Exception){
                    }


                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()

                    if(it.errorCode == 404)
                        it.errorMessage = "User Not Registered, Please Signup!!"

                    handleApiError(it) {reqOtp()}
                    properties["error"] = it.objectToString()
                    requireContext().captureEvent(Events.OTP_LOGIN_FAILED, properties)
                    binding.btSentOtp.isClickable = true
                }
                else -> {}
            }
        }
    }

    private fun loginWithPassword(){

        if(!Utilities.getPreference(requireContext(),AppConstants.EMAIL_ID_REG_)
                .toString().isNullOrEmpty()){
            Utilities.savePrefrence(requireContext(),AppConstants.RESOURCE_TYPE,"emailId")
        }else if(!Utilities.getPreference(requireContext(),AppConstants.MOBILE_NUMBER)
                .toString().isNullOrEmpty()){
            Utilities.savePrefrence(requireContext(),AppConstants.RESOURCE_TYPE,"contactNumber")
        }else if(!binding.edtEmail.text.toString().isNullOrEmpty()){
            Utilities.savePrefrence(requireContext(),AppConstants.RESOURCE_TYPE,"emailId")
        }

        viewModel.loginBody = LoginBodyV2(
            strategy = "password",
            apiKey = WhiteLabelConstants.API_KEY,
            emailId = binding.edtEmail.text.toString(),
            password = binding.edtPassword.text.toString(),
            resourceType = Utilities.getPreference(requireContext(),AppConstants.RESOURCE_TYPE).toString(),
            deviceId = Utilities.getPreference(requireContext(),AppConstants.LOGIN_UUID).toString()
        )
        viewModel.reqOtpV2(viewModel.loginBody!!)
        observeLoginWithPassword()
    }

    private fun observeLoginWithPassword() {
        val bundle = Bundle()
        Utilities.showProgressDialog(requireContext())
        viewModel.reqOtpResponseV2.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    val response = it.value.data

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.AUTH_KEY,
                        response.base64Token
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_ID,
                        response.userId
                    )
                    Utilities.saveBool(requireContext(), AppConstants.CHECK_QC, response.checkQc)
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.ENTERPRISE_DISCOUNT,
                        response.discount
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_NAME,
                        response.userName
                    )

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_ROLE,
                        response.userRole
                    )

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_ENTERPRISE,
                        response.enterpriseId
                    )

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.ENTERPRISE_NAME,
                        response.enterpriseName
                    )

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.TEAM_NAME,
                        response.teamName
                    )

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.TEAM_ID,
                        response.teamId
                    )


                    Utilities.saveBool(
                        requireContext(),
                        AppConstants.NEW_ENTERPRISE_USER,
                        response.isNewUser
                    )

                    val properties = HashMap<String, Any?>()
                    properties.apply {
                        this["user_id"] = response.userId
                        this["name"] = response.userName
                        this["email"] = response.emailId
                    }

                    requireContext().captureIdentity(response.userId, properties)
                    requireContext().captureEvent(Events.LOGIN_WITH_PASSWORD_SUCCESSED, properties)

                    // Navigation Flow
                    Handler(Looper.getMainLooper()).postDelayed({
                        Navigation.findNavController(
                            binding.root
                        ).navigate(
                            if(Utilities.getBool(requireContext(),AppConstants.NEW_ENTERPRISE_USER,false))
//                                R.id.action_fragment_login_with_password_to_main_V2_activity
                                R.id.action_fragment_login_with_password_to_main_dashboard_activity
                            else
                                R.id.action_fragment_login_with_password_to_main_dashboard_activity
                            , bundle)
                        requireActivity().finish()

                    }, 100)


                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    Toast.makeText(
                        requireContext(),
                        "Please enter a valid Email or Password!!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                else -> {
                }
            }
        }
    }


    private fun observeReqOtp() {
        val bundle = Bundle()
        bundle.putString("reqOTP", "login_flow")
        val properties = HashMap<String, Any?>()
        viewModel.reqOtpResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    if (it.value.newUser == 0) {
                        properties.apply {
                            this["email/phone"] = binding.edtEmail.text
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

                        try {
                            // send user to enter otp fragment
                            findNavController().navigate(
                                R.id.action_fragment_login_mobile_otp_to_verify_otp,
                                bundle
                            )
                            viewModel.reqOtpSuccess.value = true
                        }catch (e: Exception){
                        }
                    } else {
                        findNavController().navigate(
                            R.id.action_fragment_login_phone_number_fragment,
                            bundle
                        )
                        Toast.makeText(
                            requireContext(),
                            "Your number is not registered, Please Signup!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    properties["error"] = it.objectToString()
                    handleApiError(it) {reqOtp()}
                    requireContext().captureEvent(Events.OTP_LOGIN_FAILED, properties)
                    binding.btSentOtp.isClickable = true
                    Toast.makeText(
                        requireContext(),
                        "OTP sent failed: " + it.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }


    private fun signupWithGoogle() {
        val properties = java.util.HashMap<String, Any?>()
        properties.apply {
            this["email"] = personEmail
            this["name"] = personName
        }

        val signUpData: MutableMap<String, String> = java.util.HashMap()
        signUpData["api_key"] = WhiteLabelConstants.API_KEY
        signUpData["user_name"] = personName
        signUpData["password"] = ""
        signUpData["strategy"] = "GOOGLE"
        signUpData["country"] = ""
        signUpData["source"] = "Android_app"
        signUpData["coupon_code"] = ""
        signUpData["email_id"] = personEmail

        viewModel.signUp(signUpData)

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

//                    Utilities.hideProgressDialog()
//                    Toast.makeText(
//                        requireContext(),
//                        "Signup successful",
//                        Toast.LENGTH_SHORT
//                    ).show()

                    Utilities.savePrefrence(
                        requireContext(), AppConstants.AUTH_KEY,
                        it.value.auth_token
                    )

                    Utilities.saveBool(
                        requireContext(), AppConstants.NEW_ENTERPRISE_USER,
                        false
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

                else -> {}
            }
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, rcSignIn)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == rcSignIn) {
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
                "Google signIn failed: " + e.localizedMessage,
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
        super.onResume()

        Utilities.savePrefrence(
            requireContext(), AppConstants.MOBILE_NUMBER,
            ""
        )
        Utilities.savePrefrence(
            requireContext(), AppConstants.EMAIL_ID_REG_,
            ""
        )

        Utilities.saveBool(
            requireContext(), AppConstants.NEW_LOGIN_API,
            true
        )

        binding.edtPhoneNumber.setText("")
        binding.edtEmail.setText("")

    }


}