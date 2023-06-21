package com.spyneai.credits.model

data class CreditResourceBody(
    val resource_id: String,
    val resource_type: String,
    val source: String
)