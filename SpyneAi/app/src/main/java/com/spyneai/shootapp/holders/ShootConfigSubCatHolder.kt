package com.spyneai.shootapp.holders

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.databinding.ItemShootConfigSubcategoryBinding
import com.spyneai.needs.AppConstants

class ShootConfigSubCatHolder(
    itemView: View,
    listener: OnItemClickListener?
) : RecyclerView.ViewHolder(itemView),
    GenericAdapter.Binder<CatAgnosticResV2.CategoryAgnos.SubCategoryV2> {

    var listener: OnItemClickListener? = null
    var binding: ItemShootConfigSubcategoryBinding? = null

    init {
        binding = ItemShootConfigSubcategoryBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(subcategory: CatAgnosticResV2.CategoryAgnos.SubCategoryV2) {


        binding?.let {
            it.tvSubCat.text = subcategory.subCatName

            Glide.with(itemView.context)
                .load(
                    if (subcategory.displayThumbnail.contains("https")) {
                        subcategory.displayThumbnail
                    } else {
                        if (subcategory.displayThumbnail.contains("spyne-cliq"))
                            AppConstants.BASE_IMAGE_URL_FOOD + subcategory.displayThumbnail
                        else
                            AppConstants.BASE_IMAGE_URL + subcategory.displayThumbnail
                    }
                ).placeholder(R.mipmap.defaults)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(it.ivSubcat)


            it.ivSubcat.background = if (subcategory.isSelected) ContextCompat.getDrawable(
                itemView.context,
                R.drawable.bg_selected_image_type
            ) else ContextCompat.getDrawable(itemView.context, R.drawable.bg_unselected_image_type)
            it.tvSubCat.setTextColor(
                if (subcategory.isSelected) ContextCompat.getColor(
                    itemView.context,
                    R.color.primary_light_dark
                ) else ContextCompat.getColor(itemView.context, R.color.categories_text)
            )

            it.ivSubcat.setOnClickListener {
                listener?.onItemClick(
                    it,
                    adapterPosition,
                    subcategory
                )
            }
        }
    }
}