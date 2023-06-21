package com.spyneai.shootapp.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.JavaViewHolderFactory
import com.spyneai.base.OnItemClickListener
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.dashboard.response.NewSubCatResponse

class SubcatAndAngleAdapter(
    list: List<Any>,
    var listener: OnItemClickListener,
    val shootConfiguration: Boolean = false
) : GenericAdapter<Any>(list) {

    override fun getLayoutId(position: Int, obj: Any?): Int {
        return when (obj) {

            is CatAgnosticResV2.CategoryAgnos.SubCategoryV2 -> if (shootConfiguration) R.layout.item_shoot_config_subcategory else R.layout.item_app_subcategories
            is NewSubCatResponse.Subcategory -> R.layout.item_single_image_subcategories
            else -> error("Unknown type: for position: $position")
        }
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return JavaViewHolderFactory.create(view, viewType, listener)
    }


}