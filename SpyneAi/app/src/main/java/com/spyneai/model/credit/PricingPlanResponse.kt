package com.spyneai.model.credit

data class PricingPlanResponse(
    val `data`: PricingPlanData,
    val message: String
)
data class PricingPlanData(
    val details: PlanDetails,
    val type: String
)

data class PlanDetails(
    val available_credits: Int,
    val total_allocated_credits: Int,
    val total_credits_spent: Int,
    val total_done_cars: Int
)