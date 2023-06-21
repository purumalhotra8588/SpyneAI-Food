package com.spyneai.dashboardV2.ui.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.JavaViewHolderFactory
import com.spyneai.base.OnItemClickListener
import com.spyneai.food.DiffusionImages
import com.spyneai.food.StableDiffusionResponse
import com.spyneai.shootapp.data.OnOverlaySelectionListener

class FoodBgAdapter(
    list: List<Any>,
    var listener: OnItemClickListener,
    val overlaySelectionListener: OnOverlaySelectionListener
) : GenericAdapter<Any>(list) {

    override fun getLayoutId(position: Int, obj: Any?): Int {
        return when (obj) {

            is DiffusionImages -> R.layout.food_item
            else -> error("Unknown type: for position: $position")
        }
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return JavaViewHolderFactory.create(view, viewType, listener,overlaySelectionListener)

    }
}