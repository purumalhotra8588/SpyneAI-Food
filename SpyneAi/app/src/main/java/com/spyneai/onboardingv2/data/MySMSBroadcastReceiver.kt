package com.spyneai.onboardingv2.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.google.android.gms.auth.api.phone.SmsRetriever

import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status


class MySMSBroadcastReceiver : BroadcastReceiver() {
    val TAG = MySMSBroadcastReceiver::class.simpleName

    override fun onReceive(context: Context?, intent: Intent) {
        Log.d(TAG, "onReceive: ${intent.action}")
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status: Status? = extras!![SmsRetriever.EXTRA_STATUS] as Status?
            Log.d(TAG, "onReceive: status $status")
            when (status?.getStatusCode()) {
                CommonStatusCodes.SUCCESS -> {
                    var message: String?= extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?
                    Log.d(TAG, "onReceive: ${message}")
                }

                CommonStatusCodes.TIMEOUT -> {
                    Log.d(TAG, "onReceive: time out")
                }
            }
        }
    }
}