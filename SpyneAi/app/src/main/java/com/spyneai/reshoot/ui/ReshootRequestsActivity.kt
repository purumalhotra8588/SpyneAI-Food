package com.spyneai.reshoot.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.ExperimentalPagingApi
import com.spyneai.R
import com.spyneai.databinding.ActivityReshootRequestsBinding

class ReshootRequestsActivity : AppCompatActivity() {

    lateinit var binding: ActivityReshootRequestsBinding

    @OptIn(ExperimentalPagingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReshootRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            super.onBackPressed()
        }

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, ReshootRequestFragment())
            .commit()
    }
}