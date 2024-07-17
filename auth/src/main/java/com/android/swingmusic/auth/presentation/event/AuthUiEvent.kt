package com.android.swingmusic.auth.presentation.event

interface AuthUiEvent {

    data class LogInWithQrCode(val encoded: String) : AuthUiEvent

    object LogInWithUsernameAndPassword : AuthUiEvent

    data class OnBaseUrlChange(val newInput: String) : AuthUiEvent

    data class OnUsernameChange(val newInput: String) : AuthUiEvent

    data class OnPasswordChange(val newInput: String) : AuthUiEvent

    object ClearErrorState : AuthUiEvent
}
