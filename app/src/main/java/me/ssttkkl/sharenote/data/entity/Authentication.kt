package me.ssttkkl.sharenote.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Authentication(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: User
) : Parcelable