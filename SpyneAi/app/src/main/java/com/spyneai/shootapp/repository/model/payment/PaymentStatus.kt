package com.spyneai.shootapp.repository.model.payment

data class PaymentStatus(
    val status: Boolean,
    val message: String?,
    val paymentId: String?,
    val signature: String?
)
