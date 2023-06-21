package com.spyneai.reshoot

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemAppSelectImageBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shootapp.repository.model.image.Image


class SelectImageHolder(
    itemView: View,
    listener: OnItemClickListener?)
    : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<Image>{

    var listener: OnItemClickListener? = null
    var binding : ItemAppSelectImageBinding? = null
    val TAG = "ReshootHolder"

    init {
        binding = ItemAppSelectImageBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: Image) {

        if(Utilities.getBool(BaseApplication.getContext(),AppConstants.CHECK_QC,false)) {
            binding?.flCb?.isClickable = false
            data.qcStatus.let {
                when (it) {
                    "approved" -> {
                        binding?.clRoot?.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.qc_green))
                        binding?.cb?.isChecked=false
                        binding?.flCb?.visibility=View.INVISIBLE
                    }
                    "in_progress" -> {
                        binding?.clRoot?.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.qc_yellow))
                        binding?.cb?.isChecked=false
                        binding?.flCb?.visibility=View.INVISIBLE
                    }
                    "rejected" -> {
                        binding?.clRoot?.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.qc_red))
                        binding?.cb?.isChecked=false
                        binding?.flCb?.visibility=View.INVISIBLE
                    }
                    "reshoot" -> {
                        data.isSelected=true
                        binding?.clRoot?.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.qc_orange))
                        binding?.flCb?.visibility=View.VISIBLE
                        binding?.flCb?.isClickable=false
                    }
                    else -> {
                        binding?.flCb?.visibility=View.INVISIBLE
                        binding?.clRoot?.setBackgroundColor(Color.parseColor("#1A878787"))
                    }
                }
            }

        }
        var color = Integer.toHexString(
            ContextCompat.getColor(
                itemView.context,
                R.color.primary
            ) and 0x00ffffff
        )

        if (color.length == 5)
            color = "0"+color

        if (data.isSelected)
            binding?.clRoot?.setBackgroundColor(Color.parseColor("#CDCCCC"))
        else
            binding?.clRoot?.setBackgroundColor(Color.parseColor("#1A878787"))

        binding?.cb?.isChecked= data.isSelected

        Glide.with(itemView)
            .load(data.input_image_lres_url)
            .into(binding?.ivBefore!!)

        Glide.with(itemView)
            .load(data.output_image_lres_url)
            .into(binding?.ivAfter!!)

        binding?.clRoot?.setOnClickListener {
            listener?.onItemClick(
                it,
                adapterPosition,
                data
            )
        }

    }
}