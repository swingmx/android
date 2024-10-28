package com.android.swingmusic.artist.presentation.event

interface ArtistInfoUiEvent {
    data class OnInit(val artistHash: String) : ArtistInfoUiEvent
}