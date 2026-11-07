package com.android.swingmusic.profile.presentation.event

internal sealed interface ChangelogUiEvent {
    data object ShowAllChangesToggled : ChangelogUiEvent
    data class ReleaseToggled(val versionCode: Int) : ChangelogUiEvent
    data class LinkClicked(val url: String) : ChangelogUiEvent
    data object Dismissed : ChangelogUiEvent
}
