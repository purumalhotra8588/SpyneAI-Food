package com.spyneai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.dashboard.repository.model.AngleClassifierResponseV2
import com.spyneai.model.CategoryData
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class EcomClassifierAdapter(
    val context: Context,
    private val categoryList: List<String>,
)
    : RecyclerView.Adapter<EcomClassifierAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCrop: ImageView = view.findViewById(com.spyneai.R.id.ivCrop)
        val tvCrop: TextView = view.findViewById(com.spyneai.R.id.tvCrop)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.layout_hint, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.tvCrop.text = categoryList[position]

            Glide.with(context)
                .load(R.drawable.baseline_check_circle_24)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .skipMemoryCache(false)
                .dontAnimate()
                .into(viewHolder.ivCrop)

    }

    override fun getItemCount() = categoryList.size

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit):
            T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }
}
