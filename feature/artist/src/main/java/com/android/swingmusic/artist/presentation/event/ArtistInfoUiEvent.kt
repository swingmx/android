package com.android.swingmusic.artist.presentation.event

import com.android.swingmusic.core.domain.model.Track

interface ArtistInfoUiEvent {

    data class OnLoadArtistInfo(val artistHash: String) : ArtistInfoUiEvent

    object OnBackPress: ArtistInfoUiEvent

    data class OnToggleArtistFavorite(val isFavorite: Boolean): ArtistInfoUiEvent

    data class OnShuffle(val tracks: List<Track>): ArtistInfoUiEvent

    data class OnPlayAllTracks(val tracks: List<Track>): ArtistInfoUiEvent

    data class OnPlaySingleTrack(val index: Int, val tracks: List<Track>): ArtistInfoUiEvent

    data class OnViewAllTracks(val artistHash: String): ArtistInfoUiEvent

    data class OnViewAllAlbums(val artistHash: String): ArtistInfoUiEvent

    data class OnViewAllEPAndSingles(val artistHash: String): ArtistInfoUiEvent

    data class OnViewAllAppearances(val artistHash: String): ArtistInfoUiEvent

    data class OnClickAlbum(val albumHash: String): ArtistInfoUiEvent

    data class OnClickSimilarArtist(val artistHash: String): ArtistInfoUiEvent
}
