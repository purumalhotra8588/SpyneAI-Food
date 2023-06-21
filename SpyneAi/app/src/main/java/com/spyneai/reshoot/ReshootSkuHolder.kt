package com.spyneai.reshoot

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemReshootRequestBinding
import com.spyneai.reshoot.data.ReshootSkuRes

class ReshootSkuHolder(
    itemView: View,
    listener: OnItemClickListener?)
    : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<ReshootSkuRes.Data>{

    var listener: OnItemClickListener? = null
    var binding : ItemReshootRequestBinding? = null
    val TAG = "ReshootHolder"

    init {
        binding = ItemReshootRequestBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: ReshootSkuRes.Data) {
        binding?.let {
            it.tvSkuName.text = "SKU Name : ${data.skuName}"
            it.tvSkuId.text = "SKU ID  :  ${data.skuId}"

            it.rvImages.apply {
                layoutManager = LinearLayoutManager(itemView.context,LinearLayoutManager.HORIZONTAL,false)
                adapter = ReshootSkuImageAdapter(data.images,listener!!)
            }

            it.clReshoot.setOnClickListener {
                listener?.onItemClick(
                    it,
                    adapterPosition,
                    data
                )
            }
        }
    }

}