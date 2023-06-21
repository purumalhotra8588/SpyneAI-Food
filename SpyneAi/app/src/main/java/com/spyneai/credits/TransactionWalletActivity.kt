package com.spyneai.credits

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.paging.ExperimentalPagingApi
import com.spyneai.R
import com.spyneai.base.BaseActivity
import com.spyneai.credits.fragments.TransactionWalletFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.ActivityTransactionWalletBinding
import com.spyneai.setLocale
@ExperimentalPagingApi
class TransactionWalletActivity : BaseActivity() {
    lateinit var binding: ActivityTransactionWalletBinding
    lateinit var viewModel: DashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLocale()

        viewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, TransactionWalletFragment())
            .commit()
    }

    override fun onConnectionChange(isConnected: Boolean) {
        TODO("Not yet implemented")
    }
}