package com.android.swingmusic.profile.presentation.event

internal sealed class AboutUiEffect {
    data object NavigateBack : AboutUiEffect()
    data class OpenUrl(val url: String) : AboutUiEffect()
}
