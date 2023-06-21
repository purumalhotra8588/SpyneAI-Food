package com.spyneai.reshoot

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.JavaViewHolderFactory
import com.spyneai.base.OnItemClickListener

import com.spyneai.reshoot.data.ReshootOverlaysRes

import com.spyneai.shootapp.data.OnOverlaySelectionListener
import com.spyneai.shootapp.repository.model.image.Image

class ReshootAdapter(
    list: List<Any>,
    var listener: OnItemClickListener,
    var overlaySelectionListener : OnOverlaySelectionListener
) : GenericAdapter<Any>(list) {

    override fun getLayoutId(position: Int, obj: Any?): Int {
        return when (obj) {

            is ReshootOverlaysRes.Data -> R.layout.item_reshoot
            is Image -> R.layout.item_reshoot_ecom
            else -> error("Unknown type: for position: $position")
        }
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return JavaViewHolderFactory.create(view, viewType, listener,overlaySelectionListener)
    }


}