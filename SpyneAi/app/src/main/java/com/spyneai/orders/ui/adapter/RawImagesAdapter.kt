package com.spyneai.orders.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.spyneai.R
import com.spyneai.loadSmartlyWithCache
import com.spyneai.shootapp.repository.model.image.Image


class RawImagesAdapter(
    val activity: Context,
    val imageList: ArrayList<Image>
) : BaseAdapter() {
    override fun getCount(): Int {
        return imageList.size
    }

    override fun getItem(position: Int): Any? {
        return imageList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(activity, R.layout.item_app_raw_images, null)
        val ivRaw = view.findViewById<TextView>(R.id.ivRaw) as ImageView

        try {
            activity.loadSmartlyWithCache(
                if (imageList[position].output_image_lres_url.isNullOrEmpty()) imageList[position].path else imageList[position].output_image_lres_url,
                ivRaw
            )

        } catch (e: Exception) {

        }



        return view
    }
}