package me.ssttkkl.sharenote.data.repo

import me.ssttkkl.sharenote.data.service.NoteInviteService
import me.ssttkkl.sharenote.data.service.PrivateServiceImpl
import javax.inject.Inject

typealias NoteInviteRepository = NoteInviteService

internal class NoteInviteRepositoryImpl @Inject constructor(
    private val service: PrivateServiceImpl,
    private val noteRepo: NoteRepository
) : NoteInviteRepository by service {

    override suspend fun consumeInvite(inviteID: String) {
        service.consumeInvite(inviteID)
        (noteRepo as? NoteRepositoryImpl)?.fireDatasetUpdate()
    }
}