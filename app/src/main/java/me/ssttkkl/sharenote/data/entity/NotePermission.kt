package me.ssttkkl.sharenote.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import me.ssttkkl.sharenote.data.serializer.OffsetDateTimeKSerializer
import java.time.OffsetDateTime

@Serializable
@Parcelize
data class NotePermission(
    val readonly: Boolean,
    @Serializable(with = OffsetDateTimeKSerializer::class)
    val createdAt: OffsetDateTime,
    @Serializable(with = OffsetDateTimeKSerializer::class)
    val deletedAt: OffsetDateTime? = null,
) : Parcelable

val NotePermission.isDeleted
    get() = deletedAt != null

@Serializable
@Parcelize
data class NotePermissionWithUser(
    val user: User,
    val readonly: Boolean,
    @Serializable(with = OffsetDateTimeKSerializer::class)
    val createdAt: OffsetDateTime,
    @Serializable(with = OffsetDateTimeKSerializer::class)
    val deletedAt: OffsetDateTime? = null,
) : Parcelable
