package com.spyneai.shootapp.utils

import android.content.Context

import android.os.Build
import com.google.gson.Gson

import java.util.concurrent.Executor


fun Context.mainExecutor(): Executor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    mainExecutor
} else {
    MainExecutor()
}

fun Any.objectToString(): String = Gson().toJson(this)
