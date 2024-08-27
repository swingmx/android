package com.android.swingmusic.album.data.di

import com.android.swingmusic.album.data.repository.DataAlbumRepository
import com.android.swingmusic.album.domain.AlbumRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlbumRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDataAlbumRepository(dataAlbumRepository: DataAlbumRepository): AlbumRepository
}
