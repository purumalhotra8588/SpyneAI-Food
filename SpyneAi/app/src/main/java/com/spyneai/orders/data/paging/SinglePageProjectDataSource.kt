package com.spyneai.orders.data.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.spyneai.base.network.ProjectApi
import com.spyneai.isInternetActive
import com.spyneai.app.BaseApplication
import retrofit2.HttpException
import java.io.IOException
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.shootapp.repository.model.project.Project

@ExperimentalPagingApi
class SinglePageProjectDataSource(
    private val service: ProjectApi,
    val SpyneAppDatabase: SpyneAppDatabase,
    val status: String,
    val shootType: String
) : PagingSource<Int, Project>() {

    val TAG = "SinglePageProjectDataSource"

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Project> {
        return try {
            val page = params.key ?: 0
            Log.d(TAG, "load: $page")

            if (BaseApplication.getContext().isInternetActive()){
                val response = service.getPagedProjects(
                    pageNo = page,
                    status = status,
                    shootType = shootType
                )

                // insert the data into the database
                SpyneAppDatabase.withTransaction {
                    SpyneAppDatabase.projectDao().insertWithCheck(response.data)
                }

                val finalResponse = if (Utilities.getPreference(
                        BaseApplication.getContext(),
                        AppConstants.TEAM_ID
                    ).isNullOrEmpty()
                ) SpyneAppDatabase.projectDao().getProjectWithOutFilterTeamIdNull(
                    offset = page.times(AppConstants.FETCH_PROJECT_LIMIT_HOME),
                    limit = AppConstants.FETCH_PROJECT_LIMIT_HOME
                ) else SpyneAppDatabase.projectDao().getProjectWithOutFilter(
                    offset = page.times(AppConstants.FETCH_PROJECT_LIMIT_HOME),
                    limit = AppConstants.FETCH_PROJECT_LIMIT_HOME
                )

                var filteredResponse = finalResponse?.filter { it.categoryId ==  Utilities.getPreference(
                    BaseApplication.getContext(),
                    AppConstants.SELECTED_CATEGORY_ID
                )}

                Log.d(TAG, "load: filtered Response $filteredResponse")

                LoadResult.Page(
                    filteredResponse ?: ArrayList(),
                    prevKey = null,
                    nextKey = null
                )
            }else{
                val response = if (Utilities.getPreference(
                        BaseApplication.getContext(),
                        AppConstants.TEAM_ID
                    ).isNullOrEmpty()
                ) SpyneAppDatabase.projectDao().getProjectWithOutFilterTeamIdNull(
                    offset = page.times(AppConstants.FETCH_PROJECT_LIMIT_HOME),
                    limit = AppConstants.FETCH_PROJECT_LIMIT_HOME
                ) else SpyneAppDatabase.projectDao().getProjectWithOutFilter(
                    offset = page.times(AppConstants.FETCH_PROJECT_LIMIT_HOME),
                    limit = AppConstants.FETCH_PROJECT_LIMIT_HOME
                )

                var filteredResponse = response?.filter { it.categoryId ==  Utilities.getPreference(
                    BaseApplication.getContext(),
                    AppConstants.SELECTED_CATEGORY_ID
                )}

                Log.d(TAG, "load: filtered Response $filteredResponse")


                LoadResult.Page(
                    filteredResponse ?: ArrayList(),
                    prevKey = null,
                    nextKey = null
                )
            }
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        } catch (exception : Exception){
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Project>): Int? = null
}