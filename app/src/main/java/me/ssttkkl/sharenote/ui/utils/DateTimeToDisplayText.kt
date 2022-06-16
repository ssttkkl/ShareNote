package me.ssttkkl.sharenote.ui.utils

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


private val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

fun LocalDateTime.toDisplayText(): String {
    return this.format(dtf)
}

fun OffsetDateTime.toDisplayText(): String {
    return this.toLocalDateTime().toDisplayText()
}