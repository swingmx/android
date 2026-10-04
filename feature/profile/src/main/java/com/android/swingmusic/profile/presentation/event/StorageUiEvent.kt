package com.android.swingmusic.profile.presentation.event

internal sealed interface StorageUiEvent {
    data object OnBackClicked : StorageUiEvent
    data object OnClearImageCache : StorageUiEvent
}
