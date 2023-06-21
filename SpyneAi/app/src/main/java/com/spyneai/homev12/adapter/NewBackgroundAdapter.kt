package com.spyneai.homev12.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.shootapp.data.model.CarsBackgroundRes

class NewBackgroundAdapter(
    val context: Context,
    var backgroundAppList: ArrayList<CarsBackgroundRes.BackgroundApp>,
    val btnlistener: BtnClickListener
) : RecyclerView.Adapter<NewBackgroundAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
        val tvBgName: TextView = view.findViewById(R.id.tvBgName)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_new_background, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        try {
            viewHolder.tvBgName.text = backgroundAppList[position].bgName

            Glide.with(context).load(
                backgroundAppList[position].imageUrl
            ).into(viewHolder.ivImage)
        } catch (e: Exception) {

        }

        mClickListener = btnlistener
        viewHolder.ivImage.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
    }

    override fun getItemCount() =
        backgroundAppList.size

}
