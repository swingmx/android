package com.android.swingmusic.playlist.presentation.event

internal sealed class AddToPlaylistUiEffect {
    data class Done(val message: String) : AddToPlaylistUiEffect()
}
