package com.android.swingmusic.player.data.di


import com.android.swingmusic.player.data.repository.DataQueueRepository
import com.android.swingmusic.player.domain.repository.QueueRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindQueueRepository(
        dataQueueRepository: DataQueueRepository
    ): QueueRepository
}
