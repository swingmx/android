package com.android.swingmusic.auth.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthTokensDataStore @Inject constructor(
    private val context: Context,
    private val secureStore: SecureStore
) {
    private companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { data ->
        val token = data[ACCESS_TOKEN] ?: ""
        // secureStore.decrypt(encryptedToken)
        token
    }

    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { data ->
        val token = data[REFRESH_TOKEN] ?: ""
        // secureStore.decrypt(encryptedToken)
        token
    }

    suspend fun updateAuthTokens(accessToken: String, refreshToken: String) {
        // val encryptedAccessToken = secureStore.encrypt(accessToken)
        //  val encryptedRefreshToken = secureStore.encrypt(refreshToken)

        context.dataStore.edit { data ->
            data[ACCESS_TOKEN] = accessToken
            data[REFRESH_TOKEN] = refreshToken
        }
    }
}
