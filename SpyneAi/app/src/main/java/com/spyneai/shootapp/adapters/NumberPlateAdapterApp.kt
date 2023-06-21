package com.spyneai.shootapp.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.GenericAdapter
import com.spyneai.base.JavaViewHolderFactory
import com.spyneai.base.OnItemClickListener
import com.spyneai.dashboard.response.CatAgnosticResV2

class NumberPlateAdapterApp (
    list: List<Any>,
    var listener: OnItemClickListener
) : GenericAdapter<Any>(list) {

    override fun getLayoutId(position: Int, obj: Any?): Int {
        return when (obj) {

            is CatAgnosticResV2.CategoryAgnos.NoPlate -> com.spyneai.R.layout.item_no_plate
            else -> error("Unknown type: for position: $position")
        }
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return JavaViewHolderFactory.create(view, viewType, listener)
    }


}