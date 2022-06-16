package me.ssttkkl.sharenote.data.persist.entity

import androidx.room.ColumnInfo
import me.ssttkkl.sharenote.data.entity.NotePermission
import java.time.OffsetDateTime

data class PersistNotePermission(
    val readonly: Boolean,
    @ColumnInfo(name = "permission_created_at")
    val createdAt: OffsetDateTime,
    @ColumnInfo(name = "permission_deleted_at")
    val deletedAt: OffsetDateTime? = null,
)

fun PersistNotePermission.toNotePermission() = NotePermission(
    readonly = readonly,
    createdAt = createdAt,
    deletedAt = deletedAt
)

fun NotePermission.toPersistNotePermission() = PersistNotePermission(
    readonly = readonly,
    createdAt = createdAt,
    deletedAt = deletedAt
)