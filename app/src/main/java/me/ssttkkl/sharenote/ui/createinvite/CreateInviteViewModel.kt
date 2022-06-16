package me.ssttkkl.sharenote.ui.createinvite

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.data.entity.NoteInviteQrCodeContent
import me.ssttkkl.sharenote.data.entity.QrCodeContent
import me.ssttkkl.sharenote.data.repo.NoteInviteRepository
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CreateInviteViewModel @Inject constructor(
    private val app: Application,
    private val noteInviteRepo: NoteInviteRepository
) : ViewModel() {

    private val formatter = DateTimeFormatter.ofPattern(app.getString(R.string.data_time_formatter))

    data class Arguments(
        val noteID: Int,
        val readonly: Boolean
    )

    val arguments = MutableStateFlow(Arguments(0, false))

    private val _state = MutableStateFlow(ViewState())
    val state: StateFlow<ViewState>
        get() = _state

    init {
        viewModelScope.launch {
            arguments.collectLatest { args ->
                if (args.noteID != 0) {
                    viewModelScope.launch {
                        _state.value = ViewState(true)

                        val invite = noteInviteRepo.createInvite(args.noteID, args.readonly)

                        withContext(Dispatchers.Default) {
                            val content = Json.encodeToString<QrCodeContent>(
                                NoteInviteQrCodeContent(
                                    invite.id,
                                    invite.expiresAt
                                )
                            )
                            val qr = QrCodeGenerator.generate(content, 256)

                            val timeText = invite.expiresAt.format(formatter)
                            val expiresAt =
                                app.getString(R.string.expires_at_format).format(timeText)

                            _state.value = ViewState(false, qr, expiresAt)
                        }
                    }
                }
            }
        }
    }
}