package com.spyneai.threesixty

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.shootapp.data.model.CarsBackgroundRes

class VideoBackgroundAdapter(val context: Context,
                             val channelList: ArrayList<CarsBackgroundRes.BackgroundApp>,
                             var pos: Int,
                             val btnlistener: BtnClickListener?
) : RecyclerView.Adapter<VideoBackgroundAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCarBackground: ImageView = view.findViewById(R.id.ivCarBackground)
        val llChannel: LinearLayout = view.findViewById(R.id.llChannel)
        val tvCarBgName: TextView = view.findViewById(R.id.tvCarBgName)
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_video_backgrounds, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Glide.with(context).load(channelList[position].imageUrl)
            .thumbnail(Glide.with(context).load(R.drawable.placeholder_gif))
            .into(viewHolder.ivCarBackground)
        try {
            val list: List<String> = channelList[position].bgName.trim().split("\\s+".toRegex())
            if (list.size == 1)
                viewHolder.tvCarBgName.text = list[0]
            else
                viewHolder.tvCarBgName.text = list[0] + " " + list[1]
        } catch (e: Exception) { }


        mClickListener = btnlistener


        if (position == pos) {
            viewHolder.llChannel.setBackgroundResource(R.drawable.bg_selected)
            viewHolder.tvCarBgName.setTextColor(ContextCompat.getColor(viewHolder.itemView.context, R.color.primary_light_dark))
        }
        else {
            viewHolder.llChannel.setBackgroundResource(R.drawable.bg_channel)
            viewHolder.tvCarBgName.setTextColor(
                ContextCompat.getColor(
                    viewHolder.itemView.context,
                    R.color.black
                )
            )
        }

        viewHolder.llChannel.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)

            pos = position

            viewHolder.llChannel.setBackgroundResource(R.drawable.bg_selected)
        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = channelList.size
    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }
}