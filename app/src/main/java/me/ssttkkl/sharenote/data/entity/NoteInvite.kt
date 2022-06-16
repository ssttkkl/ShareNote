package me.ssttkkl.sharenote.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import me.ssttkkl.sharenote.data.serializer.OffsetDateTimeKSerializer
import java.time.OffsetDateTime

@Serializable
@Parcelize
data class NoteInvite(
    val id: String,
    val readonly: Boolean,
    @Serializable(with = OffsetDateTimeKSerializer::class)
    val createdAt: OffsetDateTime,
    @Serializable(with = OffsetDateTimeKSerializer::class)
    val expiresAt: OffsetDateTime,
    val note: NoteSummary? = null
) : Parcelable