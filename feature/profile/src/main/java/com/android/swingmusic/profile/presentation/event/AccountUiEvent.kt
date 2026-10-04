package com.android.swingmusic.profile.presentation.event

internal sealed interface AccountUiEvent {
    data object OnBackClicked : AccountUiEvent
    data class OnUsernameChange(val value: String) : AccountUiEvent
    data object OnSaveUsername : AccountUiEvent
    data class OnNewPasswordChange(val value: String) : AccountUiEvent
    data class OnConfirmPasswordChange(val value: String) : AccountUiEvent
    data object OnUpdatePassword : AccountUiEvent
}
