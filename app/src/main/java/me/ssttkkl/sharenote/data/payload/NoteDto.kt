package me.ssttkkl.sharenote.data.payload

import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: Int = 0,
    val title: String,
    val content: String,
    val version: Int = 0,
)