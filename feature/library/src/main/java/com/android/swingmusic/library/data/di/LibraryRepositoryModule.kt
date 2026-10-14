package com.android.swingmusic.library.data.di

import com.android.swingmusic.library.data.repository.DataFavouritesRepository
import com.android.swingmusic.library.data.repository.DataLibraryRepository
import com.android.swingmusic.library.domain.FavouritesRepository
import com.android.swingmusic.library.domain.LibraryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LibraryRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLibraryRepository(repository: DataLibraryRepository): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindFavouritesRepository(repository: DataFavouritesRepository): FavouritesRepository
}
