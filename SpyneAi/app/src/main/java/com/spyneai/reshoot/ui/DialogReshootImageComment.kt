package com.spyneai.reshoot.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentDialogReshootImageCommentBinding
import com.spyneai.shootapp.data.ShootViewModelApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DialogReshootImageComment : BaseDialogFragment<ShootViewModelApp, FragmentDialogReshootImageCommentBinding>(){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var position  = arguments?.getInt("position",0)
        var total = arguments?.getInt("total",0)

        if (position != null) {
            position = position.plus(1)
        }

        if(total==0){
            binding.tvImagesCount.visibility=View.GONE
            binding.tvImagesCount.visibility=View.GONE
        }

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.tvImagesCount.text = "Images $position/$total"

        binding.tvShootAngle?.text = "Shoot Angle: ${viewModel.desiredAngle}"

        GlobalScope.launch(Dispatchers.IO) {
            viewModel.skuApp?.skuId?.let { id ->
                val image = viewModel.getImagesById(id,viewModel.overlayId.toString())

                GlobalScope.launch(Dispatchers.Main) {
                    Glide.with(requireContext())
                        .load(image.input_image_lres_url)
                        .into(binding.ivBefore)

                    Glide.with(requireContext())
                        .load(image.output_image_lres_url)
                        .into(binding.ivAfter)

                    image.reshootComment?.let { comment ->
                        val item = comment.replace("#","\n").substring(0)
                        val comments = item.substring(0,item.length-0).split(',')

                        var listOfComments = ""

                        comments.forEachIndexed { index, string ->
                            listOfComments += string+"\n"


                            if(listOfComments.isNullOrEmpty()){
                                binding.tvCommentTitle.visibility=View.GONE
                                binding.tvComment.visibility=View.GONE
                            }else {
                                binding.tvComment.text = listOfComments
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDialogReshootImageCommentBinding.inflate(inflater, container, false)
}