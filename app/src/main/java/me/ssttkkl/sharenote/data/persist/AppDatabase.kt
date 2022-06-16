package me.ssttkkl.sharenote.data.persist

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.ssttkkl.sharenote.data.persist.converter.OffsetDatetimeConverter
import me.ssttkkl.sharenote.data.persist.dao.*
import me.ssttkkl.sharenote.data.persist.entity.*

@TypeConverters(
    OffsetDatetimeConverter::class
)
@Database(
    entities = [PersistNote::class, PersistUser::class,
        PersistNoteTag::class, PersistNoteRemoteKey::class,
        Draft::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract val noteDao: PersistNoteDao
    abstract val userDao: PersistUserDao
    abstract val noteTagDao: PersistNoteTagDao
    abstract val noteRemoteKeyDao: PersistNoteRemoteKeyDao
    abstract val draftDao: DraftDao
}
