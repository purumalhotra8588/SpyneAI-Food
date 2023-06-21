package com.spyneai.credits.adapter

import androidx.recyclerview.widget.DiffUtil
import com.spyneai.credits.model.TransactionHistory

class TransactionHistoryDiffCallback : DiffUtil.ItemCallback<TransactionHistory>() {
    override fun areItemsTheSame(oldItem: TransactionHistory, newItem: TransactionHistory): Boolean {
        return oldItem.transactionId == newItem.transactionId
    }

    override fun areContentsTheSame(oldItem: TransactionHistory, newItem: TransactionHistory): Boolean {
        // You can compare all the fields of the items here to determine if their contents are the same.
        return oldItem == newItem
    }
}