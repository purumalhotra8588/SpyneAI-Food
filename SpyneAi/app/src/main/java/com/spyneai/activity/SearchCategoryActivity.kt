package com.spyneai.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spyneai.R
import com.spyneai.databinding.ActivityMyOrdersBinding
import com.spyneai.databinding.ActivitySearchCategoryBinding
import com.spyneai.onboardingv2.ui.intro.SearchCategoryFragment
import com.spyneai.orders.ui.fragment.MyOrdersFragment

class SearchCategoryActivity : AppCompatActivity() {

    lateinit var binding: ActivitySearchCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, SearchCategoryFragment())
            .commit()

    }
}