package com.spyneai.base.network

import com.spyneai.app.BaseApplication
import com.spyneai.getRequestHeaderData
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
            .addHeader("device_details", getRequestHeaderData().toString())
            .addHeader("os", Utilities.getPreference(BaseApplication.getContext(), AppConstants.OS).toString())
            .addHeader("type", Utilities.getPreference(BaseApplication.getContext(), AppConstants.TYPE).toString())
            .addHeader("app_version_code", Utilities.getPreference(BaseApplication.getContext(), AppConstants.APP_VERSION_CODE).toString())
        if (chain.request().body?.contentType()?.subtype != "octet-stream" && chain.request().body?.contentType()?.subtype != "jpeg")
            builder.addHeader(
                "Authorization", "Bearer ${
                    Utilities.getPreference(
                        BaseApplication.getContext(),
                        AppConstants.AUTH_KEY
                    ).toString()
                }"
            )
        val request = builder.build()

        return chain.proceed(request)
    }
}