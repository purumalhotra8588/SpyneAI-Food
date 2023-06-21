package com.spyneai.onboardingv2.ui.intro.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemSampleImageBinding
import com.spyneai.onboardingv2.data.SampleImagesRes

class SampleImageHolder(
    itemView: View,
    listener: OnItemClickListener?,
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<SampleImagesRes.Data.SampleImage>{

    var listener: OnItemClickListener? = null
    var binding: ItemSampleImageBinding? = null

    val TAG = "OverlaysHolder"

    init {
        binding = ItemSampleImageBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: SampleImagesRes.Data.SampleImage) {

        binding?.let {
            Glide.with(itemView)
                .load(data.inputImage)
                .into(it.iv)

            it.iv.setOnClickListener { view ->
                listener?.onItemClick(view,adapterPosition,data)
            }
        }
    }
}