package com.android.swingmusic.search.data.di

import com.android.swingmusic.search.data.repository.DataSearchRepository
import com.android.swingmusic.search.domain.reposotory.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDataSearchRepository(dataSearchRepository: DataSearchRepository): SearchRepository
}
