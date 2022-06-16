package me.ssttkkl.sharenote.data.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import me.ssttkkl.sharenote.data.serializer.OffsetDateTimeKSerializer
import java.time.OffsetDateTime

@Serializable
@Parcelize
@Entity
data class Note(
    val id: Int = 0,
    val title: String,
    val content: String,
    val tags: Set<String>,
    val version: Int,
    val ownerUser: User,
    val modifiedBy: User,
    val permission: NotePermission,
    @Serializable(with = OffsetDateTimeKSerializer::class)
    val createdAt: OffsetDateTime,
    @Serializable(with = OffsetDateTimeKSerializer::class)
    val modifiedAt: OffsetDateTime,
) : Parcelable