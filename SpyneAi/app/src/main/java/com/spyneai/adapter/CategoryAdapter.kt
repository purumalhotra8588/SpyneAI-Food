package com.spyneai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.model.CategoryData
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class CategoryAdapter(
    val context: Context,
    private val categoryList: List<CategoryData>,
    val btnlistener: BtnClickListener,
)
    : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCategoryIcon: ImageView = view.findViewById(com.spyneai.R.id.ivCategoryIcon)
        val tvCategoryName: TextView = view.findViewById(com.spyneai.R.id.tvCategoryName)
        val llCategory: LinearLayout = view.findViewById(com.spyneai.R.id.llCategory)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_choose_category, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.tvCategoryName.text=categoryList[position].categoryName

        Glide.with(context)
            .load(categoryList[position].thumbnail)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .skipMemoryCache(false)
            .dontAnimate()
            .into(viewHolder.ivCategoryIcon)

        when(categoryList[position].isSelected){
            true->{
                viewHolder.llCategory.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_light))
                viewHolder.tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.primary))
            }
            else->{
                viewHolder.llCategory.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                viewHolder.tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.black))

            }
        }
        mClickListener = btnlistener
        viewHolder.llCategory.setOnClickListener(View.OnClickListener {

            if (mClickListener != null) {
                Utilities.savePrefrence(
                    context,
                    AppConstants.SELECTED_CATEGORY_ID,
                    categoryList[position].categoryId
                )
            }

            mClickListener?.onBtnClick(position)
        })




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
