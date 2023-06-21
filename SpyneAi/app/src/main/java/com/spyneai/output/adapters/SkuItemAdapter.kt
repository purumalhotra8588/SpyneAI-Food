package com.spyneai.output.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.databinding.FragmentContainterBinding
import com.spyneai.output.ui.SkuBeforeAfterFragment

class SkuItemAdapter(
    val context: Context,
    val list: ArrayList<String>,
    val fragmentTransaction: FragmentTransaction,
    val btnlistener: BtnClickListener,
)
    : RecyclerView.Adapter<SkuItemAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val flContainer: FrameLayout = view.findViewById(com.spyneai.R.id.flContainer)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {


        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fragment_containter, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

//        var frameLayoutRoot: FrameLayout =
//            LayoutInflater.from(context)
//                .inflate(R.layout.fragment_containter, null) as FrameLayout
//
//        val fragmentBinding = FragmentContainterBinding.bind(frameLayoutRoot)
//
//

        val fragment = SkuBeforeAfterFragment()
        var bundle = Bundle()
        bundle.putString("sku_id", list[position])
        fragment.arguments = bundle
        fragmentTransaction.add(viewHolder.flContainer.id, fragment)
        fragmentTransaction.commit()


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = list.size

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }
}
