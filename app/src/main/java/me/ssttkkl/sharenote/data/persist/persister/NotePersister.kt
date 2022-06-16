package me.ssttkkl.sharenote.data.persist.persister

import androidx.room.withTransaction
import me.ssttkkl.sharenote.data.entity.Note
import me.ssttkkl.sharenote.data.entity.Page
import me.ssttkkl.sharenote.data.persist.AppDatabase
import me.ssttkkl.sharenote.data.persist.entity.PersistNoteRemoteKey
import me.ssttkkl.sharenote.data.persist.entity.PersistNoteTag
import me.ssttkkl.sharenote.data.persist.entity.toPersistNote
import me.ssttkkl.sharenote.data.persist.entity.toPersistUser
import java.lang.Integer.max
import javax.inject.Inject

interface NotePersister {

    suspend fun persist(page: Page<Note>, clearBefore: Boolean = false)
    suspend fun persist(content: Note)
    suspend fun persistTags(noteID: Int, tags: Set<String>)

    suspend fun invalidate(id: Int)
    suspend fun clear()
    suspend fun getRemoteNextKey(id: Int): Int?
}

internal class NotePersisterImpl @Inject constructor(
    private val db: AppDatabase
) : NotePersister {

    private var largestKey: Int = 0

    override suspend fun persist(
        page: Page<Note>,
        clearBefore: Boolean
    ) {
        val nextKey = if (page.isLastPage) null else page.page + 1
        if (nextKey != null)
            largestKey = max(nextKey, largestKey)

        db.withTransaction {
            if (clearBefore) {
                clear()
            }
            page.content.forEach {
                persist(it)

                val pRemoteKey = page.content.map { PersistNoteRemoteKey(it.id, nextKey) }
                db.noteRemoteKeyDao.insert(pRemoteKey)
            }
        }
    }

//    suspend fun persistWithFakeRemoteKey(content: Note) {
//        val nextKey = largestKey
//        db.withTransaction {
//            val pNote = content.toPersistNote()
//            db.noteDao.insert(pNote)
//
//            persistTags(content.id, content.tags)
//
//            val pOwnerUser = content.ownerUser.toPersistUser()
//            val pModifiedBy = content.modifiedBy.toPersistUser()
//            db.userDao.insert(pOwnerUser)
//            db.userDao.insert(pModifiedBy)
//
//            val pRemoteKey = PersistNoteRemoteKey(content.id, nextKey)
//            db.noteRemoteKeyDao.insert(pRemoteKey)
//        }
//    }

    override suspend fun persist(content: Note) {
        db.withTransaction {
            val pNote = content.toPersistNote()
            db.noteDao.insert(pNote)

            val pOwnerUser = content.ownerUser.toPersistUser()
            val pModifiedBy = content.modifiedBy.toPersistUser()
            db.userDao.insert(pOwnerUser)
            db.userDao.insert(pModifiedBy)

            persistTags(content.id, content.tags)
        }
    }

    override suspend fun persistTags(noteID: Int, tags: Set<String>) {
        db.withTransaction {
            val pNoteTag = tags.map { PersistNoteTag(noteID, it) }
            db.noteTagDao.deleteByNoteId(noteID)
            db.noteTagDao.insert(pNoteTag)
        }
    }

    override suspend fun invalidate(id: Int) {
        db.withTransaction {
            db.noteDao.deleteById(id)
            db.noteTagDao.deleteByNoteId(id)
            db.noteRemoteKeyDao.deleteByNoteId(id)
        }
    }

    override suspend fun clear() {
        db.withTransaction {
            db.noteDao.deleteAll()
            db.userDao.deleteAll()
            db.noteTagDao.deleteAll()
            db.noteRemoteKeyDao.deleteAll()
        }
    }

    override suspend fun getRemoteNextKey(id: Int): Int? {
        return db.withTransaction {
            val key = db.noteRemoteKeyDao.findById(id)
                ?: error("remote key of last item was not found")
            key.nextKey
        }
    }
}