package com.android.swingmusic.artist.data.di

import com.android.swingmusic.artist.data.repository.DataArtistRepository
import com.android.swingmusic.artist.domain.repository.ArtistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ArtistRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDataArtistRepository(artistRepository: DataArtistRepository): ArtistRepository
}
