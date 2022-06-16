package me.ssttkkl.sharenote.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class NoteSummary(
    val id: Int = 0,
    val title: String,
    val ownerUser: User,
) : Parcelable