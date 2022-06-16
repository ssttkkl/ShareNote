package me.ssttkkl.sharenote.data.persist.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.ssttkkl.sharenote.data.persist.entity.PersistNoteRemoteKey

@Dao
interface PersistNoteRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notes: Collection<PersistNoteRemoteKey>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: PersistNoteRemoteKey)

    @Query("DELETE FROM note_remote_key WHERE note_id = :id")
    suspend fun deleteByNoteId(id: Int)

    @Query("DELETE FROM note")
    fun deleteAll(): Int

    @Query("SELECT * FROM note_remote_key WHERE note_id = :id")
    suspend fun findById(id: Int): PersistNoteRemoteKey?

    @Query("SELECT * FROM note_remote_key WHERE note_id = :id")
    fun findByIdAsFlow(id: Int): Flow<PersistNoteRemoteKey?>

    @Query("SELECT * FROM note_remote_key")
    fun findAll(): List<PersistNoteRemoteKey>

    @Query("SELECT * FROM note_remote_key")
    fun findAllAsPagingSource(): PagingSource<Int, PersistNoteRemoteKey>
}