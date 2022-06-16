package me.ssttkkl.sharenote.data.payload

import kotlinx.serialization.Serializable

@Serializable
data class LoginPayload(
    val username: String,
    val password: String
)