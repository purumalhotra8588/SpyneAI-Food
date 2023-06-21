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
import com.spyneai.databinding.ItemBackgroundNewBinding
import com.spyneai.shootapp.data.model.CarsBackgroundRes

class BackgroundHolder(
    itemView: View,
    listener: OnItemClickListener?
) : RecyclerView.ViewHolder(itemView),
    GenericAdapter.Binder<CarsBackgroundRes.BackgroundApp> {

    var listener: OnItemClickListener? = null
    var binding: ItemBackgroundNewBinding? = null

    init {
        binding = ItemBackgroundNewBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: CarsBackgroundRes.BackgroundApp) {


        binding?.let {
            Glide.with(itemView.context)
                .load(data.imageUrl)
                .placeholder(R.mipmap.defaults)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(it.ivImage)


            it.ivImage.background =
                if (data.isSelected)
                    ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.bg_background
                    )
                else
                    null

            it.ivImage.setOnClickListener { view ->
                listener?.onItemClick(
                    view,
                    adapterPosition,
                    data
                )
            }
        }
    }
}