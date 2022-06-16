package me.ssttkkl.sharenote.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.ssttkkl.sharenote.data.entity.NotePermissionWithUser
import me.ssttkkl.sharenote.data.payload.Pageable
import me.ssttkkl.sharenote.data.service.PrivateServiceImpl
import javax.inject.Inject

interface NotePermissionRepository {

    suspend fun getNotePermissions(noteID: Int): Flow<PagingData<NotePermissionWithUser>>

    suspend fun modifyNotePermission(noteID: Int, userID: Int, readonly: Boolean)
    suspend fun deleteNotePermission(noteID: Int, userID: Int)
}

internal class NotePermissionRepositoryImpl @Inject constructor(
    private val service: PrivateServiceImpl
) : NotePermissionRepository {
    override suspend fun getNotePermissions(noteID: Int): Flow<PagingData<NotePermissionWithUser>> {
        val pageSize = 20
        return Pager(config = PagingConfig(pageSize)) {
            RemotePagingSource { page ->
                service.getNotePermissions(noteID, Pageable(page * pageSize, pageSize))
            }
        }.flow
    }

    override suspend fun modifyNotePermission(noteID: Int, userID: Int, readonly: Boolean) =
        service.modifyNotePermission(noteID, userID, readonly)

    override suspend fun deleteNotePermission(noteID: Int, userID: Int) =
        service.deleteNotePermission(noteID, userID)

}