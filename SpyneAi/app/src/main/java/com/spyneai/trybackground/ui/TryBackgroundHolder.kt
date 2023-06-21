package com.spyneai.trybackground.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemTryBackgroundBinding
import com.spyneai.shootapp.data.model.CarsBackgroundRes

class TryBackgroundHolder(
    itemView: View,
    listener: OnItemClickListener?,
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<CarsBackgroundRes.BackgroundApp> {

    var listener: OnItemClickListener? = null
    var binding: ItemTryBackgroundBinding? = null

    val TAG = "OverlaysHolder"

    init {
        binding = ItemTryBackgroundBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: CarsBackgroundRes.BackgroundApp) {

        binding?.let {
            val background = it.cvMain.background as GradientDrawable
            background.cornerRadius = 30f

            if (data.isSelected) {
                background.setStroke(5, ContextCompat.getColor(itemView.context, R.color.primary))
                it.cvMain.background = background

            } else {
                background.setStroke(5, Color.WHITE)
                it.cvMain.background = background
            }

            Glide.with(itemView)
                .load(data.imageUrl)
                .into(it.ivThumbnail)

            try {
                it.tvCarBgName.text = data.bgName
            } catch (e : Exception){

            }

            it.cvMain.setOnClickListener { view ->
                listener?.onItemClick(view, adapterPosition, data)
            }
        }
    }
}