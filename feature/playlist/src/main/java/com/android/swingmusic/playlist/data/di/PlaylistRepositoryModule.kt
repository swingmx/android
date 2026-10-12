package com.android.swingmusic.playlist.data.di

import com.android.swingmusic.playlist.data.repository.DataPlaylistRepository
import com.android.swingmusic.playlist.domain.PlaylistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaylistRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(repository: DataPlaylistRepository): PlaylistRepository
}
