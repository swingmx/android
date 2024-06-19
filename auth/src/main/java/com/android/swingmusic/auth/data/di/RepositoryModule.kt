package com.android.swingmusic.auth.data.di

import com.android.swingmusic.auth.data.repository.DataAuthRepository
import com.android.swingmusic.auth.domain.repository.AuthRepository
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
    abstract fun bindDataAuthRepository(dataAuthRepository: DataAuthRepository): AuthRepository
}
