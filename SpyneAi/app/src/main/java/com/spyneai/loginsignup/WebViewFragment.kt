package com.spyneai.onboardingv2.ui.intro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.navigation.fragment.findNavController
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.carinspectionocr.viewmodel.RegistrationDataViewModel
import com.spyneai.databinding.FragmentWebViewBinding
import com.spyneai.needs.AppConstants
import com.spyneai.registration.view.ui.activity.RegistrationBaseActivity


class WebViewFragment : BaseFragment<RegistrationDataViewModel, FragmentWebViewBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.webView.visibility = View.GONE // Show the loader
        val url = AppConstants.SIGNUP_URL
        val webSettings = binding.webView.settings
        webSettings.domStorageEnabled = true
        webSettings.javaScriptEnabled = true
        webSettings.allowContentAccess = true

        binding.progressBar.visibility = View.VISIBLE // Show the loader

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE // Hide the loader
                binding.webView.visibility = View.VISIBLE
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                // post a delayed message to the main thread's message queue
                Handler().postDelayed({
                    if (url?.contains("https://console.spyne.ai/register/success")!!) {
                        // Close the web view
                        binding.webView.visibility = View.GONE
                        requireContext().startActivity(Intent(requireContext(), RegistrationBaseActivity::class.java))
                        requireActivity().finishAffinity()
                    }
                }, 3000) // 3 seconds delay
                super.doUpdateVisitedHistory(view, url, isReload)
            }
        }

        binding.webView.loadUrl(url)
    }




    override fun getViewModel() = RegistrationDataViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWebViewBinding.inflate(inflater, container, false)

}