package com.android.swingmusic.player.data.di


import com.android.swingmusic.player.data.repository.DataLyricsRepository
import com.android.swingmusic.player.data.repository.DataPLayerRepository
import com.android.swingmusic.player.domain.repository.LyricsRepository
import com.android.swingmusic.player.domain.repository.PLayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindQueueRepository(
        dataQueueRepository: DataPLayerRepository
    ): PLayerRepository

    @Binds
    @Singleton
    abstract fun bindLyricsRepository(
        dataLyricsRepository: DataLyricsRepository
    ): LyricsRepository
}
