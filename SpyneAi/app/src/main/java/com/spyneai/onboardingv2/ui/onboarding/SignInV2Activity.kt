package com.spyneai.onboardingv2.ui.onboarding

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.spyneai.databinding.ActivitySignInV2Binding

class SignInV2Activity : AppCompatActivity() {
    private val TAG = SignInV2Activity::class.simpleName

    private lateinit var gso: GoogleSignInOptions
    lateinit var binding: ActivitySignInV2Binding

    private val RC_SIGN_IN = 100
    var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInV2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

//        gso = GoogleSignInOptions
//            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken("887824920844-hk3d5e6e0pfn644o685cer3d5vjno4td.apps.googleusercontent.com")
//            .requestEmail()
//            .build()
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso!!)


        binding.btnSignInWithPhone.setOnClickListener {
            Intent(this, LoginWithPhoneActivity::class.java)
                .apply { startActivity(this) }
        }

        binding.flEmail.setOnClickListener {
            Intent(this, LoginInWithEmailActivity::class.java)
                .apply { startActivity(this) }
        }

        binding.tvSignup.setOnClickListener {
            Intent(this, SignUpV2Activity::class.java)
                .apply { startActivity(this) }
        }

        binding.flGoogle.setOnClickListener {
            val acct = GoogleSignIn.getLastSignedInAccount(this)
            if (acct != null) {
                Log.d(TAG, "onCreate: ${acct.email}")
                Log.d(TAG, "onCreate: ${acct.displayName}")
            } else {
                signIn()
//                val signInIntent = mGoogleSignInClient?.signInIntent
//                googleStartForResult.launch(signInIntent)
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

            val s = ""

            // Signed in successfully, show authenticated UI.
            //updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            //updateUI(null)
        }
    }
    val googleStartForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val s = ""
            if (result.resultCode == Activity.RESULT_OK) {
                handleLoginResponse(result.data)
            }
        }

    fun handleLoginResponse(data: Any?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data as Intent)
        try {
            val acct: GoogleSignInAccount = task.getResult(ApiException::class.java)
            Log.d(TAG, "onCreate: ${acct.email}")
            Log.d(TAG, "onCreate: ${acct.displayName}")
            Log.d(TAG, "handleLoginResponse ok$")
        } catch (e: Exception) {
            Log.e(TAG, "handleLoginResponse error: ${e.message}")
        }
    }
}