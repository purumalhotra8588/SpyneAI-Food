package com.spyneai.dashboard.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VIEW_MODEL : BaseViewModel, VIEW_BINDING : ViewBinding> :
    Fragment() {
    open protected lateinit var binding: VIEW_BINDING
    open protected lateinit var viewModel: VIEW_MODEL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        if (getLayoutId() == -1) {
//            return super.onCreateView(inflater, container, savedInstanceState)
//        }

        binding = getFragmentBinding(inflater, container)
        return binding.root
    }

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) : VIEW_BINDING

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }


    open fun initViewModel() {
        viewModel = createViewModel()
        viewModel.initIntentArgs(intent = null, bundle = arguments)
    }

    abstract fun getViewModelClass(): Class<out VIEW_MODEL>
    open fun createViewModel(): VIEW_MODEL = ViewModelProvider(this)[getViewModelClass()]

}