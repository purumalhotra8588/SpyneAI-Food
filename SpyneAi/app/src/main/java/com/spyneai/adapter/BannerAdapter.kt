package com.spyneai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.islamkhsh.CardSliderAdapter
import com.spyneai.R
import com.spyneai.homev12.adapter.NewBackgroundAdapter
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class BannerAdapter(
    private val bannerList: ArrayList<String>,
    val context: Context,
    val btnlistener: BtnClickListener
) : CardSliderAdapter<BannerAdapter.MovieViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.banner_item, parent, false)
        return MovieViewHolder(view)
    }

    override fun bindVH(holder: MovieViewHolder, position: Int) {

        if (Utilities.getPreference(
                context,
                AppConstants.SELECTED_CATEGORY_ID
            ) == AppConstants.FOOD_AND_BEV_CATEGORY_ID
        ) {
            Glide.with(context)
                .asBitmap()
                .load("https://spyne-static.s3.amazonaws.com/ezgif.com-gif-maker+(3).webp")
                .into(holder.imageView)

        } else {
            Glide.with(context)
                .asDrawable()
                .load(bannerList[position])
                .into(holder.imageView)
        }



        BannerAdapter.mClickListener = btnlistener
        holder.imageView.setOnClickListener(View.OnClickListener {
            if (BannerAdapter.mClickListener != null)
                BannerAdapter.mClickListener?.onBtnClick(position)
        })

    }

    override fun getItemCount(): Int {
        return bannerList.size
    }

    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = itemView.findViewById(R.id.ivBannerImage)
    }
}