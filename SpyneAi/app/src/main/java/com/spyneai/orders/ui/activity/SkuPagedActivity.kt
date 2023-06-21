package com.spyneai.orders.ui.activity

import android.os.Bundle
import androidx.paging.ExperimentalPagingApi
import com.spyneai.R
import com.spyneai.base.BaseActivity
import com.spyneai.databinding.ActivitySkuPagedBinding
import com.spyneai.orders.ui.fragment.SkuPagedFragment
import com.spyneai.showConnectionChangeView

class SkuPagedActivity : BaseActivity() {

    private lateinit var binding: ActivitySkuPagedBinding

    @OptIn(ExperimentalPagingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkuPagedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, SkuPagedFragment())
            .commit()

    }


    override fun onConnectionChange(isConnected: Boolean) {
        showConnectionChangeView(isConnected,binding.root)
    }
}