package me.ssttkkl.sharenote.data.persist.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.ssttkkl.sharenote.data.persist.entity.Draft

@Dao
interface DraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notes: Collection<Draft>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Draft)

    @Query("DELETE FROM draft WHERE draft_id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM draft")
    fun deleteAll(): Int

    @Query("SELECT * FROM draft WHERE draft_id = :id")
    suspend fun findById(id: Int): Draft?

    @Query("SELECT * FROM draft WHERE draft_id = :id")
    fun findByIdAsFlow(id: Int): Flow<Draft?>

    @Query("SELECT * FROM draft")
    fun findAll(): List<Draft>

    @Query("SELECT * FROM draft")
    fun findAllAsPagingSource(): PagingSource<Int, Draft>
}