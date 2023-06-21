package com.spyneai.registration.view.ui.fragment.forgetpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spyneai.databinding.FragmentResetBlankBinding


class ResetPasswordFragment : Fragment() {

    lateinit var binding : FragmentResetBlankBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentResetBlankBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        clickListener()
    }

    private fun clickListener() {
        binding.apply {
            btSentOtp.setOnClickListener {

            }
        }
    }




}