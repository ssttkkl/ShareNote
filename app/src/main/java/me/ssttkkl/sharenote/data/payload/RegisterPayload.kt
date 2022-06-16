package me.ssttkkl.sharenote.data.payload

import kotlinx.serialization.Serializable

@Serializable
data class RegisterPayload(
    val username: String,
    val password: String,
    val nickname: String,
)