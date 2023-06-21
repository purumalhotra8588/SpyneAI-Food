package com.spyneai.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VIEW_MODEL : ViewModel, VIEW_BINDING : ViewBinding> : Fragment() {

    protected lateinit var binding: VIEW_BINDING
    protected lateinit var viewModel: VIEW_MODEL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getFragmentBinding(inflater, container)
        val factory = AppViewModelFactory()
        viewModel = ViewModelProvider(requireActivity(), factory).get(getViewModel())

        return binding.root

    }

    abstract fun getViewModel() : Class<VIEW_MODEL>
    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) : VIEW_BINDING


}