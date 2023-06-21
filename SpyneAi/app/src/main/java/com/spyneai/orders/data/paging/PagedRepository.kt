package com.spyneai.orders.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.spyneai.base.network.ProjectApi
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.shootapp.repository.model.project.Project

import kotlinx.coroutines.flow.Flow

@ExperimentalPagingApi
class PagedRepository(
    private val service: ProjectApi,
    private val SpyneAppDatabase: SpyneAppDatabase,
    private val status : String,
    private val shootType : String,
    private val isSingle: Boolean = false
) {

    fun getSearchResultStream(): Flow<PagingData<Project>> {
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { if (isSingle) SinglePageProjectDataSource(
                service,
                SpyneAppDatabase,
                status,
                shootType
            )else ProjectDataSource(service,SpyneAppDatabase,status,shootType) }
        ).flow
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        const val DEFAULT_PAGE_INDEX = 0
    }
}