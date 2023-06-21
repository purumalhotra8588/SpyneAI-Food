package com.spyneai.orders.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.ExperimentalPagingApi
import com.spyneai.R
import com.spyneai.base.BaseActivity
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.ui.HomeV2Fragment
import com.spyneai.databinding.ActivityMyInvitationBinding
import com.spyneai.orders.ui.fragment.JoinDetailsFragment
import com.spyneai.orders.ui.fragment.JoinInvitationFragment
import com.spyneai.registration.view.ui.activity.JoinOtpFragment
import com.spyneai.showConnectionChangeView


class JoinInvitation : BaseActivity() {

    lateinit var binding: ActivityMyInvitationBinding
    private var viewModel: DashboardViewModel? = null

    @OptIn(ExperimentalPagingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyInvitationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, JoinInvitationFragment())
            .commit()

        viewModel?.replaceJoinDetails?.observe(this,) {
            if (it) {

                val fragment: Fragment = JoinDetailsFragment()

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                    )
                    .replace(binding.flContainer.id, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        viewModel?.replaceJoinOtp?.observe(this,) {
            if (it) {

                val fragmentnew: Fragment = JoinOtpFragment()

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                    )
                    .replace(binding.flContainer.id, fragmentnew)
                    .addToBackStack(null)
                    .commit()
            }
        }

        viewModel?.replaceJoinOtpPost?.observe(this,) {
            if (it) {

                val fragmentbacktohome: Fragment = HomeV2Fragment()

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                    )
                    .replace(binding.flContainer.id, fragmentbacktohome)
                    .addToBackStack(null)
                    .commit()
            }
        }


    }


    override fun onConnectionChange(isConnected: Boolean) {
        showConnectionChangeView(isConnected, binding.root)
    }



}


