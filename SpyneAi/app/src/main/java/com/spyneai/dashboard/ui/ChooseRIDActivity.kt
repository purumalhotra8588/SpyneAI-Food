package com.spyneai.dashboard.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.data.model.LayoutHolder
import com.spyneai.dashboard.data.model.RidResponse
import com.spyneai.dashboard.ui.adapters.RidAdapter
import com.spyneai.base.AppViewModelFactory
import com.spyneai.databinding.ActivityChooseRidBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class ChooseRIDActivity : AppCompatActivity() {

    lateinit var btnlistener: RidAdapter.BtnClickListener
    lateinit var ridAdapter: RidAdapter

    private lateinit var binding: ActivityChooseRidBinding
    private var viewModel: DashboardViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseRidBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btContinue.isEnabled=false

        viewModel = ViewModelProvider(this, AppViewModelFactory()).get(DashboardViewModel::class.java)

        getRIDS()

        binding.btContinue.setOnClickListener {

        }

    }


    private fun getRIDS() {

        Utilities.showProgressDialog(this)

        viewModel?.getRestaurantList(
            Utilities.getPreference(this, AppConstants.AUTH_KEY).toString()
        )

        viewModel?.ridResponse?.observe(this) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    if (!it.value.data.isNullOrEmpty()) {
                        ridAdapter = RidAdapter(this,
                            it.value.data as ArrayList<RidResponse.Data>,
                            object : RidAdapter.BtnClickListener {
                                override fun onBtnClick(position: Int) {
                                    binding.btContinue.isEnabled=true

                                    LayoutHolder.categoryPosition = position

                                    val selectedElement = it.value.data.firstOrNull {
                                        it.isSelected
                                    }

                                    selectedElement?.isSelected = false

                                    it.value.data[position].isSelected=true
                                    ridAdapter?.notifyDataSetChanged()


                                    try {
                                        Utilities.savePrefrence(
                                            this@ChooseRIDActivity,
                                            AppConstants.ENTITY_ID,
                                            it.value.data[position].entity_id
                                        )

                                        Utilities.savePrefrence(
                                            this@ChooseRIDActivity,
                                            AppConstants.ENTITY_NAME,
                                            it.value.data[position].entity_name
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                }

                            })

                        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
                            this,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        binding.rvRIDS.layoutManager = layoutManager
                        binding.rvRIDS.adapter = ridAdapter

                    }
                }
                is Resource.Loading -> {

                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getRIDS() }
                }
            }
        }
    }

    override fun onBackPressed() {
        return

    }

}