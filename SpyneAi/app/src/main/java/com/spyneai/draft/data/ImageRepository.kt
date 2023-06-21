package com.spyneai.draft.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.shootapp.repository.model.image.Image


import kotlinx.coroutines.flow.Flow

class ImageRepository(
    private val service: ClipperApi,
    val SpyneAppDatabase: SpyneAppDatabase,
    val skuId: String?,
    val projectUuid : String,
    val skuUuid : String,
) {

    fun getSearchResultStream(): Flow<PagingData<Image>> {
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ImageDataSource(service,SpyneAppDatabase,skuId,projectUuid,skuUuid) }
        ).flow
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 100
        const val DEFAULT_PAGE_INDEX = 0
    }

}