package com.spyneai.onboarding.ui

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureIdentity
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentEnterOtpBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboarding.data.viewmodels.OnBoardingViewModel
import com.spyneai.onboardingv2.data.MySMSBroadcastReceiver
import com.spyneai.onboardingv2.ui.intro.ChooseCategoryActivity
import com.spyneai.onboardingv2.ui.onboarding.ReferralFragment
import com.spyneai.posthog.Events


class EnterOtpFragment :
    com.spyneai.base.BaseFragment<OnBoardingViewModel, FragmentEnterOtpBinding>() {
    private var mySMSBroadcastReceiver: MySMSBroadcastReceiver? = null
    private var timer: CountDownTimer? = null
    var otp_entered = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mySMSBroadcastReceiver = MySMSBroadcastReceiver()
        requireActivity().registerReceiver(
            mySMSBroadcastReceiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("sent_to")?.let {
            binding.tvPhone.text = it
        }

        startTimer()
        textListeners()
    }

    private fun startTimer() {
        binding.tvResend.isClickable = false
        binding.tvTimer.isVisible = true

        timer = object : CountDownTimer(31000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var countDownTime =
                    if (millisUntilFinished / 1000 < 10)
                        "00 : 0" + millisUntilFinished / 1000 + ""
                    else
                        "00 : " + (millisUntilFinished / 1000).toString() + ""


                binding.tvTimer.text = "(in $countDownTime Sec)"
            }

            override fun onFinish() {
                binding?.let {
                    it.tvTimer.isVisible = false
                    it.tvResend.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.primary
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
            etOne!!.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.length == 1) {
                        ettwo!!.requestFocus()
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

            ettwo!!.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.length == 1) {
                        etThree!!.requestFocus()
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
                    if (ettwo!!.text.toString().length == 0) {
                        etOne!!.requestFocus()
                    }

                    /*if(ettwo.getText().toString() != null)
                        otp_entered += ettwo.getText().toString().charAt(0);*/
                }
            })

            etThree!!.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.length == 1) {
                        etFour!!.requestFocus()
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
                    if (etThree!!.text.toString().length == 0) {
                        ettwo!!.requestFocus()
                    }

                    /*if(etThree.getText().toString() != null)
                        otp_entered += etThree.getText().toString().charAt(0);*/
                }
            })

            etFour!!.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.length == 1) {
                        etFive!!.requestFocus()
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
                    if (etFour!!.text.toString().length == 0) {
                        etThree!!.requestFocus()
                    }
                }
            })

            etFive!!.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.length == 1) {
                        etSix!!.requestFocus()
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
                    if (etFive!!.text.toString().length == 0) {
                        etFour!!.requestFocus()
                    }
                    /*if(etFive.getText().toString() != null)
                        otp_entered += etFive.getText().toString().charAt(0);*/
                }
            })

            etSix!!.addTextChangedListener(object : TextWatcher {
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
                    //tvError.visibility = View.INVISIBLE
                    /*if(etSix.getText().toString() != null)
                        otp_entered += etSix.getText().toString().charAt(0);*/
                    if (etOne!!.text.toString().length == 1 && ettwo!!.text.toString().length == 1 && etThree!!.text.toString().length == 1 && etFour!!.text.toString().length == 1 && etFive!!.text.toString().length == 1 && etSix!!.text.toString().length == 1) {
                        otp_entered =
                            (etOne!!.text.toString() + ettwo!!.text.toString() + etThree!!.text.toString()
                                    + etFour!!.text.toString() + etFive!!.text.toString() + etSix!!.text.toString())
                        Log.d("TAG_OTP", "otp_entered$otp_entered")
                        postOtp(otp_entered)
                    }
                    if (etSix!!.text.toString().length == 0) {
                        etFive!!.requestFocus()
                    }
                }
            })

            //on pressing the back key in otp edittext
            ettwo!!.setOnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //this is for backspace
                    if (ettwo!!.length() == 1) {
                        ettwo!!.setText("")
                    } else {
                        etOne!!.setText("")
                        etOne!!.requestFocus()
                    }
                }
                false
            }

            etThree!!.setOnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //this is for backspace
                    if (etThree!!.length() == 1) {
                        etThree!!.setText("")
                    } else {
                        ettwo!!.setText("")
                        ettwo!!.requestFocus()
                    }
                }
                false
            }

            etFour!!.setOnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //this is for backspace
                    if (etFour!!.length() == 1) {
                        etFour!!.setText("")
                    } else {
                        etThree!!.setText("")
                        etThree!!.requestFocus()
                    }
                }
                false
            }

            etFive!!.setOnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //this is for backspace
                    if (etFive!!.length() == 1) {
                        etFive!!.setText("")
                    } else {
                        etFour!!.setText("")
                        etFour!!.requestFocus()
                    }
                }
                false
            }

            etSix!!.setOnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //this is for backspace
                    if (etSix!!.length() == 1) {
                        etSix!!.setText("")
                    } else {
                        etFive!!.setText("")
                        etFive!!.requestFocus()
                    }
                }
                false
            }

            binding.btnSubmit!!.setOnClickListener(View.OnClickListener {
                if (otp_entered.length == 6) {
                    postOtp(
                        etOne.text.toString()+
                                ettwo.text.toString()+
                                etThree.text.toString()+
                                etFour.text.toString()+
                                etFive.text.toString()+
                                etSix.text.toString()
                    )
                } else {
                    Toast.makeText(requireContext(), "Please enter valid OTP!", Toast.LENGTH_LONG)
                        .show()
                }
            })

            binding.tvResend!!.setOnClickListener {
                startTimer()
                viewModel.reqOtp()
                observeReqOtp()
            }
        }
    }

    private fun postOtp(otpEntered: String) {

        requireContext().captureEvent(Events.OTP_VERIFICATION_INITIATED, HashMap())
        Utilities.showProgressDialog(requireContext())

        viewModel.verifyOtp(otpEntered)

        viewModel.verifyOtpResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

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

                    val otpResponse = it.value
                    Toast.makeText(requireContext(), otpResponse.message, Toast.LENGTH_SHORT).show()

                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.AUTH_KEY,
                        otpResponse.authToken
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


//                    val intent = Intent(requireContext(), MainDashboardActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//
//                    if (otpResponse.message == "OTP validated") {
//                        intent.putExtra(AppConstants.IS_NEW_USER, true)
//                        intent.putExtra(AppConstants.CREDITS_MESSAGE, otpResponse.displayMessage)
//                    }
//
//                    startActivity(intent)
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { postOtp(otpEntered) }
                }
                else -> {

                }
            }
        }
    }

    private fun observeReqOtp() {
        viewModel.reqOtpResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        it.value?.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    it.value.userId?.let {
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.TOKEN_ID, it
                        )

                        requireContext().captureEvent(Events.OTP_RESENT, HashMap())
                    }

                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { viewModel.reqOtp() }
                }
                else -> {

                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        mySMSBroadcastReceiver?.let {
            requireActivity().unregisterReceiver(it)
        }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEnterOtpBinding.inflate(inflater, container, false)

    override fun getViewModel() = OnBoardingViewModel::class.java
}