package com.spyneai.credits.holder

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.credits.model.TransactionHistory
import com.spyneai.databinding.ItemTransactionBinding
import com.spyneai.databinding.ItemTransactionDateBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionHistoryHolder (
    itemView: View,
    listener: OnItemClickListener?)
    : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<TransactionHistory>{

    var listener: OnItemClickListener? = null
    var binding : ItemTransactionBinding? = null
    var bindingDate : ItemTransactionDateBinding? = null
    val TAG = "TransactionHistoryHolder"



    init {
        if(itemView.id == R.id.clTransaction){
            binding = ItemTransactionBinding.bind(itemView)
        }else{
            bindingDate = ItemTransactionDateBinding.bind(itemView)
        }
        this.listener = listener
    }

    override fun bind(data: TransactionHistory) {

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val outputFormatTime = SimpleDateFormat("h:mm a")
        val outputFormatDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val dateCurrent: Date = inputFormat.parse(data.date) as Date

        val trimedCurrentDate = outputFormatDate.format(dateCurrent)
        val timeCurrent = outputFormatTime.format(dateCurrent)

        binding?.let {
            it.tvTransactionId.text=data.transactionId.toString()
            it.tvTransactionTime.text=timeCurrent.toString()
            it.tvTransactionType.text=data.actionType
            setCredit(data.actionType,data.credits)
            it.clTransaction.setOnClickListener {
                listener?.onItemClick(
                    it,
                    adapterPosition,
                    data
                )
            }
        }

        bindingDate?.let {
            it.tvTransactionId.text=data.transactionId.toString()
            it.tvTransactionTime.text=timeCurrent.toString()
            it.tvTransactionType.text=data.actionType
            setCredit(data.actionType,data.credits)
            it.tvTransactionDate.text=trimedCurrentDate.toString()
            it.clTransactionDate.setOnClickListener {
                listener?.onItemClick(
                    it,
                    adapterPosition,
                    data
                )
            }
        }
    }

    private fun setCredit(actionType:String,credit:Int){

        if(credit<0){
            binding?.let {
            it.tvCreditConsumption.text = if(credit>-2) "$credit Credit" else "$credit Credits"
            it.tvCreditConsumption.setTextColor(Color.parseColor("#aa0602"))
            }
            bindingDate?.let {
                it.tvCreditConsumption.text = if(credit>-2) "$credit Credit" else "$credit Credits"
                it.tvCreditConsumption.setTextColor(Color.parseColor("#aa0602"))
            }
        }else{
            binding?.let {
                it.tvCreditConsumption.text = if(credit<2) "+$credit Credit" else "+$credit Credits"
                it.tvCreditConsumption.setTextColor(Color.parseColor("#1ca24a"))
            }
            bindingDate?.let {
                it.tvCreditConsumption.text = if(credit<2) "+$credit Credit" else "+$credit Credits"
                it.tvCreditConsumption.setTextColor(Color.parseColor("#1ca24a"))
            }

        }
    }

}