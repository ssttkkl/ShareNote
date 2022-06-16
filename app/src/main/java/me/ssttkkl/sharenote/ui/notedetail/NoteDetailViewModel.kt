package me.ssttkkl.sharenote.ui.notedetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.data.entity.isDeleted
import me.ssttkkl.sharenote.data.repo.AuthRepository
import me.ssttkkl.sharenote.data.repo.NoteRepository
import me.ssttkkl.sharenote.ui.home.HomeViewModel
import me.ssttkkl.sharenote.ui.utils.MyViewModel
import me.ssttkkl.sharenote.ui.utils.handleNoteError
import me.ssttkkl.sharenote.ui.utils.toDisplayText
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val app: Application,
    private val authRepo: AuthRepository,
    private val noteRepo: NoteRepository
) : MyViewModel() {

    val noteID = MutableStateFlow(0)

    private val refreshSignal = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)  // ensure first signal to trigger loading
    }

    private val note = refreshSignal.flatMapLatest {
        noteID.flatMapLatest { noteID ->
            noteRepo.getNoteByIDAsFlow(noteID)
        }
    }

    val noteState = note.map {
        val isDetached = it?.permission?.isDeleted == true
        val isOwner = it?.ownerUser?.id == authRepo.user.value?.id
        val isReadonly = isDetached || it?.permission?.readonly == true

        NoteViewState(
            title = it?.title ?: "",
            content = it?.content ?: "",
            tags = it?.tags?.joinToString(" ") { tag -> "#$tag" } ?: "",
            owner = it?.ownerUser?.nickname ?: "",
            modifiedBy = it?.modifiedBy?.nickname ?: "",
            permission = app.getString(
                if (isDetached)
                    R.string.hint_note_detached
                else if (isOwner)
                    R.string.owner
                else if (isReadonly)
                    R.string.readonly
                else
                    R.string.readwrite
            ),
            createdAt = it?.createdAt?.toDisplayText() ?: "",
            modifiedAt = it?.modifiedAt?.toDisplayText() ?: "",
            isOwner = isOwner,
            isReadonly = isReadonly,
            isDetached = isDetached,
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, NoteViewState())

    fun refresh() {
        viewModelScope.launch {
            refreshSignal.emit(Unit)
        }
    }

    fun removeOwnNote() {
        viewModelScope.launch {
            try {
                noteRepo.deleteNote(noteID.value)
                sendFinishSignal()
            } catch (e: Exception) {
                handleNoteError(e) {
                    sendErrorMessage(R.string.message_unknown_error)
                    Log.e(HomeViewModel.TAG, e.message, e)
                }
            }
        }
    }

    fun removeNote() {
        viewModelScope.launch {
            try {
                noteRepo.deleteSelfNotePermission(noteID.value)
                sendFinishSignal()
            } catch (e: Exception) {
                handleNoteError(e) {
                    sendErrorMessage(R.string.message_unknown_error)
                    Log.e(HomeViewModel.TAG, e.message, e)
                }
            }
        }
    }

    companion object {
        val TAG
            get() = NoteDetailViewModel::class.simpleName
    }
}