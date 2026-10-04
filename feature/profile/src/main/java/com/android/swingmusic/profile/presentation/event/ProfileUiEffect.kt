package com.android.swingmusic.profile.presentation.event

internal sealed class ProfileUiEffect {
    data object NavigateBack : ProfileUiEffect()
    data object NavigateToStats : ProfileUiEffect()
    data object NavigateToLibrary : ProfileUiEffect()
    data object NavigateToPairDevice : ProfileUiEffect()
    data object NavigateToSettings : ProfileUiEffect()
    data class CopyToClipboard(val text: String) : ProfileUiEffect()
}
