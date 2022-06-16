package me.ssttkkl.sharenote.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import me.ssttkkl.sharenote.data.RemoteAPI
import me.ssttkkl.sharenote.data.entity.*
import me.ssttkkl.sharenote.data.payload.NoteDto
import me.ssttkkl.sharenote.data.payload.Pageable
import me.ssttkkl.sharenote.data.payload.Sort
import me.ssttkkl.sharenote.data.repo.AuthRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton


interface PrivateService : NoteService, TagService,
    NotePermissionService, NoteInviteService

@Singleton
internal class PrivateServiceImpl @Inject constructor(
    client: HttpClient,
    private val authRepo: AuthRepository,
) : PrivateService {
    private suspend fun accessToken(): String {
        val acc = authRepo.accessToken.value
        if (acc != null) {
            val exp = authRepo.accessTokenExpiresAt.value
            if (exp != null && exp.isAfter(Instant.now())) {
                return acc
            }
        }

        authRepo.refresh()
        return authRepo.accessToken.value ?: ""
    }

    private val basic = BasicPrivateService(client) {
        "Bearer " + accessToken()
    }

    private suspend fun <T> autoRefresh(request: suspend () -> T): T {
        return try {
            request()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized && authRepo.refresh()) {
                request()
            } else {
                throw e
            }
        }
    }

    override suspend fun getNotes(
        titleKeywords: Set<String>,
        tags: Set<String>,
        readonly: Boolean,
        sort: Sort,
        pageable: Pageable
    ): Page<Note> = autoRefresh { basic.getNotes(titleKeywords, tags, readonly, sort, pageable) }

    override suspend fun getNoteByID(noteID: Int): Note? =
        autoRefresh { basic.getNoteByID(noteID) }

    override suspend fun createNote(dto: NoteDto): Note =
        autoRefresh { basic.createNote(dto) }

    override suspend fun modifyNote(noteID: Int, dto: NoteDto, ignoreConflict: Boolean): Note =
        autoRefresh { basic.modifyNote(noteID, dto, ignoreConflict) }

    override suspend fun deleteNote(noteID: Int) = autoRefresh { basic.deleteNote(noteID) }

    override suspend fun setNoteTags(noteID: Int, tags: Set<String>) =
        autoRefresh { basic.setNoteTags(noteID, tags) }

    override suspend fun getTags(pageable: Pageable): Page<TagCount> =
        autoRefresh { basic.getTags(pageable) }

    override suspend fun getNotePermissions(
        noteID: Int,
        pageable: Pageable
    ): Page<NotePermissionWithUser> =
        autoRefresh { basic.getNotePermissions(noteID, pageable) }

    override suspend fun modifyNotePermission(noteID: Int, userID: Int, readonly: Boolean) =
        autoRefresh { basic.modifyNotePermission(noteID, userID, readonly) }

    override suspend fun deleteNotePermission(noteID: Int, userID: Int) =
        autoRefresh { basic.deleteNotePermission(noteID, userID) }

    override suspend fun deleteSelfNotePermission(noteID: Int) =
        autoRefresh { basic.deleteSelfNotePermission(noteID) }

    override suspend fun getInvite(inviteID: String): NoteInvite =
        autoRefresh { basic.getInvite(inviteID) }

    override suspend fun createInvite(noteID: Int, readonly: Boolean): NoteInvite =
        autoRefresh { basic.createInvite(noteID, readonly) }

    override suspend fun consumeInvite(inviteID: String) =
        autoRefresh { basic.consumeInvite(inviteID) }

    override suspend fun invalidateInvite(inviteID: String) =
        autoRefresh { basic.invalidateInvite(inviteID) }
}

private class BasicPrivateService(
    private val client: HttpClient,
    private val loadAuthorization: suspend () -> String,
) : PrivateService {

    private fun HttpRequestBuilder.extractPageable(p: Pageable) {
        parameter("page", p.page)
        parameter("size", p.size)
    }

    private suspend fun HttpRequestBuilder.withAuthorization() {
        header("Authorization", loadAuthorization())
    }

    override suspend fun getNotes(
        titleKeywords: Set<String>,
        tags: Set<String>,
        readonly: Boolean,
        sort: Sort,
        pageable: Pageable
    ): Page<Note> {
        return client.get {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.getNotes)
            }
            withAuthorization()
            parameter("titleKeywords", titleKeywords)
            parameter("tags", tags)
            parameter("readonly", readonly)
            parameter("sort", sort)
            extractPageable(pageable)
        }.body()
    }

    override suspend fun getNoteByID(noteID: Int): Note? {
        try {
            return client.get {
                url {
                    host = RemoteAPI.host
                    path(RemoteAPI.basePath, RemoteAPI.getNoteByID(noteID))
                }
                withAuthorization()
            }.body()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                return null
            } else {
                throw e
            }
        }
    }

    override suspend fun createNote(dto: NoteDto): Note {
        return client.post {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.createNote)
            }
            withAuthorization()
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    override suspend fun modifyNote(noteID: Int, dto: NoteDto, ignoreConflict: Boolean): Note {
        return client.put {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.modifyNote(noteID))
            }
            withAuthorization()
            contentType(ContentType.Application.Json)
            parameter("ignoreConflict", ignoreConflict)
            setBody(dto)
        }.body()
    }

    override suspend fun deleteNote(noteID: Int) {
        client.delete {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.deleteNote(noteID))
            }
            withAuthorization()
        }
    }

    override suspend fun setNoteTags(noteID: Int, tags: Set<String>) {
        client.post {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.setNoteTags(noteID))
            }
            withAuthorization()
            contentType(ContentType.Application.Json)
            setBody(tags)
        }
    }

    override suspend fun getTags(pageable: Pageable): Page<TagCount> {
        return client.get {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.getTags)
                extractPageable(pageable)
            }
            withAuthorization()
        }.body()
    }

    override suspend fun getNotePermissions(
        noteID: Int,
        pageable: Pageable
    ): Page<NotePermissionWithUser> {
        return client.get {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.getNotePermissions(noteID))
                extractPageable(pageable)
            }
            withAuthorization()
        }.body()
    }

    override suspend fun modifyNotePermission(noteID: Int, userID: Int, readonly: Boolean) {
        client.put {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.modifyNotePermission(noteID, userID))
                parameter("readonly", readonly)
            }
            withAuthorization()
        }
    }

    override suspend fun deleteNotePermission(noteID: Int, userID: Int) {
        client.delete {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.deleteNotePermission(noteID, userID))
            }
            withAuthorization()
        }
    }

    override suspend fun deleteSelfNotePermission(noteID: Int) {
        client.delete {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.deleteSelfNotePermission(noteID))
            }
            withAuthorization()
        }
    }

    override suspend fun getInvite(inviteID: String): NoteInvite {
        return client.get {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.getInvite(inviteID))
            }
            withAuthorization()
        }.body()
    }

    override suspend fun createInvite(noteID: Int, readonly: Boolean): NoteInvite {
        return client.post {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.createInvite)
            }
            withAuthorization()
            parameter("noteID", noteID)
            parameter("readonly", readonly)
        }.body()
    }

    override suspend fun consumeInvite(inviteID: String) {
        client.post {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.consumeInvite(inviteID))
            }
            withAuthorization()
        }
    }

    override suspend fun invalidateInvite(inviteID: String) {
        client.delete {
            url {
                host = RemoteAPI.host
                path(RemoteAPI.basePath, RemoteAPI.invalidateInvite(inviteID))
            }
            withAuthorization()
        }
    }
}