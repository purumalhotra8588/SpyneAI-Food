package com.spyneai.credits.model

data class TransactionTypeResponse(
    val `data`: List<TransactionType>
)

data class TransactionType(
    val transactionId: String,
    val transactionType: String,
    val priority:Int,
    var isSelected: Boolean=false
)