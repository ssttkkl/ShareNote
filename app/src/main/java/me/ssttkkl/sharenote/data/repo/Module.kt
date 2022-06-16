package me.ssttkkl.sharenote.data.repo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class Module {

    @Binds
    abstract fun authRepo(repo: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun noteRepo(repo: NoteRepositoryImpl): NoteRepository

    @Binds
    abstract fun inviteRepo(repo: NoteInviteRepositoryImpl): NoteInviteRepository

    @Binds
    abstract fun permissionRepo(repo: NotePermissionRepositoryImpl): NotePermissionRepository

    @Binds
    abstract fun draftRepo(repository: DraftRepositoryImpl): DraftRepository
}