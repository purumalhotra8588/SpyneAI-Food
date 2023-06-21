package com.spyneai.shootapp.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.databinding.ItemAppSubcategoriesBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class SubcategoryHolder(
    itemView: View,
    listener: OnItemClickListener?
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<CatAgnosticResV2.CategoryAgnos.SubCategoryV2> {

    var listener: OnItemClickListener? = null
    var binding : ItemAppSubcategoriesBinding? = null

    init {
        binding = ItemAppSubcategoriesBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(subcategory: CatAgnosticResV2.CategoryAgnos.SubCategoryV2) {

        binding.apply {
            this?.tvSubcategories?.text = subcategory.subCatName
        }


        if(subcategory.displayThumbnail.contains("https://storage.googleapis.com/spyne-cliq/")){
            Glide.with(itemView)
                .load(subcategory.displayThumbnail)
                .into(binding?.ivSubCategories!!)

        }else{
            if (Utilities.getPreference(itemView.context, AppConstants.CATEGORY_ID).equals(AppConstants.FOOD_AND_BEV_CATEGORY_ID)){
                Glide.with(itemView)
                    .load(AppConstants.BASE_IMAGE_URL_FOOD + subcategory.displayThumbnail)
                    .into(binding?.ivSubCategories!!)
            } else{
                Glide.with(itemView)
                    .load(
                if(subcategory.displayThumbnail.contains("https")){
                    subcategory.displayThumbnail
                }else{
                    if(subcategory.displayThumbnail.contains("spyne-cliq"))
                        AppConstants.BASE_IMAGE_URL_FOOD + subcategory.displayThumbnail
                    else
                        AppConstants.BASE_IMAGE_URL + subcategory.displayThumbnail
                }
            )
                    .into(binding?.ivSubCategories!!)
            }
        }

        binding?.llSubCategories?.setOnClickListener {
            listener?.onItemClick(
                it,
                adapterPosition,
                subcategory
            )
        }
    }
}