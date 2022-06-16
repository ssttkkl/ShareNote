package me.ssttkkl.sharenote.data.persist.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "note_tag", primaryKeys = ["note_id", "tag_name"])
data class PersistNoteTag(
    @ColumnInfo(name = "note_id")
    val noteID: Int,
    @ColumnInfo(name = "tag_name")
    val tagName: String
)