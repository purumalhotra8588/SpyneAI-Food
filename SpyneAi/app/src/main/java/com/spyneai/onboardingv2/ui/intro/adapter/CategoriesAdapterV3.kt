package com.spyneai.onboardingv2.ui.intro.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.JavaViewHolderFactory
import com.spyneai.base.OnItemClickListener
import com.spyneai.model.CategoryData
import com.spyneai.onboardingv2.data.CategoryModalClass
import com.spyneai.onboardingv2.data.SelectCategoryRes

class CategoriesAdapterV3 (
    list: List<Any>,
    var listener: OnItemClickListener
) : GenericAdapter<Any>(list) {

    override fun getLayoutId(position: Int, obj: Any?): Int {
        return when (obj) {

            is SelectCategoryRes.Data -> R.layout.item_catgory_webview
            else -> error("Unknown type: for position: $position")
        }
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return JavaViewHolderFactory.create(view, viewType, listener)
    }

}