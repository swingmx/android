package com.android.swingmusic.auth.data.repository

import com.android.swingmusic.auth.data.api.service.AuthApiService
import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.datastore.AuthTokensDataStore
import com.android.swingmusic.auth.data.mapper.toModel
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.model.AllUsers
import com.android.swingmusic.auth.domain.model.CreateUserRequest
import com.android.swingmusic.auth.domain.model.LogInRequest
import com.android.swingmusic.auth.domain.model.LogInResult
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.database.data.dao.BaseUrlDao
import com.android.swingmusic.database.data.dao.UserDao
import com.android.swingmusic.database.data.mapper.toEntity
import com.android.swingmusic.database.data.mapper.toModel
import com.android.swingmusic.database.domain.model.BaseUrl
import com.android.swingmusic.database.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
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
        if (BaseUrlHolder.baseUrl == null) {
            getBaseUrl()
        }
    }

    override fun getBaseUrl(): String? {
        // TODO: Try async await or validate the use of runBlocking
        if (BaseUrlHolder.baseUrl == null) {
            runBlocking(Dispatchers.IO) {
                BaseUrlHolder.baseUrl = baseUrlDao.getBaseUrl()?.toModel()?.url
            }
        }
        return BaseUrlHolder.baseUrl
    }

    override suspend fun storeBaseUrl(url: String) {
        baseUrlDao.insertBaseUrl(BaseUrl(url = "$url/").toEntity()) // BASE_URL must end with '/'
    }

    override fun getAccessToken(): String? {
        if (AuthTokenHolder.accessToken == null) {
            runBlocking(Dispatchers.IO) {
                AuthTokenHolder.accessToken = authTokensDataStore.accessToken.firstOrNull()
            }
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
        maxAge: Long
    ) {
        AuthTokenHolder.accessToken = accessToken
        AuthTokenHolder.refreshToken = refreshToken
        authTokensDataStore.updateAuthTokens(accessToken, refreshToken, maxAge)
    }

    override suspend fun getFreshTokensFromServer(): LogInResult? {
        return try {
            val refreshToken = getRefreshToken()
            val baseUrl = BaseUrlHolder.baseUrl ?: getBaseUrl()

            val result = authApiService.refreshTokens(
                url = "$baseUrl/auth/refresh",
                bearerRefreshToken = "Bearer $refreshToken"
            )

            result.toModel()
        } catch (e: Exception) {
            Timber.e(message = "Failed to load users!")
            null
        } catch (e: HttpException) {
            Timber.e(message = "Connection Failed!")
            null
        }
    }

    override suspend fun getAllUsers(baseUrl: String): Flow<Resource<AllUsers>> {
        return flow {
            try {
                emit(Resource.Loading())

                val result = authApiService.getAllUsers("$baseUrl/auth/users").toModel()
                emit(Resource.Success(data = result))

            } catch (e: Exception) {
                emit(Resource.Error(message = "Failed to load users!"))
            } catch (e: HttpException) {
                emit(Resource.Error(message = "Connection Failed!"))
            }
        }
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
    ): Flow<Resource<User>> {
        return flow {
            try {
                emit(Resource.Loading<User>())

                val baseUrl = BaseUrlHolder.baseUrl ?: getBaseUrl()

                val request = CreateUserRequest(
                    email = email,
                    username = username,
                    password = password,
                    roles = roles
                )
                val result = authApiService.createUser(
                    url = "${baseUrl}auth/profile/create",
                    bearerAccessToken = "Bearer " + (AuthTokenHolder.accessToken
                        ?: getAccessToken()),
                    createUserRequest = request
                ).toModel()

                emit(Resource.Success(data = result))

            } catch (e: Exception) {
                emit(Resource.Error(message = "Failed to create user"))
            }
        }
    }

    override suspend fun logInWithUsernameAndPassword(
        baseUrl: String,
        username: String,
        password: String
    ): Flow<Resource<LogInResult>> {
        return flow {
            try {
                emit(Resource.Loading<LogInResult>())

                val logInRequest = LogInRequest(username = username, password = password)
                val result = authApiService.logInWithUsernameAndPassword(
                    url = "$baseUrl/auth/login",
                    logInRequest = logInRequest
                ).toModel()

                emit(Resource.Success(result))

            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    401 -> "INCORRECT PASSWORD!"
                    404 -> "USER NOT FOUND!"
                    else -> "LOGIN FAILED!"
                }
                emit(Resource.Error(message = msg))

            } catch (e: Exception) {
                emit(Resource.Error(message = "LOGIN FAILED!"))
            }
        }
    }

    override fun processQrCodeData(encoded: String): Pair<String, String> {
        //val sampleEncodedData = "http://localhost:1970 C0dE1" -> separated by " "
        val pattern = " "
        val decodedData = encoded.split(Regex(pattern), 2)
        return if (decodedData.size != 2) Pair("", "") else Pair(decodedData[0], decodedData[1])
    }

    override suspend fun logInWithQrCode(
        url: String,
        pairCode: String
    ): Flow<Resource<LogInResult>> {
        return flow {
            try {
                emit(Resource.Loading<LogInResult>())

                val result = authApiService.logInWithQrCode(
                    url = "$url/auth/pair",
                    pairCode = pairCode
                ).toModel()

                emit(Resource.Success(data = result))

            } catch (e: HttpException) {

                emit(Resource.Error(message = "PAIRING FAILED"))
            } catch (e: Exception) {

                emit(Resource.Error(message = "PAIRING FAILED"))
            }
        }
    }
}
