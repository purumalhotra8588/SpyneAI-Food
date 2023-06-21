package com.spyneai.dashboardV2.data.model

data class LogoutResponse(
    val code: String,
    val details: Any,
    val error: Boolean,
    val message: String
)