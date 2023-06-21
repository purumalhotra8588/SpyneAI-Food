package com.spyneai.shootapp.ui

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import com.google.gson.Gson
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import com.spyneai.shootapp.data.ShootViewModelApp

class ImproveShootFragment : com.spyneai.carinspection.base.BaseComposeFragment<ShootViewModelApp>() {
    @Composable
    override fun CreateContent() = ImproveShootScreen(viewModel)

    override fun getViewModelClass() = ShootViewModelApp::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireContext().captureEvent(
            "Improve Shoot Api Called",
            HashMap<String, Any?>().apply {
                put("sku_id", viewModel.skuApp?.skuId)
            }
        )

        viewModel.improveShoot()
        observeImproveShoot()
    }

    fun observeImproveShoot(){
        viewModel.improveShootResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {

                    requireContext().captureEvent(
                        "Improve Shoot Api Sucess",
                        HashMap<String, Any?>().apply {
                            put("sku_id", viewModel.skuApp?.skuId)
                            put("data", Gson().toJson(it))
                        },
                    )

                    if (!it.value.data.guidelines.isNullOrEmpty())
                    viewModel._improveShootList.postValue(it.value.data.guidelines)
                }

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        "Improve Shoot Api Failed",
                        HashMap<String, Any?>().apply {
                            put("sku_id", viewModel.skuApp?.skuId)
                            put("data", Gson().toJson(it))
                            put("message", it.errorMessage)
                            put("throwable", it.throwable)
                            put("code", it.errorCode)
                        },
                        it.errorMessage.toString()
                    )

                        handleApiError(it) { viewModel.improveShoot() }
                }
                else -> {

                }
            }
        }
    }
}