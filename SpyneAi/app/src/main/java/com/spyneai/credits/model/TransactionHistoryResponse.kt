package com.spyneai.credits.model

data class TransactionHistoryResponse(
    val `data`: TransactionData,
    val message: String
)

data class TransactionData(
    val availableCredits: Int,
    val count: Int,
    val transactions: List<TransactionHistory>
)

data class TransactionHistory(
    val actionType: String,
    val credits: Int,
    val date: String,
    val processingSource: String,
    val resourceId: String,
    val resourceType: String,
    val transactionId: Int,
    val userId: String
    )