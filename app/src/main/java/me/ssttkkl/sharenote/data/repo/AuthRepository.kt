package me.ssttkkl.sharenote.data.repo

import android.util.Log
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import me.ssttkkl.sharenote.data.entity.User
import me.ssttkkl.sharenote.data.payload.LoginPayload
import me.ssttkkl.sharenote.data.payload.RefreshPayload
import me.ssttkkl.sharenote.data.payload.RegisterPayload
import me.ssttkkl.sharenote.data.service.PublicService
import me.ssttkkl.sharenote.data.storage.UserDataStore
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    val user: StateFlow<User?>
    val accessToken: StateFlow<String?>
    val accessTokenExpiresAt: StateFlow<Instant?>

    suspend fun login(payload: LoginPayload)
    suspend fun refresh(): Boolean
    suspend fun logout()
    suspend fun register(payload: RegisterPayload)
}

val AuthRepository.isLoggedIn
    get() = user.map { it != null }

@Singleton
internal class AuthRepositoryImpl @Inject constructor(
    private val service: PublicService,
    private val userDataStore: UserDataStore,
) : AuthRepository {

    override val user: StateFlow<User?>
        get() = userDataStore.user

    override val accessToken: StateFlow<String?>
        get() = userDataStore.accessToken

    override val accessTokenExpiresAt: StateFlow<Instant?>
        get() = userDataStore.accessTokenExpiresAt

    override suspend fun login(payload: LoginPayload) {
        val auth = service.login(payload)
        val now = Instant.now()
        userDataStore.edit {
            accessToken = auth.accessToken
            refreshToken = auth.refreshToken
            accessTokenExpiresAt = now.plusSeconds(auth.expiresIn.toLong())
            user = auth.user
        }
    }

    override suspend fun refresh(): Boolean {
        val ref = userDataStore.refreshToken.value
        if (ref != null) {
            val auth = try {
                val payload = RefreshPayload(ref)
                service.refresh(payload)
            } catch (e: ClientRequestException) {
                // invalid refresh token
                if (e.response.status == HttpStatusCode.Unauthorized) {
                    Log.d("PrivateService", "automatically logout because of invalid refresh token")
                    logout()
                    return false
                } else {
                    throw e
                }
            }

            val now = Instant.now()
            userDataStore.edit {
                accessToken = auth.accessToken
                refreshToken = auth.refreshToken
                accessTokenExpiresAt = now.plusSeconds(auth.expiresIn.toLong())
                user = auth.user
            }
            Log.d("PrivateService", "new token: $auth")
            return true
        }
        return false
    }

    override suspend fun logout() {
        userDataStore.edit {
            user = null
            accessToken = null
            refreshToken = null
            accessTokenExpiresAt = null
        }
    }

    override suspend fun register(payload: RegisterPayload) {
        service.register(payload)
    }
}