package com.android.swingmusic.profile.data.di

import com.android.swingmusic.profile.data.repository.DataProfileRepository
import com.android.swingmusic.profile.domain.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDataProfileRepository(repository: DataProfileRepository): ProfileRepository
}
