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
   /* @Singleton
    @Provides
    fun providesOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .callTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }*/

    @Provides
    @Singleton
    fun providesApiService(okHttpClient: OkHttpClient, baseUrlDao: BaseUrlDao): NetworkApiService {
        // Use a runBlocking block to fetch the URL synchronously
        val baseUrl = runBlocking(Dispatchers.IO) {
            baseUrlDao.getBaseUrl()?.url ?: ""
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
