package com.android.swingmusic.profile.presentation.event

internal sealed interface AboutUiEvent {
    data object OnBackClicked : AboutUiEvent
    data object OnReportProblemClicked : AboutUiEvent
    data object OnSourceCodeClicked : AboutUiEvent
}
