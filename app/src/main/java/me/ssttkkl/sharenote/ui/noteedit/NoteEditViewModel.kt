package me.ssttkkl.sharenote.ui.noteedit

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.data.entity.Note
import me.ssttkkl.sharenote.data.payload.NoteDto
import me.ssttkkl.sharenote.data.persist.entity.Draft
import me.ssttkkl.sharenote.data.repo.DraftRepository
import me.ssttkkl.sharenote.data.repo.NoteRepository
import me.ssttkkl.sharenote.ui.utils.MyViewModel
import me.ssttkkl.sharenote.ui.utils.handleNoteError
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val noteRepo: NoteRepository,
    private val draftRepo: DraftRepository,
) : MyViewModel() {

    private var initialized = false

    var noteID: Int = 0
        private set

    private var note: Note? = null

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun setNoteID(noteID: Int = 0) {
        if (!initialized) {
            this.noteID = noteID
            initialized = true

            viewModelScope.launch {
                _loading.value = true

                note = noteRepo.getNoteByID(noteID)

                title.value = note?.title ?: ""
                content.value = note?.content ?: ""
                tags.value =
                    note?.tags?.let { TagItemModels.build(it.toList()) } ?: TagItemModels.EMPTY

                _loading.value = false
            }
        }
    }

    val title = MutableStateFlow("")
    val content = MutableStateFlow("")
    val tags = MutableStateFlow(TagItemModels.EMPTY)

    val modified = title.flatMapLatest { title ->
        content.map { content ->
            title != note?.title || content != note?.content
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _conflict = MutableSharedFlow<Unit>()
    val conflict: Flow<Unit> = _conflict

    fun handleClickTag(item: TagItemModel.Tag) {
        if (!item.showRemove) {
            tags.value = tags.value.rebuild(item.name)
        } else {
            tags.value = tags.value.minus(item)
        }
    }

    fun saveTag(tag: String): Boolean {
        if (tags.value.any { it is TagItemModel.Tag && it.name == tag })
            return false
        tags.value = tags.value.plus(tag)
        return true
    }

    fun save(ignoreConflict: Boolean = false) {
        if (!initialized || _loading.value)
            return

        viewModelScope.launch {
            _loading.value = true

            try {
                var dto = NoteDto(
                    title = title.value,
                    content = content.value,
                )

                var note = note
                if (note == null) {
                    note = noteRepo.createNote(dto)
                } else if (modified.value) {
                    dto = dto.copy(
                        id = note.id,
                        version = note.version,
                    )
                    note = noteRepo.modifyNote(note.id, dto, ignoreConflict)
                }

                val tags = tags.value.filterIsInstance<TagItemModel.Tag>()
                    .map { it.name }
                    .toSet()
                if (tags.isNotEmpty()) {
                    noteRepo.setNoteTags(note.id, tags)
                }

                sendFinishSignal()
            } catch (e: Exception) {
                if (e is ClientRequestException && e.response.status == HttpStatusCode.Conflict) {
                    _conflict.emit(Unit)
                } else {
                    handleNoteError(e) {
                        sendErrorMessage(R.string.message_unknown_error)
                        Log.e(TAG, e.message, e)
                    }
                }
                _loading.value = false
            }
        }
    }

    fun saveAsDraft() {
        viewModelScope.launch {
            val draft = Draft(
                id = 0,
                title = title.value,
                content = content.value
            )
            draftRepo.insertDraft(draft)
            sendFinishSignal()
        }
    }

    fun applyDraft(draft: Draft) {
        title.value = draft.title
        content.value = draft.content
    }

    companion object {
        val TAG
            get() = NoteEditViewModel::class.simpleName
    }
}