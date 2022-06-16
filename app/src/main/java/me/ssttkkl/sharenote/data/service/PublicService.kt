package me.ssttkkl.sharenote.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import me.ssttkkl.sharenote.data.RemoteAPI
import me.ssttkkl.sharenote.data.entity.Authentication
import me.ssttkkl.sharenote.data.entity.User
import me.ssttkkl.sharenote.data.payload.LoginPayload
import me.ssttkkl.sharenote.data.payload.RefreshPayload
import me.ssttkkl.sharenote.data.payload.RegisterPayload
import javax.inject.Inject
import javax.inject.Singleton


interface PublicService : AuthService

@Singleton
internal class PublicServiceImpl @Inject constructor(
    private val client: HttpClient,
) : PublicService {

    override suspend fun login(body: LoginPayload): Authentication {
        return client.post {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.login)
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    override suspend fun refresh(body: RefreshPayload): Authentication {
        return refresh(client, body)
    }

    override suspend fun register(body: RegisterPayload): User {
        return client.post {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.register)
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    companion object {
        suspend fun refresh(
            client: HttpClient,
            body: RefreshPayload,
            build: (suspend HttpRequestBuilder.() -> Unit)? = null
        ): Authentication {
            return client.post {
                url {
                    host = RemoteAPI.host
                    path(RemoteAPI.basePath, RemoteAPI.refresh)
                }
                contentType(ContentType.Application.Json)
                setBody(body)
                build?.invoke(this)
            }.body()
        }
    }
}