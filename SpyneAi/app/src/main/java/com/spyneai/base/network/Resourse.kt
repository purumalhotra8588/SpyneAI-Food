package com.spyneai.base.network

sealed class Resource<out ResultType> {
    data class Success<out ResultType>(val value: ResultType) : Resource<ResultType>()
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        var errorMessage: String?,
        val throwable: String? = null
    ) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}