package com.spyneai.dashboard.ui.adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.dashboard.data.model.RidResponse
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.dashboard.ui.ChooseRIDActivity
import com.spyneai.dashboard.ui.dialogs.ChooseRIDFragment
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class RidAdapter(val context: Context,
                 var ridList: ArrayList<RidResponse.Data>,
                 val btnlistener: BtnClickListener
)
    : RecyclerView.Adapter<RidAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rlRidList: RelativeLayout = view.findViewById(R.id.rlRidList)
        val tvRID: TextView = view.findViewById(R.id.tvRID)
        val tvRestaurantName: TextView = view.findViewById(R.id.tvRestaurantName)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item

        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_rid_list, viewGroup, false)

        return ViewHolder(view)

    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {


        viewHolder.tvRID.text = ridList[position].entity_id
        viewHolder.tvRestaurantName.text = ridList[position].entity_name

        val drawable = viewHolder.rlRidList.background as GradientDrawable

        if(context.getString(R.string.app_name) == AppConstants.SWIGGY) {
            if (ridList[position].isSelected){
                drawable.setStroke(1, ContextCompat.getColor(context, R.color.green))
            }else {
                drawable.setStroke(1, ContextCompat.getColor(context, R.color.primary_light))
            }
        }


        mClickListener = btnlistener
        viewHolder.rlRidList.setOnClickListener(View.OnClickListener {

            if (mClickListener != null){
//                Utilities.savePrefrence(context, AppConstants.ENTITY_ID, ridList[0].entity_id)
            }

            mClickListener?.onBtnClick(position)


        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() =
        ridList.size

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

}
