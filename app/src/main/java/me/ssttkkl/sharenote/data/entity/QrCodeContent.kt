package me.ssttkkl.sharenote.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.ssttkkl.sharenote.data.serializer.OffsetDateTimeKSerializer
import java.time.OffsetDateTime


@Serializable
sealed class QrCodeContent {
    abstract val version: Int
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@Parcelize
@SerialName("NoteInvite")
data class NoteInviteQrCodeContent(
    val inviteID: String,
    @Serializable(with = OffsetDateTimeKSerializer::class)
    val expiresAt: OffsetDateTime,
    @EncodeDefault override val version: Int = 1
) : QrCodeContent(), Parcelable