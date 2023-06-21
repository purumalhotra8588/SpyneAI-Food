package com.spyneai.loginsignup.data

data class LoginBodyV2(
    var strategy: String="",
    val apiKey: String ="",
    val emailId: String="",
    val password: String="",
    val deviceId: String="",
    val referralCode: String="",
    val contactNumber: String="",
    val resourceType: String="",
    val OTP: String="",

    )
