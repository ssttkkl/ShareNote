package me.ssttkkl.sharenote.data.repo

import androidx.paging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.data.entity.Note
import me.ssttkkl.sharenote.data.entity.TagCount
import me.ssttkkl.sharenote.data.payload.NoteDto
import me.ssttkkl.sharenote.data.payload.Pageable
import me.ssttkkl.sharenote.data.payload.Sort
import me.ssttkkl.sharenote.data.persist.AppDatabase
import me.ssttkkl.sharenote.data.persist.entity.PersistNote
import me.ssttkkl.sharenote.data.persist.entity.toNote
import me.ssttkkl.sharenote.data.persist.entity.toUser
import me.ssttkkl.sharenote.data.persist.persister.NotePersister
import me.ssttkkl.sharenote.data.service.PrivateService
import javax.inject.Inject
import javax.inject.Singleton

interface NoteRepository {

    val datasetUpdatedSignal: Flow<Any>

    fun getNotes(
        titleKeywords: Set<String> = emptySet(),
        tags: Set<String> = emptySet(),
        readonly: Boolean = false,
        sort: Sort = Sort.idAsc
    ): Flow<PagingData<Note>>

    fun getNoteByIDAsFlow(noteID: Int): Flow<Note?>

    suspend fun getNoteByID(noteID: Int): Note?
    suspend fun createNote(dto: NoteDto): Note
    suspend fun modifyNote(noteID: Int, dto: NoteDto, ignoreConflict: Boolean = false): Note
    suspend fun deleteNote(noteID: Int)

    suspend fun deleteSelfNotePermission(noteID: Int)

    suspend fun setNoteTags(noteID: Int, tags: Set<String>)

    fun getTags(): Flow<PagingData<TagCount>>
}

@OptIn(ExperimentalPagingApi::class)
@Singleton
internal class NoteRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val persister: NotePersister,
    private val service: PrivateService,
    private val authRepo: AuthRepository
) : NoteRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _datasetUpdatedSignal = MutableSharedFlow<Unit>()
    override val datasetUpdatedSignal: Flow<Any>
        get() = _datasetUpdatedSignal

    internal suspend fun fireDatasetUpdate() {
        _datasetUpdatedSignal.emit(Unit)
    }

    private inline fun onUpdate(crossinline action: () -> Unit) {
        _datasetUpdatedSignal.onEach {
            action()
        }.launchIn(scope)
    }

    init {
        scope.launch {
            authRepo.user.distinctUntilChangedBy { it?.id }
                .collectLatest {
                    fireDatasetUpdate()
                }
        }
    }

    private val noteSourceFactory = InvalidatingPagingSourceFactory {
        db.noteDao.findAllAsPagingSource()
    }.apply {
        onUpdate { invalidate() }
    }

    private suspend fun PersistNote.toNote(): Note {
        val tags = db.noteTagDao.findByNoteID(id).map { it.tagName }.toSet()
        val ownerUser = db.userDao.findById(ownerUserID)!!.toUser()
        val modifiedBy = db.userDao.findById(modifiedByUserID)!!.toUser()
        return toNote(tags, ownerUser, modifiedBy)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun Flow<PersistNote?>.toNoteFlow(): Flow<Note?> {
        return flatMapLatest { pNote ->
            if (pNote == null)
                flowOf(null)
            else
                db.noteTagDao.findByNoteIdAsFlow(pNote.id).flatMapLatest { pNoteTags ->
                    db.userDao.findByIdAsFlow(pNote.ownerUserID).flatMapLatest { pOwnerUser ->
                        db.userDao.findByIdAsFlow(pNote.modifiedByUserID).map { pModifiedBy ->
                            pNote.toNote(
                                pNoteTags.map { it.tagName }.toSet(),
                                pOwnerUser!!.toUser(),
                                pModifiedBy!!.toUser()
                            )
                        }
                    }
                }
        }
    }

    override fun getNotes(
        titleKeywords: Set<String>,
        tags: Set<String>,
        readonly: Boolean,
        sort: Sort
    ): Flow<PagingData<Note>> {
        val notePager = Pager(
            config = PagingConfig(pageSize = 20),
            remoteMediator = NoteRemoteMediator(persister) { service.getNotes(pageable = it) },
            pagingSourceFactory = noteSourceFactory
        )

        return notePager.flow.map { page ->
            page.map { it.toNote() }
        }
    }

    override fun getNoteByIDAsFlow(noteID: Int): Flow<Note?> {
        if (noteID == 0)
            return flowOf(null)

        scope.launch { getNoteByID(noteID) }

        return db.noteDao.findByIdAsFlow(noteID).toNoteFlow()
    }

    override suspend fun getNoteByID(noteID: Int): Note? {
        val note = service.getNoteByID(noteID)
        if (note != null) {
            persister.persist(note)
        }
        return note
    }

    override suspend fun createNote(dto: NoteDto): Note {
        val note = service.createNote(dto)
        fireDatasetUpdate()
        return note
    }

    override suspend fun modifyNote(noteID: Int, dto: NoteDto, ignoreConflict: Boolean): Note {
        val note = service.modifyNote(noteID, dto, ignoreConflict)
        persister.persist(note)
        fireDatasetUpdate()
        return note
    }

    override suspend fun deleteNote(noteID: Int) {
        service.deleteNote(noteID)
        fireDatasetUpdate()
    }

    override suspend fun deleteSelfNotePermission(noteID: Int) {
        service.deleteSelfNotePermission(noteID)
        fireDatasetUpdate()
    }

    override suspend fun setNoteTags(noteID: Int, tags: Set<String>) {
        service.setNoteTags(noteID, tags)
        persister.persistTags(noteID, tags)
    }

    override fun getTags(): Flow<PagingData<TagCount>> {
        val pageSize = 20
        return Pager(config = PagingConfig(pageSize)) {
            RemotePagingSource { page ->
                service.getTags(Pageable(page * pageSize, pageSize))
            }
        }.flow
    }
}