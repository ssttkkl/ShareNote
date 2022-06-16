package me.ssttkkl.sharenote.data.persist.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_remote_key")
data class PersistNoteRemoteKey(
    @PrimaryKey
    @ColumnInfo(name = "note_id")
    val id: Int,
    @ColumnInfo(name = "next_key")
    val nextKey: Int?,
)