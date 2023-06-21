package com.spyneai.registration.view.ui.activity

import InvitationEmailBody
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentAddLinkBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shootapp.data.ShootViewModelApp


class InvitationAddFragment : BaseFragment<ShootViewModelApp, FragmentAddLinkBinding>(),
    OnItemClickListener {

    var arraylist = ArrayList<String>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        listeners()

    }


    private fun sendInvitation(){
        checkIfFragmentAttached {
            Utilities.showProgressDialog(requireContext())
            val InvitationEmailBody = InvitationEmailBody(
                mailList = arraylist,
                authkey = Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())
            viewModel.sendInvitaionEmailId(InvitationEmailBody)
            observelist()
        }
    }


    fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }

    private fun observelist(){
        viewModel.inviteEmailRes.observe(viewLifecycleOwner) {
            when (it) {
                is com.spyneai.base.network.Resource.Success -> {
                    Utilities.hideProgressDialog()
                        Toast.makeText(requireContext(), " Invitation Sent ", Toast.LENGTH_LONG).show()

                }
                is com.spyneai.base.network.Resource.Failure ->{
                    Utilities.hideProgressDialog()
                    handleApiError(it) { sendInvitation() }

                }
                com.spyneai.base.network.Resource.Loading -> {

                }
            }
        }
    }

    private fun listeners() {

        binding.edtEmail.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //do here your stuff
                if (!binding.edtEmail.text.isNullOrEmpty() && Utilities.isValidEmailNew(
                        binding.edtEmail.text.toString().trim()
                    )
                ) {
                    addChipToGroup(binding.edtEmail.text.toString())
                    binding.edtEmail.setText("")


                } else {
                    binding.edtEmail.error = "Please enter Email"
                }
                true
            } else false
        })


        binding.addButton.setOnClickListener {
            if (!binding.edtEmail.text.isNullOrEmpty() && Utilities.isValidEmailNew(
                    binding.edtEmail.text.toString().trim()
                )
            ) {
                addChipToGroup(binding.edtEmail.text.toString())
                binding.edtEmail.setText("")


            } else {
                binding.edtEmail.error = "Please enter Email"
            }


        }


        binding.btSentOtp.setOnClickListener {
            if (!binding.edtEmail.text.isNullOrEmpty() && Utilities.isValidEmailNew(
                    binding.edtEmail.text.toString().trim()) || !arraylist.isNullOrEmpty()) {
                sendInvitation()
            }
            else{
                binding.edtEmail.error = "Please enter Email"
            }
            binding.edtEmail.setText("")
        }

        binding.ivBackGif.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }


    private fun addChipToGroup(person: String) {
        val chip = Chip(context)
        chip.text = person
        arraylist.add(person)
        chip.isChipIconVisible = false
        chip.isCloseIconVisible = true

        // necessary to get single selection working
        chip.isClickable = true
        chip.isCheckable = false
        binding.chipGroup.addView(chip as View)
        chip.setOnCloseIconClickListener { binding.chipGroup.removeView(chip as View)
        arraylist.remove(person)}
    }


    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )  = FragmentAddLinkBinding.inflate(inflater, container, false)

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun onItemClick(view: View, position: Int, data: Any?) {

    }
}