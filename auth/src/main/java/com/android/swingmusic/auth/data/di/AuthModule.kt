package com.android.swingmusic.auth.data.di


import android.content.Context
import com.android.swingmusic.auth.data.datastore.AuthTokensDataStore
import com.android.swingmusic.auth.data.datastore.SecureStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.KeyStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore
    }

    @Provides
    @Singleton
    fun providesAuthTokenDataStore(
        @ApplicationContext context: Context,
        secureStore: SecureStore
    ): AuthTokensDataStore {
        return AuthTokensDataStore(context = context.applicationContext, secureStore = secureStore)
    }
}
