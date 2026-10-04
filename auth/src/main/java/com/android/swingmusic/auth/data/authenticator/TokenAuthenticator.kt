package com.android.swingmusic.auth.data.authenticator

import com.android.swingmusic.auth.domain.model.SessionEndReason
import com.android.swingmusic.auth.domain.model.TokenRefreshResult
import com.android.swingmusic.auth.domain.repository.AuthRepository
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * On a 401, refreshes the access token once and retries the request.
 * If the server rejects the refresh token, the session is ended.
 */
class TokenAuthenticator @Inject constructor(
    private val authRepository: Lazy<AuthRepository>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (UNAUTHENTICATED_PATHS.any { path.endsWith(it) }) return null
        if (response.priorResponse != null) return null

        val failedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()
            .takeUnless { it.isNullOrEmpty() } ?: return null

        val freshToken = runBlocking {
            val repository = authRepository.get()
            if (!repository.isLoggedIn()) return@runBlocking null

            val current = repository.getAccessToken()
            if (!current.isNullOrEmpty() && current != failedToken) return@runBlocking current

            when (val result = repository.refreshTokens()) {
                is TokenRefreshResult.Refreshed -> result.accessToken
                is TokenRefreshResult.Rejected -> {
                    repository.endSession(SessionEndReason.EXPIRED)
                    null
                }

                is TokenRefreshResult.Failed -> null
            }
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $freshToken")
            .build()
    }

    private companion object {
        val UNAUTHENTICATED_PATHS = listOf("auth/refresh", "auth/login", "auth/pair")
    }
}
