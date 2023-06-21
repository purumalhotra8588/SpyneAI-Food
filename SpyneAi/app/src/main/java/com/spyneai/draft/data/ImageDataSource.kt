package com.spyneai.draft.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.isInternetActive
import com.spyneai.shootapp.repository.model.image.Image

import retrofit2.HttpException
import java.io.IOException

class ImageDataSource(
    private val service: ClipperApi,
    val SpyneAppDatabase: SpyneAppDatabase,
    val skuId: String?,
    val projectUuid : String,
    val skuUuid : String,
) : PagingSource<Int, Image>() {

    val TAG = "SkuDataSource"

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Image> {
        val page = params.key ?: 0

        return try {
            if (skuId != null && BaseApplication.getContext().isInternetActive()){
                val response = service.getImagesOfSku(
                    skuId = skuId
                )

                val prevKey = if (page == 0) null else page - 1
                val nextKey = null

                SpyneAppDatabase.withTransaction {
                     SpyneAppDatabase.imageDao().insertImagesWithCheck(response.data as ArrayList<Image>,projectUuid,skuUuid)
                }

                val finalResponse = SpyneAppDatabase.imageDao().getImagesBySkuUuid(
                    skuUuid = skuUuid
                )

                LoadResult.Page(
                    finalResponse,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }else{
                val response = SpyneAppDatabase.imageDao().getImagesBySkuUuid(
                    skuUuid = skuUuid
                )

                val prevKey = if (page == 0) null else page - 1
                val nextKey = null


                LoadResult.Page(
                    response,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Image>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }


}