package me.ssttkkl.sharenote.data

object RemoteAPI {
    const val host = "localhost:8080"
    const val basePath = "api/v1"

    const val login = "auth/login"
    const val refresh = "auth/refresh"
    const val register = "auth/register"

    const val getNotes = "notes"
    const val createNote = "notes"
    fun getNoteByID(noteID: Int) = "notes/${noteID}"
    fun modifyNote(noteID: Int) = "notes/${noteID}"
    fun deleteNote(noteID: Int) = "notes/${noteID}"
    fun setNoteTags(noteID: Int) = "notes/${noteID}/tags"

    fun getNotePermissions(noteID: Int) = "notes/${noteID}/permissions"
    fun modifyNotePermission(noteID: Int, userID: Int) = "notes/${noteID}/permissions/${userID}"
    fun deleteNotePermission(noteID: Int, userID: Int) = "notes/${noteID}/permissions/${userID}"
    fun deleteSelfNotePermission(noteID: Int) = "notes/${noteID}/permissions/self"

    const val getTags = "notes/all/tags"

    const val createInvite = "invites"
    fun getInvite(inviteID: String) = "invites/${inviteID}"
    fun consumeInvite(inviteID: String) = "invites/${inviteID}/consumption"
    fun invalidateInvite(inviteID: String) = "invites/${inviteID}"
}