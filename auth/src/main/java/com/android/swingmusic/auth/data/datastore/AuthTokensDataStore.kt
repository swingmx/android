package com.android.swingmusic.auth.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthTokensDataStore @Inject constructor(
    private val context: Context
) {
    private companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val MAX_AGE: Key<Long> = longPreferencesKey("max_age")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val SESSION_EXPIRED = booleanPreferencesKey("session_expired")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { data ->
        val token = data[ACCESS_TOKEN] ?: ""
        token
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { data ->
        val token = data[REFRESH_TOKEN] ?: ""
        token
    }

    val maxTokenAge: Flow<Long?> = context.dataStore.data.map { data ->
        val age = data[MAX_AGE] ?: 0L
        age
    }

    /** Null on installs that predate the flag. */
    val isLoggedIn: Flow<Boolean?> = context.dataStore.data.map { data -> data[IS_LOGGED_IN] }

    val sessionExpired: Flow<Boolean> = context.dataStore.data.map { data ->
        data[SESSION_EXPIRED] ?: false
    }

    suspend fun updateAuthTokens(
        accessToken: String,
        refreshToken: String,
        maxAge: Long
    ) {
        context.dataStore.edit { data ->
            data[ACCESS_TOKEN] = accessToken
            data[REFRESH_TOKEN] = refreshToken
            data[MAX_AGE] = maxAge
        }
    }

    suspend fun setLoggedIn() {
        context.dataStore.edit { data ->
            data[IS_LOGGED_IN] = true
            data[SESSION_EXPIRED] = false
        }
    }

    suspend fun clearSession(expired: Boolean) {
        context.dataStore.edit { data ->
            data.remove(ACCESS_TOKEN)
            data.remove(REFRESH_TOKEN)
            data.remove(MAX_AGE)
            data[IS_LOGGED_IN] = false
            data[SESSION_EXPIRED] = expired
        }
    }

    suspend fun clearSessionExpired() {
        context.dataStore.edit { data -> data[SESSION_EXPIRED] = false }
    }
}
