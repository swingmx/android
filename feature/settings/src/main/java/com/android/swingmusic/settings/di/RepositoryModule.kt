package com.android.swingmusic.settings.di

import com.android.swingmusic.settings.data.repository.AppSettingsDataRepository
import com.android.swingmusic.settings.domain.repository.AppSettingsRepository
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
    abstract fun bindAppSettingsRepository(
        impl: AppSettingsDataRepository
    ): AppSettingsRepository
}
