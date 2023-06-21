package com.spyneai.carinspection.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spyneai.base.AppViewModelFactory

abstract class BaseComposeFragment<VIEW_MODEL : ViewModel> : Fragment() {

    protected lateinit var viewModel: VIEW_MODEL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val factory = AppViewModelFactory()
        viewModel = ViewModelProvider(requireActivity(), factory).get(getViewModelClass())

        val root = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BaseComposeContent {
                    CreateContent()
                }
            }
        }
        return root
    }

    @Composable
    protected fun BaseComposeContent(content: @Composable () -> Unit) {
        MaterialTheme(typography = MaterialTheme.typography) {
            Surface(color = MaterialTheme.colors.background) {
                content()
            }
        }
    }

    @Composable
    abstract fun CreateContent()

    abstract fun getViewModelClass(): Class<VIEW_MODEL>


}