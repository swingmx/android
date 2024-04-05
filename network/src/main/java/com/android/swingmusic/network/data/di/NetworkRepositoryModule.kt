package com.android.swingmusic.network.data.di

import com.android.swingmusic.network.data.repository.DataNetworkRepository
import com.android.swingmusic.network.domain.repository.NetworkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDataNetworkRepository(networkRepository: DataNetworkRepository): NetworkRepository
}
