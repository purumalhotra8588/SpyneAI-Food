package com.spyneai.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.spyneai.R
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.loadThumbnail
import com.spyneai.needs.AppConstants
import com.spyneai.pxFromDp

class SubCategorySearchAdapter(
    val context: Context,
    var subCatList: ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2?>,
    val btnlistener: BtnClickListener
) : RecyclerView.Adapter<SubCategorySearchAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int, data: CatAgnosticResV2.CategoryAgnos.SubCategoryV2?)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSubcat: ImageView = view.findViewById(R.id.ivSubcat)
        val tvSubCat: TextView = view.findViewById(R.id.tvSubCat)
    }

    override fun getItemViewType(position: Int): Int {
        return if (subCatList[position] == null) 0 else 1
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(
                if (viewType == 0) R.layout.item_see_more else R.layout.item_subcat,
                viewGroup,
                false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        subCatList[position]?.let { data ->
            val url = if (data.displayThumbnail.contains("https")) {
                data.displayThumbnail
            } else {
                if (data.displayThumbnail.contains("spyne-cliq"))
                    AppConstants.BASE_IMAGE_URL_FOOD + data.displayThumbnail
                else
                    AppConstants.BASE_IMAGE_URL + data.displayThumbnail
            }

            context.loadThumbnail(
                url,
                viewHolder.ivSubcat,
                pxFromDp(context, 30f).toInt(),
                pxFromDp(context, 30f).toInt()
            )

            viewHolder.tvSubCat.text = data.subCatName

            mClickListener = btnlistener
        }


        viewHolder.ivSubcat.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position, subCatList[position])
        })
    }


    override fun getItemCount() =
        subCatList.size

}
