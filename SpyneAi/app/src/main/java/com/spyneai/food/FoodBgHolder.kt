package com.spyneai.shootapp.holders

import android.util.Log
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.dashboardV2.ui.adapters.FoodBgAdapter
import com.spyneai.databinding.FoodItemBinding
import com.spyneai.food.DiffusionImages
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shootapp.data.OnOverlaySelectionListener
import com.spyneai.shootapp.utils.objectToString

class FoodBgHolder(
    itemView: View,
    listener: OnItemClickListener?,
    overlaySelectionListener: OnOverlaySelectionListener?
) : RecyclerView.ViewHolder(itemView),
    GenericAdapter.Binder<DiffusionImages> {

    val TAG = FoodBgHolder::class.java.simpleName
    var listener: OnItemClickListener? = null
    var overlaySelectionListener: OnOverlaySelectionListener? = null
    var binding: FoodItemBinding? = null

    init {
        binding = FoodItemBinding.bind(itemView)
        this.listener = listener
        this.overlaySelectionListener = overlaySelectionListener
    }

    override fun bind(data: DiffusionImages) {
        Log.d(TAG, "bind: ${data.objectToString()}")

        if (data.isSelected) {

            binding?.rvView?.backgroundTintList = ContextCompat.getColorStateList(
                BaseApplication.getContext(),
                R.color.primary_light_dark
            )

            overlaySelectionListener?.onOverlaySelected(
                itemView,
                adapterPosition,
                data
            )
        } else {
            binding?.rvView?.backgroundTintList = ContextCompat.getColorStateList(
                BaseApplication.getContext(),
                R.color.food_light_white
            )
        }


        if (data.isEnabled) {
            if (data.processedImageUrl.isNullOrEmpty()) {
                Glide.with(itemView)
                    .load(data.rawUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding?.ivImage!!)
            }

            if ((Utilities.getBool(itemView.context, AppConstants.ENABLE_AFTER))) {
                Glide.with(itemView)
                    .load(data.processedImageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding?.ivImage!!)
            }
            else{
                    Glide.with(itemView)
                        .load(data.rawUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding?.ivImage!!)
            }



            binding?.ivImage?.let {
                it.setOnClickListener {
                    listener?.onItemClick(
                        itemView,
                        adapterPosition,
                        data
                    )
                }
            }
        } else {
            binding?.ivImage?.let {
                it.setOnClickListener {
                    null
                }
            }
            binding?.ivImage?.setImageDrawable(null)
        }


    }

}