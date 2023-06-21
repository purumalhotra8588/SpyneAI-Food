package com.spyneai.credits.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.spyneai.base.network.ClipperApi
import com.spyneai.credits.model.TransactionHistory
import kotlinx.coroutines.flow.Flow

@ExperimentalPagingApi
class TransactionPagedRepository(
private val service: ClipperApi,
val fromDate: String,
val toDate: String,
val actionType: String
) {

    fun getSearchResultStream(): Flow<PagingData<TransactionHistory>> {
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { TransactionDataSource(service,fromDate,toDate,actionType) }
        ).flow
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        const val DEFAULT_PAGE_INDEX = 0
    }
}