package me.ssttkkl.sharenote.ui.login

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.data.payload.LoginPayload
import me.ssttkkl.sharenote.data.repo.AuthRepository
import me.ssttkkl.sharenote.ui.utils.MyViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepo: AuthRepository
) : MyViewModel() {

    val username = MutableStateFlow("")
    val password = MutableStateFlow("")
    val loading = MutableStateFlow(false)

    private fun validateUsername(username: String): Boolean {
        return username.length >= 4 && username.all { it.isLetterOrDigit() || it == '-' }
    }

    private fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }

    private suspend fun validate(): Boolean {
        if (!validateUsername(username.value)) {
            sendErrorMessage(R.string.message_invalid_username)
            return false
        } else if (!validatePassword(password.value)) {
            sendErrorMessage(R.string.message_invalid_password)
            return false
        }
        return true
    }


    fun login() {
        viewModelScope.launch {
            try {
                if (validate()) {
                    loading.value = true
                    userRepo.login(LoginPayload(username.value, password.value))
                    sendFinishSignal()
                }
            } catch (e: ClientRequestException) {
                loading.value = false
                if (e.response.status == HttpStatusCode.Unauthorized) {
                    sendErrorMessage(R.string.message_incorrect_username_or_password)
                } else {
                    sendErrorMessage(R.string.message_unknown_error)
                    Log.e(TAG, e.message, e)
                }
            } catch (e: HttpRequestTimeoutException) {
                loading.value = false
                sendErrorMessage(R.string.message_request_timeout)
            } catch (e: Exception) {
                loading.value = false
                sendErrorMessage(R.string.message_unknown_error)
                Log.e(TAG, e.message, e)
            }
        }
    }

    companion object {
        val TAG
            get() = LoginViewModel::class.simpleName
    }
}