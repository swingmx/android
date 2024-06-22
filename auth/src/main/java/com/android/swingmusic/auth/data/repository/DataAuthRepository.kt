package com.android.swingmusic.auth.data.repository

import com.android.swingmusic.auth.data.datastore.AuthTokensDataStore
import com.android.swingmusic.auth.data.tokenmanager.AuthTokenManager
import com.android.swingmusic.auth.data.util.Resource
import com.android.swingmusic.auth.domain.model.CreateUserResult
import com.android.swingmusic.auth.domain.model.LogInResult
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.database.data.dao.BaseUrlDao
import com.android.swingmusic.database.data.mapper.toEntity
import com.android.swingmusic.database.data.mapper.toModel
import com.android.swingmusic.database.domain.model.BaseUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class DataAuthRepository @Inject constructor(
    private val authTokensDataStore: AuthTokensDataStore,
    private val baseUrlDao: BaseUrlDao
) : AuthRepository {
    init {
        if (AuthTokenManager.accessToken == null)
            AuthTokenManager.accessToken = this.getAccessToken()
    }

    override fun getBaseUrl(): String? {
        // TODO: Try async await or validate the use of runBlocking
        return runBlocking(Dispatchers.IO) {
            baseUrlDao.getBaseUrl()?.toModel()?.url
        }
    }

    override suspend fun storeBaseUrl(url: String) {
        baseUrlDao.insertBaseUrl(BaseUrl(url = "${url}/").toEntity()) // BASE_URL must end with "/"
    }

    override fun getAccessToken(): String? {
        return runBlocking(Dispatchers.IO) {
            authTokensDataStore.accessTokenFlow.firstOrNull()
        }
    }

    override fun getRefreshToken(): String? {
        return runBlocking(Dispatchers.IO) {
            authTokensDataStore.refreshTokenFlow.firstOrNull()
        }
    }

    override suspend fun storeAuthTokens(accessToken: String, refreshToken: String) {
        authTokensDataStore.updateAuthTokens(accessToken, refreshToken)
    }

    override fun createUser(username: String, password: String): Resource<CreateUserResult> {
        TODO("Not yet implemented")
    }

    override fun logInWithUsernameAndPassword(
        username: String,
        password: String
    ): Resource<LogInResult> {
        TODO("Not yet implemented")
    }

    override fun processQrCodeData(encoded: String): Pair<String, String> {
        TODO("Not yet implemented")
    }

    override fun logInWithQrCode(serverUrl: String, pairCode: String): Resource<LogInResult> {
        TODO("Not yet implemented")
    }
}
