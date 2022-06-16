package me.ssttkkl.sharenote.ui.consumeinvite

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.data.entity.NoteInvite
import me.ssttkkl.sharenote.data.repo.NoteInviteRepository
import java.time.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class ConsumeInviteViewModel @Inject constructor(
    private val app: Application,
    private val noteInviteRepo: NoteInviteRepository
) : ViewModel() {

    val inviteID = MutableStateFlow<String?>(null)

    private var invite: NoteInvite? = null

    private val _state = MutableStateFlow(ViewState(true))
    val state: StateFlow<ViewState> = _state

    init {
        viewModelScope.launch {
            inviteID.collectLatest { inviteID ->
                if (inviteID != null) {
                    _state.value = ViewState(true)

                    invite = noteInviteRepo.getInvite(inviteID)
                    _state.value = ViewState(
                        title = invite!!.note!!.title,
                        owner = invite!!.note!!.ownerUser.nickname,
                        permission = app.getString(
                            if (invite!!.readonly)
                                R.string.readonly
                            else
                                R.string.readwrite
                        )
                    )
                }
            }
        }
    }

    private val _finishMessage = Channel<String>(Channel.BUFFERED)
    val finishMessage: Flow<String> = _finishMessage.receiveAsFlow()

    fun consume() {
        viewModelScope.launch {
            val invite = invite
            if (invite == null) {
                _finishMessage.send(app.getString(R.string.messageInvalidInvite))
                return@launch
            }

            if (invite.expiresAt.isBefore(OffsetDateTime.now())) {
                _finishMessage.send(app.getString(R.string.messageExpiredInvite))
                return@launch
            }

            noteInviteRepo.consumeInvite(invite.id)
            _finishMessage.send(app.getString(R.string.messageInviteConsumed))
        }
    }
}