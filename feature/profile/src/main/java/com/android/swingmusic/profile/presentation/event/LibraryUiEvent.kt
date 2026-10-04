package com.android.swingmusic.profile.presentation.event

internal sealed interface LibraryUiEvent {
    data object OnBackClicked : LibraryUiEvent
    data object OnFoldersClicked : LibraryUiEvent
    data object OnQuickScanClicked : LibraryUiEvent
    data object OnFullScanClicked : LibraryUiEvent
    data object OnFullScanConfirmed : LibraryUiEvent
    data object OnFullScanDismissed : LibraryUiEvent
    data object OnRetryRootDirs : LibraryUiEvent
}
