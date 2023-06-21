package com.spyneai.dashboard.ui.base

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {
    open fun initIntentArgs(intent: Intent? = null, bundle: Bundle? = null) {
    }
}