package me.ssttkkl.sharenote.data.payload

import kotlinx.serialization.Serializable

@Serializable
data class RefreshPayload(val refreshToken: String)