package me.ssttkkl.sharenote.data.persist.persister

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class Module {
    @Binds
    abstract fun notePersister(impl: NotePersisterImpl): NotePersister
}