package me.ssttkkl.sharenote.data.service

//object FakeService : PublicService, PrivateService {
//
//    val user = User(1, "test", "user", "")
//
//    val auth = Authentication("114514", "1919810", 3600, user)
//
//    fun genTags() = buildSet {
//        repeat(Random.nextInt(4)) {
//            val tagID = Random.nextInt(6) + 1
//            val tagName = "tag$tagID"
//            add(tagName)
//        }
//    }
//
//    val notes = (1..100).map { id ->
//        Note(
//            id = id,
//            title = "test$id",
//            content = "content$id",
//            tags = genTags(),
//            ownerUser = user,
//            version = 0,
//            readonly = false,
//            createdAt = OffsetDateTime.now(),
//            modifiedAt = OffsetDateTime.now()
//        )
//    }.toMutableList()
//
//    val tags
//        get() = buildMap<String, Int> {
//            notes.forEach { note ->
//                note.tags.forEach { tag ->
//                    this[tag] = this.getOrDefault(tag, 0) + 1
//                }
//            }
//        }.map { TagCount(it.key, it.value) }
//
//    private var noteCounter = 100
//
//    override suspend fun login(body: LoginPayload): Authentication = auth
//
//    override suspend fun refresh(body: RefreshPayload): Authentication = auth
//
//    override suspend fun register(body: RegisterPayload): User = user
//
//    override suspend fun getNotes(
//        titleKeywords: Set<String>,
//        tags: Set<String>,
//        readonly: Boolean,
//        sort: Sort,
//        pageable: Pageable
//    ): Page<Note> {
//        val offset = pageable.page * pageable.size
//        val page = Page(
//            notes.drop(offset).take(pageable.size),
//            pageable.page,
//            pageable.size,
//            ceil(1.0 * notes.size / pageable.size).toInt(),
//            notes.size
//        )
//        return page
//    }
//
//    override suspend fun getNoteByID(noteID: Int): Note? {
//        return notes.find { it.id == noteID }!!
//    }
//
//    override suspend fun createNote(dto: NoteDto): Note {
//        val id = ++noteCounter
//        val n = Note(
//            id = id,
//            title = dto.title,
//            content = dto.content,
//            tags = emptySet(),
//            ownerUser = user,
//            version = 0,
//            readonly = false,
//            createdAt = OffsetDateTime.now(),
//            modifiedAt = OffsetDateTime.now()
//        )
//        notes.add(n)
//        return n
//    }
//
//    override suspend fun modifyNote(noteID: Int, dto: NoteDto): Note {
//        val idx = notes.indexOfFirst { it.id == noteID }
//        val n = notes[idx].copy(
//            title = dto.title,
//            content = dto.content,
//            modifiedAt = OffsetDateTime.now()
//        )
//        notes[idx] = n
//        return n
//    }
//
//    override suspend fun deleteNote(noteID: Int) {
//        val idx = notes.indexOfFirst { it.id == noteID }
//        notes.removeAt(idx)
//    }
//
//    override suspend fun setNoteTags(noteID: Int, tags: Set<String>) {
//        val idx = notes.indexOfFirst { it.id == noteID }
//        notes[idx] = notes[idx].copy(tags = tags)
//    }
//
//    override suspend fun getTags(pageable: Pageable): Page<TagCount> {
//        val tags = tags
//        val offset = pageable.size * pageable.page
//        return Page(
//            tags.drop(offset).take(pageable.size),
//            pageable.page,
//            pageable.size,
//            ceil(1.0 * notes.size / pageable.size).toInt(),
//            notes.size
//        )
//    }
//
//    override suspend fun getNotePermissions(noteID: Int, pageable: Pageable): Page<NotePermission> {
//        val permissions = listOf(NotePermission(user, false))
//        return Page(
//            permissions,
//            0,
//            1,
//            1,
//            1
//        )
//    }
//
//    override suspend fun modifyNotePermission(noteID: Int, userID: Int, readonly: Boolean) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun deleteNotePermission(noteID: Int, userID: Int) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun deleteSelfNotePermission(noteID: Int) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getInvite(inviteID: String): NoteInvite {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun createInvite(noteID: Int, readonly: Boolean): NoteInvite {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun consumeInvite(inviteID: String) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun invalidateInvite(inviteID: String) {
//        TODO("Not yet implemented")
//    }
//
//}