package me.ssttkkl.sharenote.data.persist.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.ssttkkl.sharenote.data.persist.entity.PersistNoteTag

@Dao
interface PersistNoteTagDao {
    @Insert
    suspend fun insert(noteTags: List<PersistNoteTag>)

    @Insert
    suspend fun insert(noteTag: PersistNoteTag)

    @Delete
    suspend fun delete(noteTag: PersistNoteTag)

    @Query("DELETE FROM note_tag")
    suspend fun deleteAll()

    @Query("DELETE FROM note_tag WHERE note_id = :noteID")
    suspend fun deleteByNoteId(noteID: Int): Int

    @Query("SELECT * FROM note_tag WHERE note_id = :noteID")
    suspend fun findByNoteID(noteID: Int): List<PersistNoteTag>

    @Query("SELECT * FROM note_tag WHERE note_id = :noteID")
    fun findByNoteIdAsFlow(noteID: Int): Flow<List<PersistNoteTag>>
}