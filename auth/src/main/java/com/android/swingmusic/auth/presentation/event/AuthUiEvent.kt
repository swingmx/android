package com.android.swingmusic.auth.presentation.event

interface AuthUiEvent {
    data class LogInWithUsernameAndPassword(
        val username: String,
        val password: String
    ) : AuthUiEvent

    data class LogInWithQrCode(val encoded: String) : AuthUiEvent

    object ResetStates: AuthUiEvent
}
