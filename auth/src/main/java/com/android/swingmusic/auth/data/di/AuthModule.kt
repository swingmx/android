package com.android.swingmusic.auth.data.di


import android.content.Context
import com.android.swingmusic.auth.data.api.service.AuthApiService
import com.android.swingmusic.auth.data.datastore.AuthTokensDataStore
import com.android.swingmusic.database.data.dao.BaseUrlDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
object AuthModule {
    @Provides
    @Singleton
    fun providesAuthTokenDataStore(
        @ApplicationContext context: Context
    ): AuthTokensDataStore {
        return AuthTokensDataStore(context = context.applicationContext)
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .callTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun providesAuthApiService(okHttpClient: OkHttpClient, baseUrlDao: BaseUrlDao): AuthApiService {
        val baseUrl = runBlocking(Dispatchers.IO) {
            baseUrlDao.getBaseUrl()?.url ?: "http://default"
        }
        Timber.e(":auth -> BASE URL: $baseUrl")

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(AuthApiService::class.java)
    }
}
