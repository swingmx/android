package com.android.swingmusic.folder.data.repository.di

import com.android.swingmusic.folder.data.repository.DataFolderRepository
import com.android.swingmusic.folder.domain.FolderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FolderRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDataFolderRepository(folderRepository: DataFolderRepository): FolderRepository
}
