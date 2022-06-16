package me.ssttkkl.sharenote.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.data.entity.QrCodeContent
import me.ssttkkl.sharenote.data.entity.isDeleted
import me.ssttkkl.sharenote.data.repo.AuthRepository
import me.ssttkkl.sharenote.data.repo.NoteRepository
import me.ssttkkl.sharenote.ui.utils.MyViewModel
import me.ssttkkl.sharenote.ui.utils.handleNoteError
import me.ssttkkl.sharenote.ui.utils.toDisplayText
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val app: Application,
    private val authRepo: AuthRepository,
    private val noteRepo: NoteRepository,
) : MyViewModel() {

    private val user = authRepo.user

    val userState = user.map { user ->
        UserViewState(
            isLoggedIn = user != null,
            nickname = user?.nickname
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, UserViewState())

    private val refreshSignal = noteRepo.datasetUpdatedSignal

    @OptIn(ExperimentalCoroutinesApi::class)
    private val notes = refreshSignal.flatMapLatest {
        noteRepo.getNotes()
    }

    val noteModels = notes.map { page ->
        page.map {
            NoteItemModel(
                id = it.id,
                title = it.title,
                content = it.content,
                modifiedAt = it.modifiedAt.toDisplayText(),
                isOwner = it.ownerUser.id == user.value?.id,
                isReadonly = it.permission.readonly,
                isDetached = it.permission.isDeleted
            )
        }
    }.cachedIn(viewModelScope)

    fun removeOwnNote(noteID: Int) {
        viewModelScope.launch {
            try {
                noteRepo.deleteNote(noteID)
            } catch (e: Exception) {
                handleNoteError(e) {
                    sendErrorMessage(R.string.message_unknown_error)
                    Log.e(TAG, e.message, e)
                }
            }
        }
    }

    fun removeNote(noteID: Int) {
        viewModelScope.launch {
            try {
                noteRepo.deleteSelfNotePermission(noteID)
            } catch (e: Exception) {
                handleNoteError(e) {
                    sendErrorMessage(R.string.message_unknown_error)
                    Log.e(TAG, e.message, e)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
        }
    }

    private val _qrCodeContent = Channel<QrCodeContent>(Channel.BUFFERED)
    val qrCodeContent = _qrCodeContent.receiveAsFlow()

    fun parseQrCode(text: String) {
        viewModelScope.launch {
            try {
                val content = Json.decodeFromString<QrCodeContent>(text)
                _qrCodeContent.send(content)
            } catch (e: SerializationException) {
                sendErrorMessage(R.string.message_invalid_qr_code)
            } catch (e: Exception) {
                sendErrorMessage(R.string.message_unknown_error)
                Log.e(TAG, e.message, e)
            }
        }
    }

    companion object {
        val TAG
            get() = HomeViewModel::class.simpleName
    }
}