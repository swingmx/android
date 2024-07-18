package com.android.swingmusic.player.data.di


import com.android.swingmusic.player.data.repository.DataPLayerRepository
import com.android.swingmusic.player.domain.repository.PLayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindQueueRepository(
        dataQueueRepository: DataPLayerRepository
    ): PLayerRepository
}
