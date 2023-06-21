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
import com.spyneai.dashboard.response.CatAgnosticResV2

class ProjectsAdapter(
    val context: Context,
    var projectList: List<CatAgnosticResV2.CategoryAgnos.Crousel>,
    val btnlistener: BtnClickListener
)
    : RecyclerView.Adapter<ProjectsAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
        val tvProject: TextView = view.findViewById(R.id.tvProject)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_projects, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        Glide.with(context).load(
            projectList[position].before_image
        ).into(viewHolder.ivImage)

        mClickListener = btnlistener
        viewHolder.ivImage.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
    }

    override fun getItemCount() =
        projectList.size

}
