package com.android.swingmusic.profile.presentation.event

internal sealed class ChangelogUiEffect {
    data class OpenUrl(val url: String) : ChangelogUiEffect()
}
