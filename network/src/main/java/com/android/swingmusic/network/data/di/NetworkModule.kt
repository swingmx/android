package com.android.swingmusic.network.data.di

import com.android.swingmusic.database.data.dao.BaseUrlDao
import com.android.swingmusic.network.data.api.service.NetworkApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun providesNetworkApiService(okHttpClient: OkHttpClient, baseUrlDao: BaseUrlDao): NetworkApiService {
        val baseUrl = runBlocking(Dispatchers.IO) {
            baseUrlDao.getBaseUrl()?.url ?: "http://default"
        }
        Timber.e("BASE URL: $baseUrl")

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(NetworkApiService::class.java)
    }
}
