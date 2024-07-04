package com.android.swingmusic.auth.presentation.state

import com.android.swingmusic.auth.presentation.util.AuthError

data class AuthUiState(
    val baseUrl: String? = null,
    val authState: AuthState = AuthState.LOGGED_OUT,
    val isLoading: Boolean = false,
    val authError: AuthError = AuthError.None
)
