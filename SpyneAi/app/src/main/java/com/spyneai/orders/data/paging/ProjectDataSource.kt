package com.spyneai.orders.data.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.google.gson.Gson
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.ProjectApi
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.paging.PagedRepository.Companion.DEFAULT_PAGE_INDEX
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.shootapp.repository.model.project.Project

import retrofit2.HttpException
import java.io.IOException

@ExperimentalPagingApi
class ProjectDataSource(
    private val service: ProjectApi,
    val SpyneAppDatabase: SpyneAppDatabase,
    val status: String,
    val shootType: String
) : PagingSource<Int, Project>() {

    val TAG = "ProjectDataSource"

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Project> {
        val page = params.key ?: DEFAULT_PAGE_INDEX
        Log.d(TAG, "load: $page")
        //val apiQuery = query + IN_QUALIFIER

        return try {
            if (BaseApplication.getContext().isInternetActive()) {
                val response = service.getPagedProjects(
                    pageNo = page,
                    status = status,
                    shootType = shootType
                )

                Log.d(TAG, "load: ${response.data}")
                Log.d(TAG, "load: ${response.data.isNullOrEmpty()}")
                Log.d(
                    TAG,
                    "load: team id ${
                        Utilities.getPreference(
                            BaseApplication.getContext(),
                            AppConstants.TEAM_ID
                        )
                    }"
                )

                Log.d(
                    TAG,
                    "load: selected category ${
                        Utilities.getPreference(
                            BaseApplication.getContext(),
                            AppConstants.SELECTED_CATEGORY_ID
                        )
                    }"
                )
                val prevKey = if (page == DEFAULT_PAGE_INDEX) null else page - 1
                val nextKey = if (response.data.isNullOrEmpty()) null else page + 1

                SpyneAppDatabase.withTransaction {
                    val ss = SpyneAppDatabase.projectDao().insertWithCheck(response.data)
                    Log.d(TAG, "load: ${Gson().toJson(ss)}")
                }

                val finalResponse = if (shootType == "Inspection") {
                    if (status == "all") SpyneAppDatabase.projectDao().getInspectionProjectWithOutFilter(
                        offset = page.times(AppConstants.FETCH_PROJECT_LIMIT),
                        limit = AppConstants.FETCH_PROJECT_LIMIT
                    ) else SpyneAppDatabase.projectDao().getInspectionProjectsWithLimitAndSkip(
                        offset = page.times(AppConstants.FETCH_PROJECT_LIMIT),
                        status = status,
                        limit = AppConstants.FETCH_PROJECT_LIMIT
                    )
                } else {
                    if (status == "all") if (Utilities.getPreference(
                            BaseApplication.getContext(),
                            AppConstants.TEAM_ID
                        ).isNullOrEmpty()
                    ) SpyneAppDatabase.projectDao().getProjectWithOutFilterTeamIdNull(
                        offset = page.times(AppConstants.FETCH_PROJECT_LIMIT),
                        limit = AppConstants.FETCH_PROJECT_LIMIT
                    ) else SpyneAppDatabase.projectDao().getProjectWithOutFilter(
                        offset = page.times(AppConstants.FETCH_PROJECT_LIMIT),
                        limit = AppConstants.FETCH_PROJECT_LIMIT
                    )
                    else if (Utilities.getPreference(
                            BaseApplication.getContext(),
                            AppConstants.TEAM_ID
                        ).isNullOrEmpty()
                    ) SpyneAppDatabase.projectDao().getProjectsWithLimitAndSkipTeamIdNull(
                        offset = page.times(AppConstants.FETCH_PROJECT_LIMIT),
                        status = status,
                        limit = AppConstants.FETCH_PROJECT_LIMIT
                    )
                    else SpyneAppDatabase.projectDao().getProjectsWithLimitAndSkip(
                        offset = page.times(AppConstants.FETCH_PROJECT_LIMIT),
                        status = status,
                        limit = AppConstants.FETCH_PROJECT_LIMIT
                    )
                }

                Log.d(TAG, "load: finalResponse $finalResponse")

                var filteredResponse = finalResponse?.filter { it.categoryId ==  Utilities.getPreference(
                    BaseApplication.getContext(),
                    AppConstants.SELECTED_CATEGORY_ID
                )}

                Log.d(TAG, "load: filtered Response $filteredResponse")


                LoadResult.Page(
                    filteredResponse ?: ArrayList(),
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            } else {
                val response = if (shootType == "Inspection") {
                    if (status == "all") SpyneAppDatabase.projectDao().getInspectionProjectWithOutFilter(
                        offset = page.times(AppConstants.FETCH_PROJECT_LIMIT),
                        limit = AppConstants.FETCH_PROJECT_LIMIT
                    ) else SpyneAppDatabase.projectDao().getInspectionProjectsWithLimitAndSkip(
                        offset = page.times(AppConstants.FETCH_PROJECT_LIMIT),
                        status = status,
                        limit = AppConstants.FETCH_PROJECT_LIMIT
                    )
                } else {
                    if (status == "all") if (Utilities.getPreference(
                            BaseApplication.getContext(),
                            AppConstants.TEAM_ID
                        ).isNullOrEmpty()
                    ) SpyneAppDatabase.projectDao().getProjectWithOutFilterTeamIdNull(
                        offset = page.times(AppConstants.FETCH_PROJECT_LIMIT),
                        limit = AppConstants.FETCH_PROJECT_LIMIT
                    ) else SpyneAppDatabase.projectDao().getProjectWithOutFilter(
                        offset = page.times(AppConstants.FETCH_PROJECT_LIMIT),
                        limit = AppConstants.FETCH_PROJECT_LIMIT
                    )
                    else if (Utilities.getPreference(
                            BaseApplication.getContext(),
                            AppConstants.TEAM_ID
                        ).isNullOrEmpty()
                    ) SpyneAppDatabase.projectDao().getProjectsWithLimitAndSkipTeamIdNull(
                        offset = page.times(AppConstants.FETCH_PROJECT_LIMIT),
                        status = status,
                        limit = AppConstants.FETCH_PROJECT_LIMIT
                    )
                    else SpyneAppDatabase.projectDao().getProjectsWithLimitAndSkip(
                        offset = page.times(AppConstants.FETCH_PROJECT_LIMIT),
                        status = status,
                        limit = AppConstants.FETCH_PROJECT_LIMIT
                    )
                }

                Log.d(TAG, "load: response $response")

                val prevKey = if (page == DEFAULT_PAGE_INDEX) null else page - 1
                val nextKey = if (response.isNullOrEmpty()) null else page + 1

                var filteredResponse = response?.filter { it.categoryId ==  Utilities.getPreference(
                    BaseApplication.getContext(),
                    AppConstants.SELECTED_CATEGORY_ID
                )}

                Log.d(TAG, "load: filtered Response $filteredResponse")

                LoadResult.Page(
                    filteredResponse ?: ArrayList(),
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Project>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}