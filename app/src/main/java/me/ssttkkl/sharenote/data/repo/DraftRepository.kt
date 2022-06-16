package me.ssttkkl.sharenote.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.ssttkkl.sharenote.data.persist.AppDatabase
import me.ssttkkl.sharenote.data.persist.entity.Draft
import javax.inject.Inject
import javax.inject.Singleton

interface DraftRepository {
    fun getDrafts(): Flow<PagingData<Draft>>
    suspend fun insertDraft(draft: Draft)
    suspend fun deleteDraft(draftID: Int)
}

@Singleton
internal class DraftRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
) : DraftRepository {
    override fun getDrafts(): Flow<PagingData<Draft>> {
        val pager = Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { db.draftDao.findAllAsPagingSource() }
        )
        return pager.flow
    }

    override suspend fun insertDraft(draft: Draft) {
        db.draftDao.insert(draft)
    }

    override suspend fun deleteDraft(draftID: Int) {
        db.draftDao.deleteById(draftID)
    }
}