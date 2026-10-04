package com.android.swingmusic.auth.domain.repository

import com.android.swingmusic.auth.domain.model.AllUsers
import com.android.swingmusic.auth.domain.model.LogInResult
import com.android.swingmusic.auth.domain.model.SessionEndReason
import com.android.swingmusic.auth.domain.model.TokenRefreshResult
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.database.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /** Emits once each time a session ends, whether by log out or expiry. */
    val sessionEnded: Flow<SessionEndReason>

    suspend fun initializeBaseUrlAndAuthTokens()
    
    suspend fun getBaseUrl(): String?

    suspend fun storeBaseUrl(url: String)

    suspend fun getAccessToken(): String?

    suspend fun getRefreshToken(): String?

    suspend fun storeAuthTokens(
        accessToken: String,
        refreshToken: String,
        maxAge: Long
    )

    suspend fun refreshTokens(): TokenRefreshResult

    suspend fun getAllUsers(baseUrl: String): Flow<Resource<AllUsers>>

    suspend fun getLoggedInUser(): User?

    /** Fetches the user from the server and caches it locally. */
    suspend fun fetchCurrentUser(): Resource<User>

    /** Pass only what changes; returns the updated user, already cached. */
    suspend fun updateProfile(username: String? = null, password: String? = null): Resource<User>

    suspend fun isLoggedIn(): Boolean

    suspend fun markLoggedIn()

    suspend fun isSessionExpired(): Boolean

    suspend fun clearSessionExpired()

    suspend fun logOut()

    suspend fun endSession(reason: SessionEndReason)

    suspend fun createUser(
        username: String,
        password: String,
        email: String,
        roles: List<String>
    ): Flow<Resource<User>>

    suspend fun logInWithUsernameAndPassword(
        baseUrl: String,
        username: String,
        password: String
    ): Flow<Resource<LogInResult>>

    /**Should return a Pair of <Url, Code> after decoding the encoded string*/
    fun processQrCodeData(encoded: String): Pair<String, String>

    suspend fun logInWithQrCode(url: String, pairCode: String): Flow<Resource<LogInResult>>
}
