package com.spyneai.singleimageprocessing.ui.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.databinding.ItemSingleImageSubcategoriesBinding
import com.spyneai.needs.AppConstants

class SingleImageSubcategoryHolder(
    itemView: View,
    listener: OnItemClickListener?
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<NewSubCatResponse.Subcategory> {

    var listener: OnItemClickListener? = null
    var binding : ItemSingleImageSubcategoriesBinding? = null

    init {
        binding = ItemSingleImageSubcategoriesBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(subcategory: NewSubCatResponse.Subcategory) {

        binding.apply {
            this?.tvSubcategories?.text = subcategory.sub_cat_name
        }

        binding?.ivSubCategories?.let {
            Glide.with(itemView)
                .load(subcategory.display_thumbnail)
                .into(it)
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