package me.ssttkkl.sharenote.ui.notepermission

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.data.repo.NotePermissionRepository
import me.ssttkkl.sharenote.ui.utils.MyViewModel
import me.ssttkkl.sharenote.ui.utils.handleNotePermissionError
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotePermissionViewModel @Inject constructor(
    private val permissionRepo: NotePermissionRepository,
) : MyViewModel() {

    val noteID = MutableStateFlow(0)

    val permissions = noteID.flatMapLatest {
        if (it > 0)
            permissionRepo.getNotePermissions(it)
        else
            emptyFlow()
    }.mapLatest { page ->
        page.map {
            PermissionItemModel(
                id = it.user.id,
                user = it.user.nickname,
                readonly = it.readonly
            )
        }
    }.cachedIn(viewModelScope)

    val refreshSignal = MutableSharedFlow<Unit>()

    fun setReadonly(userID: Int, readonly: Boolean) {
        viewModelScope.launch {
            try {
                permissionRepo.modifyNotePermission(noteID.value, userID, readonly)
                refreshSignal.emit(Unit)
            } catch (e: Exception) {
                handleNotePermissionError(e) {
                    sendErrorMessage(R.string.message_unknown_error)
                    Log.e(TAG, e.message, e)
                }
            }
        }
    }

    fun remove(userID: Int) {
        viewModelScope.launch {
            try {
                permissionRepo.deleteNotePermission(noteID.value, userID)
                refreshSignal.emit(Unit)
            } catch (e: Exception) {
                handleNotePermissionError(e) {
                    sendErrorMessage(R.string.message_unknown_error)
                    Log.e(TAG, e.message, e)
                }
            }
        }
    }

    companion object {
        val TAG
            get() = NotePermissionViewModel::class.simpleName
    }
}