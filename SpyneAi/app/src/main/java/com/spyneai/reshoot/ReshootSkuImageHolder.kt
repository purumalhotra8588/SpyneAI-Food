package com.spyneai.reshoot

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemReshootSkuImageBinding
import com.spyneai.reshoot.data.ReshootSkuRes
import kotlinx.android.synthetic.main.item_reshoot_sku_image.view.*


class ReshootSkuImageHolder (
    itemView: View,
    listener: OnItemClickListener?)
    : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<ReshootSkuRes.Data.Image>{

    var listener: OnItemClickListener? = null
    var binding : ItemReshootSkuImageBinding? = null
    val TAG = "ReshootHolder"
    var comments = ""
    var commentList: List<String> = listOf()

    init {
        binding = ItemReshootSkuImageBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: ReshootSkuRes.Data.Image) {

        var isSelected=false

        binding?.let {
            Glide.with(itemView.context)
                .load(data.outputImageHresUrl)
                .into(it.ivImage)
            if(!data.reshootComment.isNullOrEmpty()){
             comments = data.reshootComment.replace("[\"","").replace("\"]","").replace("\"","")
            // commentList = Lists.newArrayList(Splitter.on(",").split(comments))
            it.tvReshootComment.text= commentList[0]
            }else{
                it.tvReshootComment.text= "No Comment Added"
            }


            it.tvReshootComment.setOnClickListener {
                if (!data.reshootComment.isNullOrEmpty()) {
                    if (!isSelected) {
                        for (i in 1 until (commentList.size)) {
                            it.tvReshootComment.text =
                                it.tvReshootComment.text.toString() + "\n" + commentList[i]

                        }
                        isSelected = true

                    } else {
                        it.tvReshootComment.text = commentList[0]
                        isSelected = false
                    }

                }
            }
            it.ivImage.setOnClickListener {
                listener?.onItemClick(
                    it,
                    adapterPosition,
                    data
                )
            }
        }
    }

}