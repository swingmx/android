package com.android.swingmusic.auth.data.repository

import com.android.swingmusic.auth.data.api.service.AuthApiService
import com.android.swingmusic.auth.data.datastore.AuthTokensDataStore
import com.android.swingmusic.auth.data.mapper.toModel
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.data.util.Resource
import com.android.swingmusic.auth.domain.model.CreateUserRequest
import com.android.swingmusic.auth.domain.model.LogInRequest
import com.android.swingmusic.auth.domain.model.LogInResult
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.database.data.dao.BaseUrlDao
import com.android.swingmusic.database.data.dao.UserDao
import com.android.swingmusic.database.data.mapper.toEntity
import com.android.swingmusic.database.data.mapper.toModel
import com.android.swingmusic.database.domain.model.BaseUrl
import com.android.swingmusic.database.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class DataAuthRepository @Inject constructor(
    private val authTokensDataStore: AuthTokensDataStore,
    private val authApiService: AuthApiService,
    private val baseUrlDao: BaseUrlDao,
    private val userDao: UserDao
) : AuthRepository {
    init {
        if (AuthTokenHolder.accessToken == null) {
            getAccessToken()
        }
    }

    override fun getBaseUrl(): String? {
        // TODO: Try async await or validate the use of runBlocking
        return runBlocking(Dispatchers.IO) {
            baseUrlDao.getBaseUrl()?.toModel()?.url
        }
    }

    override suspend fun storeBaseUrl(url: String) {
        baseUrlDao.insertBaseUrl(BaseUrl(url = "$url/").toEntity()) // BASE_URL must end with '/'
    }

    override fun getAccessToken(): String? {
        runBlocking(Dispatchers.IO) {
            AuthTokenHolder.accessToken = authTokensDataStore.accessToken.firstOrNull()
        }
        return AuthTokenHolder.accessToken
    }

    override fun getRefreshToken(): String? {
        runBlocking(Dispatchers.IO) {
            AuthTokenHolder.refreshToken = authTokensDataStore.refreshToken.firstOrNull()
        }
        return AuthTokenHolder.refreshToken
    }

    override suspend fun storeAuthTokens(
        accessToken: String,
        refreshToken: String,
        loggedInAs: String,
        maxAge: Long
    ) {
        AuthTokenHolder.accessToken = accessToken
        AuthTokenHolder.refreshToken = refreshToken
        authTokensDataStore.updateAuthTokens(accessToken, refreshToken, loggedInAs, maxAge)
    }

    override suspend fun getLoggedInUser(): User? {
        return userDao.getLoggedInUser()?.toModel()
    }

    override suspend fun storeLoggedInUser(user: User) {
        userDao.insertLoggedInUser(user.toEntity())
    }

    override suspend fun createUser(
        username: String,
        password: String,
        email: String,
        roles: List<String>
    ): Resource<User> {
        return try {
            Resource.Loading<User>()

            val request = CreateUserRequest(
                email = email,
                username = username,
                password = password,
                roles = roles
            )
            val result = authApiService.createUser(
                bearerAccessToken = "Bearer " + (AuthTokenHolder.accessToken ?: getAccessToken()),
                createUserRequest = request
            ).toModel()

            Resource.Success(data = result)
        } catch (e: Exception) {
            Resource.Error(message = "Failed to create user")
        }
    }

    override suspend fun logInWithUsernameAndPassword(
        baseUrl: String,
        username: String,
        password: String
    ): Resource<LogInResult> {
        return try {
            Resource.Loading<LogInResult>()

            val logInRequest = LogInRequest(username = username, password = password)
            val result = authApiService.logInWithUsernameAndPassword(
                url = "$baseUrl/auth/pair",
                logInRequest = logInRequest
            ).toModel()

            Resource.Success(result)

        } catch (e: Exception) {
            Resource.Error(message = "Login Failed")
        }
    }

    override fun processQrCodeData(encoded: String): Pair<String, String> {
        // sampleEncodedData = "http://localhost:1970 C0dE1" -> separated by " "
        val decodedData = encoded.split(Regex(" "), 2)
        return if (decodedData.size != 2) Pair("", "") // Leave empty to indicate error
        else Pair(decodedData[0], decodedData[1])
    }

    override suspend fun logInWithQrCode(
        url: String,
        pairCode: String
    ): Resource<LogInResult> {
        return try {
            Resource.Loading<LogInResult>()

            Timber.e("LOGGING IN... URL -> $url/auth/pair WITH CODE -> $pairCode")

            val result = authApiService.logInWithQrCode(
                url = "$url/auth/pair",
                pairCode = pairCode
            ).toModel()

            Resource.Success(data = result)
        } catch (e: HttpException) {

            Resource.Error(message = "Pairing Failed")
        } catch (e: Exception) {

            Resource.Error(message = e.message ?: "Pairing Failed")
        }
    }
}
