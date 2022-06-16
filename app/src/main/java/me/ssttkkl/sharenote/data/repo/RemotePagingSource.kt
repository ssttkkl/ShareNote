package me.ssttkkl.sharenote.data.repo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import me.ssttkkl.sharenote.data.entity.Page

internal class RemotePagingSource<T : Any>(val remote: suspend (page: Int) -> Page<T>) :
    PagingSource<Int, T>() {
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, T> {
        return try {
            val nextPageNumber = params.key ?: 0
            val response = remote(nextPageNumber)
            LoadResult.Page(
                data = response.content,
                prevKey = null, // Only paging forward.
                nextKey = if (response.isLastPage) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}