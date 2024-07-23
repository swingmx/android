package com.android.swingmusic.auth.domain.repository

import com.android.swingmusic.auth.domain.model.AllUsers
import com.android.swingmusic.auth.domain.model.LogInResult
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.database.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun getBaseUrl(): String?

    suspend fun storeBaseUrl(url: String)

    fun getAccessToken(): String?

    fun getRefreshToken(): String?

    suspend fun storeAuthTokens(
        accessToken: String,
        refreshToken: String,
        maxAge: Long
    )

    suspend fun getFreshTokensFromServer(): LogInResult?

    suspend fun getAllUsers(baseUrl: String): Flow<Resource<AllUsers>>

    suspend fun getLoggedInUser(): User?

    suspend fun storeLoggedInUser(user: User)

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
