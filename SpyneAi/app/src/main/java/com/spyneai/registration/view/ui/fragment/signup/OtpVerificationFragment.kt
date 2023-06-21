package com.spyneai.registration.view.ui.fragment.signup

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.autoreadotp.SmsBroadcastReceiver
import com.spyneai.autoreadotp.SmsBroadcastReceiver.SmsBroadcastReceiverListener
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentMobileOtpVarificationBinding
import com.spyneai.loginsignup.data.LoginBodyV2
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.ui.intro.ChooseCategoryActivity
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureIdentity
import com.spyneai.registration.viewmodels.RegistrationViewModel
import kotlinx.android.synthetic.main.fragment_mobile_otp_varification.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class OtpVerificationFragment : Fragment() {

    lateinit var binding: FragmentMobileOtpVarificationBinding
    private var timer: CountDownTimer? = null
    var otpEntered = ""
    var otpResend = false
    private val viewModel: RegistrationViewModel by viewModels()
    private var loginType: String? = ""
    private val REQ_USER_CONSENT = 200
    private lateinit var smsBroadcastReceiver: SmsBroadcastReceiver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMobileOtpVarificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.arguments?.apply {
            loginType = this.getString("reqOTP", "")
            Log.d("LOGINTYPE", "onViewCreated: $loginType")
        }

        startSmsUserConsent()
        initView()
    }

    private fun initView() {
        startTimer()
        textListeners()
        binding.tvResend.isClickable = false
    }

    private fun startTimer() {
        binding.tvResend.isClickable = false
        binding.tvTimer.isVisible = true
        tvResend.setTextColor(
            ContextCompat.getColor(
                BaseApplication.getContext(), R.color.otp_expire_color
            )
        )

        timer = object : CountDownTimer(31000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val countDownTime =
                    if (millisUntilFinished / 1000 < 10)
                        "00 : 0" + millisUntilFinished / 1000 + ""
                    else
                        "00 : " + (millisUntilFinished / 1000).toString() + ""

                binding.tvTimer.text = "(in $countDownTime Sec)"
            }

            override fun onFinish() {
                binding.let {
                    it.tvTimer.isVisible = false
                    it.tvResend.setTextColor(
                        ContextCompat.getColor(
                            BaseApplication.getContext(),
                            com.spyneai.R.color.primary_light_dark
                        )
                    )
                    it.tvResend.isClickable = true
                }
            }
        }
        timer?.start()
    }

    private fun textListeners() {
        binding.apply {
            //changing focus to next edit text in otp
            etOne.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.length == 1) {
                        ettwo.requestFocus()
                        //otp_entered += etOne.getText().toString().charAt(0);
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int,
                    count: Int
                ) {
                    /*if(etOne.getText().toString() != null)
                            otp_entered += etOne.getText().toString().charAt(0);*/
                }
            })

            ettwo.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.length == 1) {
                        etThree.requestFocus()
                        //otp_entered += ettwo.getText().toString().charAt(0);
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int,
                    count: Int
                ) {
                    if (ettwo.text.toString().isEmpty()) {
                        etOne.requestFocus()
                    }

                    /*if(ettwo.getText().toString() != null)
                        otp_entered += ettwo.getText().toString().charAt(0);*/
                }
            })

            etThree.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.length == 1) {
                        etFour.requestFocus()
                        //otp_entered += etThree.getText().toString().charAt(0);
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int,
                    count: Int
                ) {
                    if (etThree.text.toString().isEmpty()) {
                        ettwo.requestFocus()
                    }

                    /*if(etThree.getText().toString() != null)
                        otp_entered += etThree.getText().toString().charAt(0);*/
                }
            })

            etFour.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.length == 1) {
                        etFive.requestFocus()
                        //otp_entered += etFour.getText().toString().charAt(0);
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int,
                    count: Int
                ) {
                    if (etFour.text.toString().isEmpty()) {
                        etThree.requestFocus()
                    }
                }
            })

            etFive.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.length == 1) {
                        etSix.requestFocus()
                        //otp_entered += etFive.getText().toString().charAt(0);
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int,
                    count: Int
                ) {
                    if (etFive.text.toString().isEmpty()) {
                        etFour!!.requestFocus()
                    }
                    /*if(etFive.getText().toString() != null)
                        otp_entered += etFive.getText().toString().charAt(0);*/
                }
            })

            etSix.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.length == 1) {
                        //otp_entered += etSix.getText().toString().charAt(0);
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int,
                    count: Int
                ) {
                    if (etOne.text.toString().length == 1 && ettwo.text.toString().length == 1 && etThree.text.toString().length == 1 && etFour.text.toString().length == 1 && etFive.text.toString().length == 1 && etSix.text.toString().length == 1) {
                        otpEntered =
                            (etOne.text.toString() + ettwo.text.toString() + etThree.text.toString()
                                    + etFour.text.toString() + etFive.text.toString() + etSix.text.toString())
                        Log.d("TAG_OTP", "otp_entered$otpEntered")

                        if (otpEntered.length == 6) {
                            postOtp(
                                etOne.text.toString() +
                                        ettwo.text.toString() +
                                        etThree.text.toString() +
                                        etFour.text.toString() +
                                        etFive.text.toString() +
                                        etSix.text.toString()
                            )
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Please enter valid OTP!",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }

                    }
                    if (etSix.text.toString().isEmpty()) {
                        etFive.requestFocus()
                    }
                }
            })

            //on pressing the back key in otp edittext
            ettwo.setOnKeyListener { _, keyCode, _ -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //this is for backspace
                    if (ettwo.length() == 1) {
                        ettwo.setText("")
                    } else {
                        etOne.setText("")
                        etOne.requestFocus()
                    }
                }
                false
            }

            etThree.setOnKeyListener { _, keyCode, _ -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //this is for backspace
                    if (etThree.length() == 1) {
                        etThree.setText("")
                    } else {
                        ettwo.setText("")
                        ettwo.requestFocus()
                    }
                }
                false
            }

            etFour.setOnKeyListener { _, keyCode, _ -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //this is for backspace
                    if (etFour.length() == 1) {
                        etFour.setText("")
                    } else {
                        etThree.setText("")
                        etThree.requestFocus()
                    }
                }
                false
            }

            etFive.setOnKeyListener { _, keyCode, _ -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //this is for backspace
                    if (etFive.length() == 1) {
                        etFive.setText("")
                    } else {
                        etFour.setText("")
                        etFour.requestFocus()
                    }
                }
                false
            }

            etSix.setOnKeyListener { _, keyCode, _ -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //this is for backspace
                    if (etSix.length() == 1) {
                        etSix.setText("")
                    } else {
                        etFive.setText("")
                        etFive.requestFocus()
                    }
                }
                false
            }

            binding.btnSubmit.setOnClickListener {
                if (otpEntered.length == 6) {
                    postOtp(
                        etOne.text.toString() +
                                ettwo.text.toString() +
                                etThree.text.toString() +
                                etFour.text.toString() +
                                etFive.text.toString() +
                                etSix.text.toString()
                    )
                } else {
                    Toast.makeText(requireContext(), "Please enter valid OTP!", Toast.LENGTH_LONG)
                        .show()
                }
            }

            binding.tvResend.setOnClickListener {
                otpResend = true
                binding.tvResend.isClickable = false
                binding.tvResend.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.spyneai.R.color.otp_expire_color
                    )
                )
                Utilities.saveBool(
                    requireContext(), AppConstants.RESEND_OTP,
                    true
                )

                startTimer()

                if (Utilities.getBool(requireContext(), AppConstants.NEW_LOGIN_API, false)) {
                    viewModel.loginBody = LoginBodyV2(
                        strategy = "generateOTP",
                        apiKey = WhiteLabelConstants.API_KEY,
                        emailId = Utilities.getPreference(
                            requireContext(),
                            AppConstants.EMAIL_ID_REG_
                        )
                            .toString(),
                        contactNumber = Utilities.getPreference(
                            requireContext(),
                            AppConstants.MOBILE_NUMBER
                        ).toString(),
                        resourceType = Utilities.getPreference(
                            requireContext(),
                            AppConstants.RESOURCE_TYPE
                        )
                            .toString(),
                        deviceId = Utilities.getPreference(
                            requireContext(),
                            AppConstants.LOGIN_UUID
                        ).toString()
                    )
                    viewModel.reqOtpV2(viewModel.loginBody!!)
                    observeReqOtpV2(Bundle())
                } else {
                    //old api to generate otp
                    viewModel.reqOtp("login_flow")
                    observeReqOtp(Bundle())
                }
            }
        }
    }


    private fun postOtp(otpEntered: String) {
        val bundle = Bundle()
        requireContext().captureEvent(Events.OTP_VERIFICATION_INITIATED, HashMap())
        Utilities.showProgressDialog(requireContext())

        if (Utilities.getBool(requireContext(), AppConstants.NEW_LOGIN_API, false)) {
            viewModel.loginBody = LoginBodyV2(
                strategy = "validateOTP",
                apiKey = WhiteLabelConstants.API_KEY,
                emailId = Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID_REG_)
                    .toString(),
                contactNumber = Utilities.getPreference(
                    requireContext(),
                    AppConstants.MOBILE_NUMBER
                ).toString(),
                resourceType = Utilities.getPreference(requireContext(), AppConstants.RESOURCE_TYPE)
                    .toString(),
                deviceId = Utilities.getPreference(requireContext(), AppConstants.LOGIN_UUID)
                    .toString(),
                OTP = otpEntered
            )
            viewModel.reqOtpV2(viewModel.loginBody!!)

            observeReqOtpV2(bundle)
        } else {
            //use od otp verification
            viewModel.verifyOtp(otpEntered)
            observeReqOtp(bundle)
        }
    }

    private fun observeReqOtp(bundle: Bundle) {
        viewModel.verifyOtpResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    val otpResponse = it.value
//                    Toast.makeText(requireContext(), otpResponse.message, Toast.LENGTH_SHORT).show()

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.AUTH_KEY,
                        otpResponse.authToken
                    )

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_ID,
                        it.value.userId
                    )

                    Utilities.saveBool(
                        requireContext(),
                        AppConstants.CHECK_QC,
                        false
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
                        AppConstants.USER_ENTERPRISE,
                        it.value.enterprise_id
                    )

                    val properties = HashMap<String, Any?>()
                    properties.apply {
                        this["user_id"] = otpResponse.userId
                        this["name"] = otpResponse.userName
                        this["email"] = otpResponse.emailId
                    }

                    requireContext().captureIdentity(otpResponse.userId, properties)
                    requireContext().captureEvent(Events.OTP_VERIFIED, properties)
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_ID,
                        otpResponse.userId
                    )

                    if (!otpResend){
                    // Navigation Flow
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (isAdded) {
                                if (loginType.equals("sign_up_enterprise_flow")) {
                                    bundle.putString("reqOTP", "sign_up_enterprise_flow")

                                    Navigation.findNavController(
                                        binding.root
                                    ).navigate(
                                        R.id.action_fragment_phone_otp_verify_fragment_user_info,
                                        bundle
                                    )
                                } else
                                    if (loginType.equals("sign_up_flow")) {
                                        Navigation.findNavController(
                                            binding.root
                                        ).navigate(
                                            R.id.action_fragment_phone_otp_verify_fragment_user_info,
                                            bundle
                                        )
                                    } else {
                                        chooseCategory(it.value.newWUser, bundle)
                                    }
                            }
                        }, 100)
                }
                }

                is Resource.Failure -> {
                    binding.etOne.setText("")
                    binding.ettwo.setText("")
                    binding.etThree.setText("")
                    binding.etFour.setText("")
                    binding.etFive.setText("")
                    binding.etSix.setText("")
                    Utilities.hideProgressDialog()
                    Toast.makeText(requireContext(), "Please enter a valid otp", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                }
            }
        }
    }

    private fun chooseCategory(newUser: Int, bundle: Bundle) {
        if (newUser == 1) {
            Navigation.findNavController(
                binding.root
            ).navigate(
                R.id.action_fragment_phone_otp_verify_fragment_user_info,
                bundle
            )
        } else {
            startActivity(Intent(requireContext(), ChooseCategoryActivity::class.java))
            requireActivity().finishAffinity()
        }
    }

    private fun observeReqOtpV2(bundle: Bundle) {
        viewModel.reqOtpResponseV2.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    val otpResponse = it.value.data

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.AUTH_KEY,
                        otpResponse.base64Token
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_ID,
                        it.value.data.userId
                    )
                    Utilities.saveBool(requireContext(), AppConstants.CHECK_QC, true)
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.ENTERPRISE_DISCOUNT,
                        it.value.data.discount
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_NAME,
                        it.value.data.userName
                    )

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_ENTERPRISE,
                        it.value.data.enterpriseId
                    )

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.ENTERPRISE_NAME,
                        otpResponse.enterpriseName
                    )

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.TEAM_NAME,
                        otpResponse.teamName
                    )

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.TEAM_ID,
                        otpResponse.teamId
                    )


                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.USER_ROLE,
                        otpResponse.userRole
                    )

                    Utilities.saveBool(
                        requireContext(),
                        AppConstants.NEW_ENTERPRISE_USER,
                        otpResponse.isNewUser
                    )

                    val properties = HashMap<String, Any?>()
                    properties.apply {
                        this["user_id"] = otpResponse.userId
                        this["name"] = otpResponse.userName
                        this["email"] = otpResponse.emailId
                    }





                    try {
                        requireContext().captureIdentity(otpResponse.userId, properties)
                        requireContext().captureEvent(Events.OTP_VERIFIED, properties)
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.USER_ID,
                            otpResponse.userId
                        )
                    } catch (e: Exception) {

                    }

                    if(!otpResend){
                    // Navigation Flow
                    Handler(Looper.getMainLooper()).postDelayed({

                        if (loginType.equals("sign_up_enterprise_flow")) {

                            bundle.putString("reqOTP", "sign_up_enterprise_flow")

                            Navigation.findNavController(
                                binding.root
                            ).navigate(
                                R.id.action_fragment_phone_otp_verify_fragment_user_info,
                                bundle
                            )
                        } else
                            if (loginType.equals("sign_up_flow")) {
                                Navigation.findNavController(
                                    binding.root
                                ).navigate(
                                    R.id.action_fragment_phone_otp_verify_fragment_user_info,
                                    bundle
                                )
                            } else if (loginType.equals("login_flow")) {
                                if (Utilities.getBool(
                                        requireContext(),
                                        AppConstants.NEW_ENTERPRISE_USER,
                                        false
                                    )
                                ) {
//                                    Navigation.findNavController(
//                                        binding.root
//                                    ).navigate(
//                                        R.id.action_email_with_otp_to_main_v2_activity,
//                                        bundle
//                                    )
                                    startActivity(
                                        Intent(
                                            requireContext(),
                                            ChooseCategoryActivity::class.java
                                        )
                                    )
                                    requireActivity().finish()
                                } else {
                                    startActivity(
                                        Intent(
                                            requireContext(),
                                            ChooseCategoryActivity::class.java
                                        )
                                    )
                                    requireActivity().finishAffinity()
                                }
                            } else {
                                Navigation.findNavController(
                                    binding.root
                                ).navigate(
                                    R.id.action_fragment_phone_otp_verify_fragment_user_info,
                                    bundle
                                )
                            }
                    }, 100)
                }


                }
                is Resource.Failure -> {
                    binding.etOne.setText("")
                    binding.ettwo.setText("")
                    binding.etThree.setText("")
                    binding.etFour.setText("")
                    binding.etFive.setText("")
                    binding.etSix.setText("")
                    Utilities.hideProgressDialog()
                    Toast.makeText(requireContext(), "Please enter a valid otp", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                }
            }
        }
    }

    private fun startSmsUserConsent() {
        val client = SmsRetriever.getClient(requireActivity())
        //We can add sender phone number or leave it blank
        // I'm adding null here
        client.startSmsUserConsent(null).addOnSuccessListener {
//            Toast.makeText(requireContext(), "On Success", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
//            Toast.makeText(requireContext(), "On OnFailure", Toast.LENGTH_LONG).show()
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_USER_CONSENT) {
            if (resultCode == RESULT_OK && data != null) {
                //That gives all message to us.
                // We need to get the code from inside with regex
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
//                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                getOtpFromMessage(message!!)
            }
        }
    }


    private fun getOtpFromMessage(message: String) {
        // This will match any 6 digit number in the message
        val pattern: Pattern = Pattern.compile("(|^)\\d{6}")
        val matcher: Matcher = pattern.matcher(message)
        if (matcher.find()) {

            matcher.group(0).forEachIndexed { index, char ->
                when (index) {
                    0 -> etOne.setText(char.toString())
                    1 -> ettwo.setText(char.toString())
                    2 -> etThree.setText(char.toString())
                    3 -> etFour.setText(char.toString())
                    4 -> etFive.setText(char.toString())
                    5 -> etSix.setText(char.toString())
                    else -> {
                        etOne.setText("")
                        ettwo.setText("")
                        etThree.setText("")
                        etFour.setText("")
                        etFive.setText("")
                        etSix.setText("")
                        Toast.makeText(
                            requireContext(),
                            "Please enter valid OTP!",
                            Toast.LENGTH_LONG
                        )
                            .show()

                    }
                }
            }
        }
    }


    private fun registerBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver.smsBroadcastReceiverListener = object : SmsBroadcastReceiverListener {
            override fun onSuccess(intent: Intent?) {
                startActivityForResult(intent, REQ_USER_CONSENT)
            }

            override fun onFailure() {}
        }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        requireActivity().registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    override fun onStart() {
        super.onStart()
        registerBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(smsBroadcastReceiver)
    }

}