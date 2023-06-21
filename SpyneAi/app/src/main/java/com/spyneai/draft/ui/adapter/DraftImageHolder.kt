package com.spyneai.draft.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.shootapp.repository.model.image.Image


class DraftImageHolder(
    val context: Context,
    val categoryId : String,
    val view: View
) : RecyclerView.ViewHolder(view) {
    val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
    
    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(
            context: Context,
            categoryId : String,
            parent: ViewGroup
        ): DraftImageHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_app_raw_images, parent, false)
            return DraftImageHolder(context, categoryId,view)
        }
    }

    fun bind(item: Image?) {
        item?.let { showData(it) }
    }

    private fun showData(imageApp: Image) {
        try {
            imageApp.path?.let {
                if (categoryId == AppConstants.CARS_CATEGORY_ID || categoryId == AppConstants.BIKES_CATEGORY_ID) {
                    Glide.with(context) // replace with 'this' if it's in activity
                        .load(it)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                        .into(ivThumbnail)
                } else {
                    context.loadSmartly(it, ivThumbnail)
                }
            }


        } catch (e: Exception) {

        }

    }
}