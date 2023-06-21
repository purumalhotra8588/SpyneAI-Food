package com.spyneai.shootapp.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.JavaViewHolderFactory
import com.spyneai.base.OnItemClickListener
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.shootapp.data.OnOverlaySelectionListener

class OverlaysAdapter (
    list: List<Any>,
    var listener: OnItemClickListener,
    var overlaySelectionListener : OnOverlaySelectionListener
) : GenericAdapter<Any>(list) {

    override fun getLayoutId(position: Int, obj: Any?): Int {
        return when (obj) {

            is CatAgnosticResV2.CategoryAgnos.SubCategoryV2.OverlayApp -> R.layout.item_overlays
            //is OverlaysResponse.Overlays -> R.layout.item_single_image_overlays
            is CatAgnosticResV2.CategoryAgnos.Interior ->  R.layout.item_interior
            //is NewSubCatResponse.Interior ->  R.layout.item_interior
            is CatAgnosticResV2.CategoryAgnos.Miscellaneou -> R.layout.item_miscellanous
            //is NewSubCatResponse.Miscellaneous -> R.layout.item_miscellanous
            else -> error("Unknown type: for position: $position")
        }
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return JavaViewHolderFactory.create(view, viewType, listener,overlaySelectionListener)
    }


}