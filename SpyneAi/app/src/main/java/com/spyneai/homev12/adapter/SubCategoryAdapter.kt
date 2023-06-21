package com.spyneai.homev12.adapter

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
import com.spyneai.needs.AppConstants

class SubCategoryAdapter( val context: Context,
var subCatList : ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2>,
val btnlistener: BtnClickListener
)
: RecyclerView.Adapter<SubCategoryAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSubcat: ImageView = view.findViewById(R.id.ivSubcat)
        val tvSubCat: TextView = view.findViewById(R.id.tvSubCat)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_app_subcat, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        Glide.with(context).load(
            if(subCatList[position].displayThumbnail.contains("https")){
                subCatList[position].displayThumbnail
            }else{
                if(subCatList[position].displayThumbnail.contains("spyne-cliq"))
                    AppConstants.BASE_IMAGE_URL_FOOD + subCatList[position].displayThumbnail
                else
                    AppConstants.BASE_IMAGE_URL + subCatList[position].displayThumbnail
            }

//                    https://spyne-static.s3.amazonaws.com/Sub_Categories/Automobile/Sports.png

        ).placeholder(R.mipmap.defaults)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(viewHolder.ivSubcat)

        viewHolder.tvSubCat.text = subCatList[position].subCatName

        mClickListener = btnlistener
        viewHolder.ivSubcat.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
    }

    override fun getItemCount() =
        subCatList.size

}
