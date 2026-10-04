package com.android.swingmusic.profile.presentation.event

internal sealed class AccountUiEffect {
    data object NavigateBack : AccountUiEffect()
    data class ShowSnackBar(val message: String) : AccountUiEffect()
}
