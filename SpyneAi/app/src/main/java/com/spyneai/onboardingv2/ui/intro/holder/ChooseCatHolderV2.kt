package com.spyneai.onboardingv2.ui.intro.holder

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemCatgoryWebviewBinding
import com.spyneai.onboardingv2.data.SelectCategoryRes




class ChooseCatHolderV2(
    itemView: View,
    listener: OnItemClickListener?,
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<SelectCategoryRes.Data>{

    var listener: OnItemClickListener? = null
    var binding: ItemCatgoryWebviewBinding? = null

    val TAG = "OverlaysHolder"

    init {
        binding = ItemCatgoryWebviewBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: SelectCategoryRes.Data) {

        binding?.let {
            val background = it.clMain.background as GradientDrawable

            Glide.with(itemView)
                .load(data.thumbnail)
                .into(it.ivThumbnail)

            if (data.isSelected){
                it.clMain.background = background
                it.tvTitle.text = data.categoryName
                it.tvTitle.setTextColor(Color.BLACK)

                Glide.with(itemView)
                    .load(R.drawable.bg_channel_new)
                    .into(it.ivOval)
            }else {
                background.cornerRadius = 20f
                background.setStroke(5, Color.parseColor(data.borderColor))
                it.clMain.background = background

                Glide.with(itemView)
                    .load(R.drawable.bg_channel)
                    .into(it.ivOval)

                it.tvTitle.text = data.categoryName
                it.tvTitle.setTextColor(Color.parseColor(data.borderColor))
            }

            it.clMain.setOnClickListener { view ->
                listener?.onItemClick(view,adapterPosition,data)
            }

        }


    }
}