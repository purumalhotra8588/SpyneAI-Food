package com.spyneai.credits.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.base.JavaViewHolderFactory
import com.spyneai.base.OnItemClickListener
import com.spyneai.credits.holder.TransactionHistoryHolder
import com.spyneai.credits.model.TransactionHistory
import com.spyneai.shootapp.utils.objectToString
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TransactionHistoryAdapter(
    var listener: OnItemClickListener
) : PagingDataAdapter<TransactionHistory, RecyclerView.ViewHolder>(TransactionHistoryDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        val currentItem = getItem(position)
        val previousItem = if (position > 0) getItem(position - 1) else null
        return when (currentItem) {
            is TransactionHistory -> {
                val showItem = compareDate(currentItem, previousItem)

                Log.d("TransactionHistory", "getItemViewType: ${showItem}")
                if (showItem == 0)
                    R.layout.item_transaction
                else
                    R.layout.item_transaction_date
            }
            else -> error("Unknown type: for position: $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return JavaViewHolderFactory.create(view, viewType, listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val obj = getItem(position)
        if (obj != null) {
            when (holder) {
                is TransactionHistoryHolder -> {
                    holder.bind(obj)
                }
            }
        }
    }

    private fun compareDate(currentItem: TransactionHistory, previousItem: TransactionHistory?): Int {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val dateCurrentFull: Date = inputFormat.parse(currentItem.date) as Date
            val dateCurrentOnly = outputFormat.format(dateCurrentFull)
            if (previousItem != null) {
                val datePreviousItem: Date = inputFormat.parse(previousItem.date) as Date
                val datePreviousOnly = outputFormat.format(datePreviousItem)
                if (outputFormat.parse(dateCurrentOnly).before(outputFormat.parse(datePreviousOnly))) 1 else 0
            } else 0
        } catch (e: ParseException) {
            Log.e("TransactionHistory", "compareDate: ${e.objectToString()}", )
            e.printStackTrace()
            0
        }
    }

}