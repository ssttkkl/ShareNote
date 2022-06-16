package me.ssttkkl.sharenote.data.service

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ServiceModule {
    @Binds
    abstract fun publicService(impl: PublicServiceImpl): PublicService

    @Binds
    abstract fun privateService(impl: PrivateServiceImpl): PrivateService
}