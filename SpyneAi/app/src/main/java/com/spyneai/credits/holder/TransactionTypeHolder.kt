package com.spyneai.credits.holder

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.credits.model.TransactionType
import com.spyneai.databinding.ItemTransactionTypeBinding

class TransactionTypeHolder(
    itemView: View,
    listener: OnItemClickListener?,
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<TransactionType>{

    var listener: OnItemClickListener? = null
    var binding: ItemTransactionTypeBinding? = null

    val TAG = "OverlaysHolder"

    init {
        binding = ItemTransactionTypeBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: TransactionType) {

        binding?.let {
            it.tvTransactionType.text=data.transactionType

            it.tvTransactionType.setBackgroundColor(
                if(data.isSelected)
                    ContextCompat.getColor(itemView.context, R.color.transaction_type_selected)
                else
                    ContextCompat.getColor(itemView.context, R.color.transaction_type_unselected))

            it.tvTransactionType.setOnClickListener { view ->
                listener?.onItemClick(view,adapterPosition,data)
            }

        }


    }
}