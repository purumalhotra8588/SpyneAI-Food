package com.spyneai.dashboard.ui

import android.app.ActionBar
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.credits.CreditUtils
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.WalletDashboardFragmentBinding
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.credit.AvailableCreditResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WalletDashboardFragment : BaseFragment<DashboardViewModel, WalletDashboardFragmentBinding>()  {

    private var call: Call<AvailableCreditResponse>? = null
    private var availableCredits = 0
    private var TAG = "WalletDashboardFragment"




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        Log.d(TAG, "onViewCreated: "+ Utilities.getPreference(requireContext(),AppConstants.ENTERPRISE_ID))

        binding.flAddCredits.visibility = View.GONE
        binding.tvLine.visibility = View.GONE


        if (Utilities.getPreference(requireContext(), AppConstants.USER_EMAIL).toString() != ""){
            binding.tvUserEmail.visibility = View.VISIBLE
            binding.viewUser.visibility = View.VISIBLE
            if(Utilities.getPreference(requireContext(), AppConstants.USER_NAME)!="null") {
                binding.tvUserName.text = Utilities.getPreference(
                    requireContext(),
                    AppConstants.USER_NAME
                )
                binding.tvUserName.visibility = View.VISIBLE
            }else
                binding.tvUserName.visibility = View.GONE

            binding.tvUserEmail.text = Utilities.getPreference(requireContext(), AppConstants.USER_EMAIL)

            if (Utilities.getPreference(requireContext(), AppConstants.USER_NAME).toString().trim().equals("default")){
                binding.tvUserName.visibility = View.GONE
                binding.tvUserName.paddingTop

                val params: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)
                params.setMargins(10, 20, 10, 10)
                binding.tvUserEmail.setLayoutParams(params)

            }
        }



        fetchUserCreditDetails()
    }


    private fun fetchUserCreditDetails(){

        binding.shimmer.startShimmer()

        val request = RetrofitClients.buildService(APiService::class.java)
        call = request.availableCredits()

        call?.enqueue(object : Callback<AvailableCreditResponse> {
            override fun onResponse(
                call: Call<AvailableCreditResponse>,
                response: Response<AvailableCreditResponse>
            ) {
                Utilities.hideProgressDialog()
                binding.shimmer.stopShimmer()

                if (response.isSuccessful) {
                    requireContext().captureEvent(Events.FETCH_CREDITS, HashMap<String,Any?>())

                    binding.shimmer.visibility = View.GONE
                    binding.tvCredits.visibility = View.VISIBLE

                    availableCredits = response.body()?.data?.available_credits!!

                    if (response.body()?.data?.available_credits.toString() == "0"){
                        binding.tvCredits.setTextColor(ContextCompat.getColor(requireContext(),R.color.zero_credits))
                        binding.tvCredits.text = "00 "
                    }else{
                        binding.tvCredits.setTextColor(ContextCompat.getColor(requireContext(),R.color.available_credits))
                        binding.tvCredits.text = CreditUtils.getFormattedNumber(response.body()!!.data.available_credits)
                    }

//                    Utilities.savePrefrence(
//                        requireContext(),
//                        AppConstants.CREDIT_ALLOTED,
//                        response.body()?.data?.credit_allotted.toString()
//                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.CREDIT_AVAILABLE,
                        response.body()?.data?.available_credits.toString()
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.CREDIT_USED,
                        response.body()?.data?.total_credits_spent.toString()
                    )


                } else {
                    requireContext().captureFailureEvent(Events.FETCH_CREDITS_FAILED, HashMap<String,Any?>(),
                        "Server not responding"
                    )
                    Toast.makeText(
                        requireContext(),
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun onFailure(call: Call<AvailableCreditResponse>, t: Throwable) {
                if (!call.isCanceled){
                    Toast.makeText(
                        requireContext(),
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()

        if(call!= null && call!!.isExecuted) {
            call!!.cancel()
        }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = WalletDashboardFragmentBinding.inflate(inflater, container, false)

    override fun getViewModel() = DashboardViewModel::class.java


}