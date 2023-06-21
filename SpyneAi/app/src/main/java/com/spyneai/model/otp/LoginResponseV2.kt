package com.spyneai.model.otp

import com.google.gson.annotations.SerializedName

data class LoginResponseV2(
    val message: String,
    val error: Boolean,
    val code: String,
    val details: String?,
    val data: Data,
)
data class Data(
    val status: Long,
    val message: String,
    @SerializedName("auth_token")
    val authToken: String,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("crm_role")
    val crmRole: String,
    val role: String,
    @SerializedName("secret_key")
    val secretKey: String,
    @SerializedName("check_qc")
    val checkQc: Boolean,
    @SerializedName("contact_no")
    val contactNo: String,
    @SerializedName("isd_code")
    val isdCode: String,
    @SerializedName("email_id")
    val emailId: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("enterprise_id")
    val enterpriseId: String,
    @SerializedName("discount")
    val discount: String,
    @SerializedName("is_new_system_user")
    val isNewUser: Boolean,
    @SerializedName("enterprise_name")
    val enterpriseName: String,
    @SerializedName("team_name")
    val teamName: String,
    @SerializedName("user_role")
    val userRole: String,
    @SerializedName("team_id")
    val teamId: String,
    val base64Token: String,
)

