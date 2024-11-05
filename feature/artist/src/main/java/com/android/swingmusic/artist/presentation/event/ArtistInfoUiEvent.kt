package com.android.swingmusic.artist.presentation.event

interface ArtistInfoUiEvent {

    data class OnLoadArtistInfo(val artistHash: String) : ArtistInfoUiEvent

    data class OnUpdateArtistHash(val artistHash: String) : ArtistInfoUiEvent

    data class OnToggleArtistFavorite(
        val artistHash: String,
        val isFavorite: Boolean
    ) : ArtistInfoUiEvent

    data class OnRefresh(val artistHash: String) : ArtistInfoUiEvent
}
