package com.android.swingmusic.profile.presentation.event

internal sealed interface LibraryManagementUiEvent {
    data object OnBackClicked : LibraryManagementUiEvent
    data object OnQuickScanClicked : LibraryManagementUiEvent
    data object OnFullScanClicked : LibraryManagementUiEvent
    data object OnFullScanConfirmed : LibraryManagementUiEvent
    data object OnFullScanDismissed : LibraryManagementUiEvent
    data object OnRetryRootDirs : LibraryManagementUiEvent
}
