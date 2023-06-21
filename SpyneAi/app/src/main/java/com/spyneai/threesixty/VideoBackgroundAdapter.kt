package com.spyneai.threesixty

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
        val ivBackground: ImageView = view.findViewById(R.id.ivBackground)
        val clMain: ConstraintLayout = view.findViewById(R.id.clMain)
        val tvBgName: TextView = view.findViewById(R.id.tvBgName)
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_video_backgrounds, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Glide.with(context).load(channelList[position].imageUrl)
            .thumbnail(Glide.with(context).load(R.drawable.placeholder_gif))
            .into(viewHolder.ivBackground)
        try {
            val list: List<String> = channelList[position].bgName.trim().split("\\s+".toRegex())
            if (list.size == 1)
                viewHolder.tvBgName.text = list[0]
            else
                viewHolder.tvBgName.text = list[0] + " " + list[1]
        } catch (e: Exception) { }


        mClickListener = btnlistener


        if (position == pos)
            viewHolder.clMain.setBackgroundResource(R.drawable.bg_selected)
        else
            viewHolder.clMain.setBackgroundResource(R.drawable.bg_channel)

        viewHolder.clMain.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)

            pos = position

            viewHolder.clMain.setBackgroundResource(R.drawable.bg_selected)
        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = channelList.size
    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }
}