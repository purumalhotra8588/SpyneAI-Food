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
import com.spyneai.databinding.ItemNoPlateBinding

class NumberPlateHolderApp(
    itemView: View,
    listener: OnItemClickListener?
) : RecyclerView.ViewHolder(itemView),
    GenericAdapter.Binder<CatAgnosticResV2.CategoryAgnos.NoPlate> {

    var listener: OnItemClickListener? = null
    var binding: ItemNoPlateBinding? = null

    init {
        binding = ItemNoPlateBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: CatAgnosticResV2.CategoryAgnos.NoPlate) {


        binding?.let {
            Glide.with(itemView.context)
                .load(data.number_plate_logo_url)
                .placeholder(R.mipmap.defaults)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(it.ivNoPlateImage)


            it.ivNoPlateImage.background =
                if (data.isSelected)
                    ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.bg_background
                    )
                else
                    null

            it.ivNoPlateImage.setOnClickListener { view ->
                listener?.onItemClick(
                    view,
                    adapterPosition,
                    data
                )
            }
        }
    }
}