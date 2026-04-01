package ru.netology.nmadia_hw.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.netology.nmadia_hw.api.PostsApiService
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.error.ApiError

class PostPagingSource(
    private val apiService: PostsApiService,
) : PagingSource<Long, Post>() {

    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        return try {
            val response = when (params) {
                is LoadParams.Refresh -> apiService.getLatest(params.loadSize)
                is LoadParams.Prepend -> return LoadResult.Page(
                    data = emptyList(),
                    prevKey = params.key,
                    nextKey = null,
                )
                is LoadParams.Append -> apiService.getBefore(
                    id = params.key,
                    count = params.loadSize,
                )
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val nextKey = if (body.isEmpty()) null else body.last().id

            LoadResult.Page(
                data = body,
                prevKey = params.key,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}