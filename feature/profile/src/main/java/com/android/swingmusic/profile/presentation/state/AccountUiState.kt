package com.android.swingmusic.profile.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
internal data class AccountUiState(
    val currentUsername: String = "",
    val username: String = "",
    val isSavingUsername: Boolean = false,
    val usernameError: String? = null,

    val newPassword: String = "",
    val confirmPassword: String = "",
    val isSavingPassword: Boolean = false,
    val passwordError: String? = null,
)

internal val AccountUiState.canSaveUsername: Boolean
    get() = !isSavingUsername && username.isNotBlank() && username.trim() != currentUsername

internal val AccountUiState.passwordsMismatch: Boolean
    get() = confirmPassword.isNotEmpty() && newPassword != confirmPassword

internal val AccountUiState.canUpdatePassword: Boolean
    get() = !isSavingPassword && newPassword.isNotEmpty() && newPassword == confirmPassword
