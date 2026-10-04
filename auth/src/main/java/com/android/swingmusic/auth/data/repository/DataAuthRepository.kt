package com.android.swingmusic.auth.data.repository

import android.content.Context
import com.android.swingmusic.auth.data.api.service.AuthApiService
import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.datastore.AuthTokensDataStore
import com.android.swingmusic.auth.data.mapper.toModel
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.data.workmanager.cancelTokenRefreshWork
import com.android.swingmusic.auth.data.workmanager.scheduleTokenRefreshWork
import com.android.swingmusic.auth.domain.model.AllUsers
import com.android.swingmusic.auth.domain.model.CreateUserRequest
import com.android.swingmusic.auth.domain.model.LogInRequest
import com.android.swingmusic.auth.domain.model.LogInResult
import com.android.swingmusic.auth.domain.model.SessionEndReason
import com.android.swingmusic.auth.domain.model.TokenRefreshResult
import com.android.swingmusic.auth.domain.model.UpdateProfileRequest
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.database.data.dao.BaseUrlDao
import com.android.swingmusic.database.data.dao.LastPlayedTrackDao
import com.android.swingmusic.database.data.dao.QueueDao
import com.android.swingmusic.database.data.dao.UserDao
import com.android.swingmusic.database.data.mapper.toEntity
import com.android.swingmusic.database.data.mapper.toModel
import com.android.swingmusic.database.domain.model.BaseUrl
import com.android.swingmusic.database.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class DataAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authTokensDataStore: AuthTokensDataStore,
    private val authApiService: AuthApiService,
    private val baseUrlDao: BaseUrlDao,
    private val userDao: UserDao,
    private val queueDao: QueueDao,
    private val lastPlayedTrackDao: LastPlayedTrackDao
) : AuthRepository {

    private val sessionEndedChannel = Channel<SessionEndReason>(Channel.BUFFERED)
    override val sessionEnded: Flow<SessionEndReason> = sessionEndedChannel.receiveAsFlow()

    private val refreshMutex = Mutex()
    private val endSessionMutex = Mutex()

    override suspend fun initializeBaseUrlAndAuthTokens() {
        if (AuthTokenHolder.accessToken == null) {
            getAccessToken()
        }
        if (BaseUrlHolder.baseUrl == null) {
            getBaseUrl()
        }
    }

    override suspend fun getBaseUrl(): String? {
        if (BaseUrlHolder.baseUrl == null) {
            BaseUrlHolder.baseUrl = withContext(Dispatchers.IO) {
                baseUrlDao.getBaseUrl()?.toModel()?.url
            }
        }
        return BaseUrlHolder.baseUrl
    }

    override suspend fun storeBaseUrl(url: String) {
        val baseUrl = "${url.trimEnd('/')}/" // BASE_URL must end with exactly one '/'
        baseUrlDao.insertBaseUrl(BaseUrl(url = baseUrl).toEntity())
        BaseUrlHolder.baseUrl = baseUrl
    }

    override suspend fun getAccessToken(): String? {
        if (AuthTokenHolder.accessToken == null) {
            AuthTokenHolder.accessToken = authTokensDataStore.accessToken.firstOrNull()
        }
        return AuthTokenHolder.accessToken
    }

    override suspend fun getRefreshToken(): String? {
        AuthTokenHolder.refreshToken = authTokensDataStore.refreshToken.firstOrNull()
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

    override suspend fun refreshTokens(): TokenRefreshResult = refreshMutex.withLock {
        val refreshToken = getRefreshToken()
        if (refreshToken.isNullOrEmpty()) return TokenRefreshResult.Rejected

        return try {
            val result = authApiService.refreshTokens(
                url = "${getBaseUrl()}auth/refresh",
                bearerRefreshToken = "Bearer $refreshToken"
            ).toModel()

            if (result.accessToken.isEmpty()) {
                TokenRefreshResult.Failed
            } else {
                storeAuthTokens(result.accessToken, result.refreshToken, result.maxAge)
                TokenRefreshResult.Refreshed(result.accessToken)
            }
        } catch (e: HttpException) {
            if (e.code() == 401 || e.code() == 422) TokenRefreshResult.Rejected
            else TokenRefreshResult.Failed
        } catch (e: Exception) {
            Timber.tag("AUTH").e(e, "Token refresh failed")
            TokenRefreshResult.Failed
        }
    }

    override suspend fun getAllUsers(baseUrl: String): Flow<Resource<AllUsers>> {
        return flow {
            try {
                emit(Resource.Loading())

                val result = authApiService.getAllUsers("$baseUrl/auth/users").toModel()
                emit(Resource.Success(data = result))

            } catch (e: HttpException) {
                emit(Resource.Error(message = "Connection Failed!"))
            } catch (e: Exception) {
                emit(Resource.Error(message = "Failed to load users!"))
            }
        }
    }

    override suspend fun getLoggedInUser(): User? {
        return userDao.getLoggedInUser()?.toModel()

    }

    private suspend fun storeLoggedInUser(user: User) {
        userDao.clearLoggedInUser()
        userDao.insertLoggedInUser(user.toEntity())
    }

    override suspend fun fetchCurrentUser(): Resource<User> {
        return try {
            val user = authApiService.getCurrentUser(
                url = "${getBaseUrl()}auth/user",
                bearerAccessToken = "Bearer ${getAccessToken()}"
            ).toModel()
            storeLoggedInUser(user)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(data = getLoggedInUser(), message = "Couldn't load your profile")
        }
    }

    override suspend fun updateProfile(username: String?, password: String?): Resource<User> {
        return try {
            val user = authApiService.updateProfile(
                url = "${getBaseUrl()}auth/profile/update",
                bearerAccessToken = "Bearer ${getAccessToken()}",
                updateProfileRequest = UpdateProfileRequest(username = username, password = password)
            ).toModel()
            storeLoggedInUser(user)
            Resource.Success(user)
        } catch (e: HttpException) {
            val serverMessage = runCatching {
                JSONObject(e.response()?.errorBody()?.string().orEmpty()).optString("msg")
            }.getOrNull()
            Resource.Error(message = serverMessage?.takeIf { it.isNotBlank() } ?: "Couldn't update your profile.")
        } catch (e: IOException) {
            Resource.Error(message = "Check your connection to the server.")
        } catch (e: Exception) {
            Timber.tag("AUTH").e(e)
            Resource.Error(message = "Couldn't update your profile.")
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        val flag = authTokensDataStore.isLoggedIn.first()
        if (flag != null) return flag

        // Installs from before the flag existed: a stored token means logged in.
        val hasToken = !getAccessToken().isNullOrEmpty()
        if (hasToken) authTokensDataStore.setLoggedIn()
        return hasToken
    }

    override suspend fun markLoggedIn() {
        authTokensDataStore.setLoggedIn()
        scheduleTokenRefreshWork(context)
    }

    override suspend fun isSessionExpired(): Boolean = authTokensDataStore.sessionExpired.first()

    override suspend fun clearSessionExpired() = authTokensDataStore.clearSessionExpired()

    // GET /auth/logout only clears the web cookie; bearer tokens aren't revoked server-side.
    override suspend fun logOut() = endSession(SessionEndReason.LOGGED_OUT)

    override suspend fun endSession(reason: SessionEndReason) {
        withContext(NonCancellable) {
            endSessionMutex.withLock {
                // Nothing to end: also covers fresh installs, where the flag isn't set yet.
                if (!isLoggedIn()) return@withLock

                authTokensDataStore.clearSession(expired = reason == SessionEndReason.EXPIRED)
                AuthTokenHolder.accessToken = null
                AuthTokenHolder.refreshToken = null

                withContext(Dispatchers.IO) {
                    userDao.clearLoggedInUser()
                    queueDao.clearQueue()
                    lastPlayedTrackDao.clearLastPlayedTrack()
                }
                cancelTokenRefreshWork(context)

                sessionEndedChannel.send(reason)
            }
        }
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
