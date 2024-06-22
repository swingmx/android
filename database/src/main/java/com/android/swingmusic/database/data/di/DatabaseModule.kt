package com.android.swingmusic.database.data.di

import android.content.Context
import androidx.room.Room
import com.android.swingmusic.database.data.dao.BaseUrlDao
import com.android.swingmusic.database.data.dao.LastPlayedTrackDao
import com.android.swingmusic.database.data.dao.QueueDao
import com.android.swingmusic.database.data.dao.UserDao
import com.android.swingmusic.database.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideBaseUrlDao(database: AppDatabase): BaseUrlDao {
        return database.baseUrlDao()
    }

    @Provides
    fun provideTrackDao(database: AppDatabase): QueueDao {
        return database.queueDao()
    }

    @Provides
    fun provideLastPlayedTrackDao(database: AppDatabase): LastPlayedTrackDao {
        return database.lastPlayedTrackDao()
    }
}
