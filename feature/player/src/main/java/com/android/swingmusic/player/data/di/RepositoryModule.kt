package com.android.swingmusic.player.data.di


import com.android.swingmusic.player.data.repository.DataLyricsRepository
import com.android.swingmusic.player.data.repository.DataPlayerRepository
import com.android.swingmusic.player.domain.repository.LyricsRepository
import com.android.swingmusic.player.domain.repository.PlayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindQueueRepository(
        dataQueueRepository: DataPlayerRepository
    ): PlayerRepository

    @Binds
    @Singleton
    abstract fun bindLyricsRepository(
        dataLyricsRepository: DataLyricsRepository
    ): LyricsRepository
}
