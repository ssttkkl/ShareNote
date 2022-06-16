package me.ssttkkl.sharenote.data.service

import me.ssttkkl.sharenote.data.entity.*
import me.ssttkkl.sharenote.data.payload.*

interface AuthService {

    suspend fun login(body: LoginPayload): Authentication
    suspend fun refresh(body: RefreshPayload): Authentication
    suspend fun register(body: RegisterPayload): User
}

interface NoteService {

    suspend fun getNotes(
        titleKeywords: Set<String> = emptySet(),
        tags: Set<String> = emptySet(),
        readonly: Boolean = false,
        sort: Sort = Sort.idAsc,
        pageable: Pageable = Pageable.default,
    ): Page<Note>

    suspend fun getNoteByID(noteID: Int): Note?
    suspend fun createNote(dto: NoteDto): Note
    suspend fun modifyNote(noteID: Int, dto: NoteDto, ignoreConflict: Boolean = false): Note
    suspend fun deleteNote(noteID: Int)
    suspend fun setNoteTags(noteID: Int, tags: Set<String>)
}

interface TagService {

    suspend fun getTags(pageable: Pageable = Pageable.default): Page<TagCount>
}

interface NotePermissionService {

    suspend fun getNotePermissions(
        noteID: Int,
        pageable: Pageable = Pageable.default
    ): Page<NotePermissionWithUser>

    suspend fun modifyNotePermission(noteID: Int, userID: Int, readonly: Boolean)
    suspend fun deleteNotePermission(noteID: Int, userID: Int)
    suspend fun deleteSelfNotePermission(noteID: Int)
}

interface NoteInviteService {

    suspend fun getInvite(inviteID: String): NoteInvite
    suspend fun createInvite(noteID: Int, readonly: Boolean): NoteInvite
    suspend fun consumeInvite(inviteID: String)
    suspend fun invalidateInvite(inviteID: String)
}
