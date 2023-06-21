package com.spyneai.credits.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.ClipperApi
import com.spyneai.credits.model.TransactionHistory
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.paging.PagedRepository
import retrofit2.HttpException
import java.io.IOException

@ExperimentalPagingApi
class TransactionDataSource(
    private val service: ClipperApi,
    val fromDate: String,
    val toDate: String,
    val actionType: String
) : PagingSource<Int, TransactionHistory>() {

    val TAG = "TransactionDataSource"

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TransactionHistory> {
        val page = params.key ?: PagedRepository.DEFAULT_PAGE_INDEX
        Log.d(TAG, "load: $page")
        //val apiQuery = query + IN_QUALIFIER

        return try {
                val response = service.getTransactionHistory(
                    pageNumber = page,
                    fromDate = fromDate,
                    toDate = toDate,
                    actionType = actionType
                )

            Utilities.savePrefrence(BaseApplication.getContext(),AppConstants.CREDITS,response.data.availableCredits.toString())

                val prevKey = if (page == PagedRepository.DEFAULT_PAGE_INDEX) null else page - 1
                val nextKey = if (response.data.transactions.isNullOrEmpty()) null else page + 1

                LoadResult.Page(
                    response.data.transactions,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
        catch (exception : Exception){
            return LoadResult.Error(exception)
        }

    }


    override fun getRefreshKey(state: PagingState<Int, TransactionHistory>): Int? {

        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }


    }
}