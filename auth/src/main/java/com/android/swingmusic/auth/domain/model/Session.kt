package com.android.swingmusic.auth.domain.model

enum class SessionEndReason {
    LOGGED_OUT,
    EXPIRED
}

sealed interface TokenRefreshResult {
    data class Refreshed(val accessToken: String) : TokenRefreshResult

    /** The server rejected the refresh token: the session is over. */
    data object Rejected : TokenRefreshResult

    /** Offline or server error: keep the session and try later. */
    data object Failed : TokenRefreshResult
}
