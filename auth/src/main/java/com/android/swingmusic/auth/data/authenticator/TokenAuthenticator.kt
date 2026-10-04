package com.android.swingmusic.auth.data.authenticator

import com.android.swingmusic.auth.domain.model.SessionEndReason
import com.android.swingmusic.auth.domain.model.TokenRefreshResult
import com.android.swingmusic.auth.domain.repository.AuthRepository
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import javax.inject.Inject

/**
 * When the server rejects the access token, refreshes it once and retries the request.
 * If the server rejects the refresh token too, the session is ended.
 *
 * An interceptor rather than an OkHttp Authenticator: the server answers 401 for an
 * expired token but 422 for a malformed or wrongly signed one, and Authenticator only
 * sees 401s.
 */
class TokenAuthenticator @Inject constructor(
    private val authRepository: Lazy<AuthRepository>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val path = request.url.encodedPath
        if (UNAUTHENTICATED_PATHS.any { path.endsWith(it) }) return response
        if (!response.isTokenRejection()) return response

        val failedToken = request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()
            .takeUnless { it.isNullOrEmpty() } ?: return response

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
        } ?: return response

        response.close()
        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $freshToken")
                .build()
        )
    }

    /** 401, or a 422 shaped like a JWT error ({"msg": ...}) rather than a validation error list. */
    private fun Response.isTokenRejection(): Boolean = when (code) {
        401 -> true
        422 -> runCatching { JSONObject(peekBody(4096).string()).has("msg") }.getOrDefault(false)
        else -> false
    }

    private companion object {
        val UNAUTHENTICATED_PATHS = listOf("auth/refresh", "auth/login", "auth/pair")
    }
}
