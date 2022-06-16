package me.ssttkkl.sharenote.ui.utils

import io.ktor.client.plugins.*
import io.ktor.http.*
import me.ssttkkl.sharenote.R

suspend inline fun MyViewModel.handleNoteError(
    e: Exception,
    crossinline handleElse: suspend (Exception) -> Unit
) {
    if (e is HttpRequestTimeoutException) {
        sendErrorMessage(R.string.message_request_timeout)
    } else if (e is ClientRequestException) {
        when (e.response.status) {
            HttpStatusCode.NotFound -> sendErrorMessage(R.string.message_note_not_exists)
            HttpStatusCode.Forbidden -> sendErrorMessage(R.string.message_note_permission_denied)
            HttpStatusCode.Conflict -> sendErrorMessage(R.string.message_note_version_conflict)
        }
    }
    handleElse.invoke(e)
}

suspend inline fun MyViewModel.handleNotePermissionError(
    e: Exception,
    crossinline handleElse: suspend (Exception) -> Unit
) {
    if (e is HttpRequestTimeoutException) {
        sendErrorMessage(R.string.message_request_timeout)
    } else if (e is ClientRequestException) {
        when (e.response.status) {
            HttpStatusCode.NotFound -> sendErrorMessage(R.string.message_note_permission_not_exists)
            HttpStatusCode.Forbidden -> sendErrorMessage(R.string.message_note_permission_denied)
        }
    }
    handleElse.invoke(e)
}