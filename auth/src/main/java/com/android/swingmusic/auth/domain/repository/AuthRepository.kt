package com.android.swingmusic.auth.domain.repository

import com.android.swingmusic.auth.data.util.Resource
import com.android.swingmusic.auth.domain.model.CreateUserResult
import com.android.swingmusic.auth.domain.model.LogInResult

interface AuthRepository {

    fun getBaseUrl(): String?

    suspend fun storeBaseUrl(url: String)

    fun getAccessToken(): String?

    fun getRefreshToken(): String?

    suspend fun storeAuthTokens(accessToken: String, refreshToken: String)

    fun createUser(username: String, password: String): Resource<CreateUserResult>

    fun logInWithUsernameAndPassword(username: String, password: String): Resource<LogInResult>

    fun processQrCodeData(encoded: String): Pair<String, String> // Pair of <Url, Code>

    fun logInWithQrCode(serverUrl: String, pairCode: String): Resource<LogInResult>
}
