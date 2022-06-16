package me.ssttkkl.sharenote.data.persist.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.ssttkkl.sharenote.data.entity.Note
import me.ssttkkl.sharenote.data.entity.User
import java.time.OffsetDateTime

@Entity(tableName = "note")
data class PersistNote(
    @PrimaryKey
    @ColumnInfo(name = "note_id")
    val id: Int = 0,
    val title: String,
    @ColumnInfo(name = "draft_id", typeAffinity = ColumnInfo.TEXT)
    val content: String,
    @ColumnInfo(name = "owner_user_id")
    val ownerUserID: Int,
    @ColumnInfo(name = "modified_by_user_id")
    val modifiedByUserID: Int,
    val version: Int,
    @Embedded
    val permission: PersistNotePermission,
    @ColumnInfo(name = "created_at")
    val createdAt: OffsetDateTime,
    @ColumnInfo(name = "modified_at")
    val modifiedAt: OffsetDateTime,
)

fun PersistNote.toNote(tags: Set<String>, ownerUser: User, modifiedByUser: User) = Note(
    id = this.id,
    title = this.title,
    content = this.content,
    tags = tags,
    version = this.version,
    ownerUser = ownerUser,
    modifiedBy = modifiedByUser,
    permission = this.permission.toNotePermission(),
    createdAt = this.createdAt,
    modifiedAt = this.modifiedAt
)

fun Note.toPersistNote() = PersistNote(
    id = this.id,
    title = this.title,
    content = this.content,
    ownerUserID = this.ownerUser.id,
    modifiedByUserID = this.modifiedBy.id,
    permission = this.permission.toPersistNotePermission(),
    version = this.version,
    createdAt = this.createdAt,
    modifiedAt = this.modifiedAt,
)