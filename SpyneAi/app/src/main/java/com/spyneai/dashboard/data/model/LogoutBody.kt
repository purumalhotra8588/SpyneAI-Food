package com.spyneai.dashboardV2.data.model

data class LogoutBody(
    val deviceIdList: List<String>,
    val isLogOutFromAllDevices: Boolean = false
)