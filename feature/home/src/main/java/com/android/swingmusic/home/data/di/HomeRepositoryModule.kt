package com.android.swingmusic.home.data.di

import com.android.swingmusic.home.data.repository.DataHomeRepository
import com.android.swingmusic.home.domain.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDataHomeRepository(dataHomeRepository: DataHomeRepository): HomeRepository
}
