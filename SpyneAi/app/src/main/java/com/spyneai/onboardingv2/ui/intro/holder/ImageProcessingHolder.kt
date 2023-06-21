package com.spyneai.onboardingv2.ui.intro.holder

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemProcessImageBinding
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shootapp.data.model.CarsBackgroundRes

class ImageProcessingHolder(
    itemView: View,
    listener: OnItemClickListener?,
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<CarsBackgroundRes.BackgroundApp>{

    var listener: OnItemClickListener? = null
    var binding: ItemProcessImageBinding? = null

    val TAG = "OverlaysHolder"

    init {
        binding = ItemProcessImageBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: CarsBackgroundRes.BackgroundApp) {

        binding?.let {
            val background = it.cvMain.background as GradientDrawable
            background.cornerRadius = 30f

            if (data.isSelected){
                background.setStroke(5,ContextCompat.getColor(itemView.context,R.color.primary))
                it.cvMain.background = background

            }else {
                background.setStroke(5, Color.WHITE)
                it.cvMain.background = background
            }

            data.imageUrl?.let { imageUrl ->
                if (imageUrl.contains("http") || imageUrl.contains("https")){
                    Glide.with(itemView)
                        .load(imageUrl)
                        .into(it.ivThumbnail)
                }else {
                    if (Utilities.getPreference(itemView.context,AppConstants.SELECTED_CATEGORY_ORIENTATION).toString() == "portrait"
                        && imageUrl.contains("/storage/emulated/0/DCIM/Spyne/"))
                        itemView.loadSmartly(imageUrl,it.ivThumbnail)
                    else
                        Glide.with(itemView)
                            .load(imageUrl)
                            .into(it.ivThumbnail)
                }
            }



            it.cvMain.setOnClickListener { view ->
                listener?.onItemClick(view,adapterPosition,data)
            }
        }


    }
}