package me.ssttkkl.sharenote.data.persist.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.ssttkkl.sharenote.data.persist.entity.PersistNote

@Dao
interface PersistNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notes: Collection<PersistNote>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: PersistNote)

    @Query("DELETE FROM note WHERE note_id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM note")
    fun deleteAll(): Int

    @Query("SELECT * FROM note WHERE note_id = :id")
    suspend fun findById(id: Int): PersistNote?

    @Query("SELECT * FROM note WHERE note_id = :id")
    fun findByIdAsFlow(id: Int): Flow<PersistNote?>

    @Query("SELECT * FROM note")
    fun findAll(): List<PersistNote>

    @Query("SELECT * FROM note")
    fun findAllAsPagingSource(): PagingSource<Int, PersistNote>
}