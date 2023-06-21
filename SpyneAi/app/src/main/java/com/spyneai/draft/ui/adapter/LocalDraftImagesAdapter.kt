package com.spyneai.draft.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.shootapp.repository.model.image.Image


class LocalDraftImagesAdapter(
    val context: Context,
    val imageAppList: ArrayList<Image>,
    val categoryId: String
) : BaseAdapter() {
    override fun getCount(): Int {
        return imageAppList.size
    }

    override fun getItem(position: Int): Any? {
        return imageAppList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.item_app_raw_images, null)
        val ivRaw = view.findViewById<TextView>(R.id.ivRaw) as ImageView

        try {
            imageAppList[position].path?.let {
                if (categoryId == AppConstants.CARS_CATEGORY_ID || categoryId == AppConstants.BIKES_CATEGORY_ID) {
                    Glide.with(context) // replace with 'this' if it's in activity
                        .load(it)
                        .thumbnail(Glide.with(context).load(R.drawable.placeholder_gif))
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .skipMemoryCache(false)
                        .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                        .into(ivRaw)
                } else {
                    context.loadSmartly(it, ivRaw)
                }
            }


        } catch (e: Exception) {

        }



        return view
    }
}