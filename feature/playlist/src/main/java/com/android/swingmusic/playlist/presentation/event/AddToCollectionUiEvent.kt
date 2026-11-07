package com.android.swingmusic.playlist.presentation.event

import com.android.swingmusic.playlist.domain.Collection

internal sealed interface AddToCollectionUiEvent {
    data object OnRetry : AddToCollectionUiEvent
    data class OnCollectionClicked(val collection: Collection) : AddToCollectionUiEvent
    data object OnNewCollectionClicked : AddToCollectionUiEvent
    data object OnCloseNewCollection : AddToCollectionUiEvent
    data class OnNewCollectionNameChanged(val name: String) : AddToCollectionUiEvent
    data object OnCreateCollection : AddToCollectionUiEvent
}

internal sealed class AddToCollectionUiEffect {
    data class Done(val message: String) : AddToCollectionUiEffect()
}
