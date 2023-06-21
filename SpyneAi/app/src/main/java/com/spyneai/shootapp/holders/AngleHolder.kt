package com.spyneai.shootapp.holders

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.databinding.ItemAngleBinding
import com.spyneai.databinding.ItemShootConfigSubcategoryBinding
import com.spyneai.needs.AppConstants
import com.spyneai.shootapp.data.model.Frame

class AngleHolder(
    itemView: View,
    listener: OnItemClickListener?
) : RecyclerView.ViewHolder(itemView),
    GenericAdapter.Binder<Frame> {

    var listener: OnItemClickListener? = null
    var binding: ItemAngleBinding? = null

    init {
        binding = ItemAngleBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: Frame) {


        binding?.let {
            it.tvValue.text = data.value.toString()

            it.tvValue.background = if (data.isSelected) ContextCompat.getDrawable(
                itemView.context,
                R.drawable.bg_angle_selected
            ) else ContextCompat.getDrawable(itemView.context, R.drawable.bg_angle_unselected)

            it.tvValue.setTextColor(
                if (data.isSelected) ContextCompat.getColor(
                    itemView.context,
                    R.color.white
                ) else ContextCompat.getColor(itemView.context, R.color.categories_text)
            )

            it.tvValue.setOnClickListener {
                listener?.onItemClick(
                    it,
                    adapterPosition,
                    data
                )
            }
        }
    }
}