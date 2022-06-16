package me.ssttkkl.sharenote.data.storage

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.ssttkkl.sharenote.data.entity.User
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataStore @Inject constructor(
    val app: Application
) {

    private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

    private val keyUser = stringPreferencesKey("user")
    private val keyAccessToken = stringPreferencesKey("access_token")
    private val keyRefreshToken = stringPreferencesKey("refresh_token")
    private val keyAccessTokenExpiresAt = longPreferencesKey("access_token_expires_at")

    private val scope = CoroutineScope(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    val user: StateFlow<User?> = app.userDataStore.data.mapLatest { pref ->
        try {
            Json.decodeFromString<User?>(pref[keyUser]!!)
        } catch (e: Exception) {
            null
        }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val accessToken: StateFlow<String?> = app.userDataStore.data.mapLatest { pref ->
        pref[keyAccessToken]
    }.stateIn(scope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val refreshToken: StateFlow<String?> = app.userDataStore.data.mapLatest { pref ->
        pref[keyRefreshToken]
    }.stateIn(scope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val accessTokenExpiresAt: StateFlow<Instant?> = app.userDataStore.data.mapLatest { pref ->
        pref[keyAccessTokenExpiresAt]?.let { Instant.ofEpochSecond(it) }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    inner class Editor {
        private val edit = HashMap<Preferences.Key<*>, Any?>()

        var user: User?
            get() = this@UserDataStore.user.value
            set(value) {
                edit[keyUser] = value?.let { Json.encodeToString(it) }
            }

        var accessToken: String?
            get() = this@UserDataStore.accessToken.value
            set(value) {
                edit[keyAccessToken] = value
            }

        var refreshToken: String?
            get() = this@UserDataStore.refreshToken.value
            set(value) {
                edit[keyRefreshToken] = value
            }

        var accessTokenExpiresAt: Instant?
            get() = this@UserDataStore.accessTokenExpiresAt.value
            set(value) {
                edit[keyAccessTokenExpiresAt] = value?.toEpochMilli()
            }

        suspend fun submit() {
            app.userDataStore.edit { pref ->
                edit.forEach { (key, value) ->
                    if (value != null) {
                        when (key) {
                            keyUser -> pref[keyUser] = value as String
                            keyAccessToken -> pref[keyAccessToken] = value as String
                            keyRefreshToken -> pref[keyRefreshToken] = value as String
                            keyAccessTokenExpiresAt -> pref[keyAccessTokenExpiresAt] = value as Long
                        }
                    } else {
                        when (key) {
                            keyUser -> pref -= keyUser
                            keyAccessToken -> pref -= keyAccessToken
                            keyRefreshToken -> pref -= keyRefreshToken
                            keyAccessTokenExpiresAt -> pref -= keyAccessTokenExpiresAt
                        }
                    }
                }
            }
        }
    }

    suspend inline fun edit(crossinline action: Editor.() -> Unit) {
        val editor = Editor()
        action(editor)
        editor.submit()
    }
}