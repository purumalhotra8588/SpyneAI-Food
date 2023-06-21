package com.spyneai.model.credit

data class SubscriptionPlanResponse(
    val contactDetails: ContactDetails,
    val invoices: List<Invoice>,
    val next_payment_details: NextPaymentDetails,
    val palnDetails: PalnDetails,
    val status: Int = 0,
)

data class ContactDetails(
    val emailId: String
)

data class Invoice(
    val after_credit: Int,
    val before_credit: Int,
    val created_on: String,
    val credits: Int,
    val debug_data: String,
    val enterprise_id: String,
    val entity_id: String,
    val id: Int,
    val invoice_number: String,
    val invoice_pdf: Any,
    val notes: Any,
    val paid_amount: Double,
    val parent_order_id: Int,
    val payment_id: String,
    val payment_signature: Any,
    val updated_on: String,
    val user_id: String
)

data class NextPaymentDetails(
    val code: String,
    val `data`: NextPaymentDetailData,
    val details: Any,
    val error: Boolean,
    val message: String
)

data class PalnDetails(
    val billing_cycle: String,
    val cost_annually: Int,
    val cost_monthly: Int,
    val number_of_cars: Int
)

data class NextPaymentDetailData(
    val auth_attempts: Int,
    val change_scheduled_at: Any,
    val charge_at: Long,
    val created_at: Long,
    val current_end: Long,
    val current_start: Long,
    val customer_id: Any,
    val customer_notify: Boolean,
    val end_at: Long,
    val ended_at: Any,
    val entity: String,
    val expire_by: Long,
    val has_scheduled_changes: Boolean,
    val id: String,
    val notes: List<Any>,
    val offer_id: Any,
    val paid_count: Int,
    val payment_method: String,
    val plan_id: String,
    val quantity: Int,
    val remaining_count: Int,
    val short_url: String,
    val source: String,
    val start_at: Long,
    val status: String,
    val total_count: Int
)