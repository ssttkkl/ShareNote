package me.ssttkkl.sharenote.data.repo

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import me.ssttkkl.sharenote.data.entity.Note
import me.ssttkkl.sharenote.data.entity.Page
import me.ssttkkl.sharenote.data.payload.Pageable
import me.ssttkkl.sharenote.data.persist.entity.PersistNote
import me.ssttkkl.sharenote.data.persist.persister.NotePersister

@OptIn(ExperimentalPagingApi::class)
internal class NoteRemoteMediator(
    private val persister: NotePersister,
    private val remote: (suspend (Pageable) -> Page<Note>)
) : RemoteMediator<Int, PersistNote>() {

//    private var lastUpdatedAt = Instant.MIN
//
//    override suspend fun initialize(): InitializeAction {
//        val cacheTimeout = 60
//        return if (Instant.now().epochSecond - lastUpdatedAt.epochSecond < cacheTimeout) {
//            InitializeAction.SKIP_INITIAL_REFRESH
//        } else {
//            InitializeAction.LAUNCH_INITIAL_REFRESH
//        }
//    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PersistNote>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    getRemoteNextKeyClosestToCurrentPosition(state)?.minus(1) ?: 0
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(true)
                }
                LoadType.APPEND -> {
                    getRemoteNextKeyForLastItem(state)
                        ?: return MediatorResult.Success(true)
                }
            }

            val size = state.config.pageSize
            Log.d(
                "NoteRemoteMediator",
                "load next page (loadType=$loadType, page=$page, size=$size)"
            )
            val response = remote(Pageable(page, size))
            Log.d(
                "NoteRemoteMediator",
                "loaded next page (loadType=$loadType, page=${response.page}, size=${response.pageSize}, totalPages=${response.totalPages}, totalElements=${response.totalElements})"
            )

            persister.persist(response, clearBefore = loadType == LoadType.REFRESH)
            MediatorResult.Success(endOfPaginationReached = response.isLastPage)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteNextKeyForLastItem(state: PagingState<Int, PersistNote>): Int? {
        return state.lastItemOrNull()?.let { item ->
            persister.getRemoteNextKey(item.id)
        }
    }

    private suspend fun getRemoteNextKeyClosestToCurrentPosition(state: PagingState<Int, PersistNote>): Int? {
        // check on which scroll position we are currently, which item it is
        // and find its corresponding RemoteKey object so we can see which page is a current page.

        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                persister.getRemoteNextKey(id)
            }
        }
    }
}