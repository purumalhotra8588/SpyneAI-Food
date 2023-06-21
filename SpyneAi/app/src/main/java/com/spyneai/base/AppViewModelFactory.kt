package com.spyneai.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.onboarding.data.viewmodels.OnBoardingViewModel
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.processedimages.data.ProcessedViewModelApp
import com.spyneai.shootapp.data.ProcessViewModelApp
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.singleimageprocessing.data.SingleImageViewModel
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class AppViewModelFactory(): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when{
            modelClass.isAssignableFrom(SingleImageViewModel::class.java) -> SingleImageViewModel() as T
            modelClass.isAssignableFrom(ShootViewModelApp::class.java) -> ShootViewModelApp() as T
            modelClass.isAssignableFrom(ProcessViewModelApp::class.java) -> ProcessViewModelApp() as T
            modelClass.isAssignableFrom(MyOrdersViewModel::class.java) -> MyOrdersViewModel() as T
            modelClass.isAssignableFrom(ProcessedViewModelApp::class.java) -> ProcessedViewModelApp() as T
            modelClass.isAssignableFrom(ThreeSixtyViewModel::class.java) -> ThreeSixtyViewModel() as T
            modelClass.isAssignableFrom(DraftViewModel::class.java) -> DraftViewModel() as T
            modelClass.isAssignableFrom(OnBoardingViewModel::class.java) -> OnBoardingViewModel() as T
            else ->
                throw IllegalArgumentException("ViewModelClass not found")
        }
    }

}