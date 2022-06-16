package me.ssttkkl.sharenote.data.persist.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.ssttkkl.sharenote.data.persist.entity.PersistUser

@Dao
interface PersistUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notes: Collection<PersistUser>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: PersistUser)

    @Query("DELETE FROM user WHERE user_id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM user")
    fun deleteAll(): Int

    @Query("SELECT * FROM user WHERE user_id = :id")
    suspend fun findById(id: Int): PersistUser?

    @Query("SELECT * FROM user WHERE user_id = :id")
    fun findByIdAsFlow(id: Int): Flow<PersistUser?>

    @Query("SELECT * FROM user")
    fun findAll(): List<PersistUser>

    @Query("SELECT * FROM user")
    fun findAllAsPagingSource(): PagingSource<Int, PersistUser>
}