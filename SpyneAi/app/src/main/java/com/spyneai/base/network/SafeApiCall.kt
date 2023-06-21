package com.spyneai.base.network

import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

interface SafeApiCall {
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ) : Resource<T> {
        return withContext(Dispatchers.IO){
            try {
                Resource.Success(apiCall.invoke())
            }catch (throwable: Throwable){
                val s =""
                val throwableDeatils = JSONObject().apply {
                    put("type",throwable.javaClass.canonicalName)
                    put("cause",throwable.cause)
                    put("message",throwable.localizedMessage)
                }.toString()

                when(throwable){

                    is ServerException -> {
                        Resource.Failure(false, throwable.hashCode(),
                            throwable.response,
                            throwableDeatils)
                    }

                    is HttpException -> {
                        Resource.Failure(false, throwable.code(),
                            throwable.response()?.errorBody().toString(),throwableDeatils)
                    }

                    is JsonSyntaxException -> {
                        Resource.Failure(false, throwable.hashCode(),
                            throwable.message,
                            throwableDeatils)
                    }

                    is SocketTimeoutException -> {
                        Resource.Failure(false, throwable.hashCode(),
                            throwable.message,
                            throwableDeatils)
                    }

                    is SSLHandshakeException -> {
                        Resource.Failure(false, throwable.hashCode(),
                            throwable.message,
                            throwableDeatils)
                    }

                    else -> {
                        Resource.Failure(true, null,
                            "Please check your internet connection",
                            throwableDeatils)
                    }
                }
            }
        }
    }
}