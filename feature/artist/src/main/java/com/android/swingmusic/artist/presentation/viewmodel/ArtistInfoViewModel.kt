package com.android.swingmusic.artist.presentation.viewmodel

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import com.android.swingmusic.settings.domain.repository.AppSettingsRepository
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.artist.domain.repository.ArtistRepository
import com.android.swingmusic.artist.presentation.event.ArtistInfoUiEvent
import com.android.swingmusic.artist.presentation.state.ArtistInfoState
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.AlbumsAndAppearances
import com.android.swingmusic.core.domain.model.ArtistExpanded
import com.android.swingmusic.core.domain.model.ArtistInfo
import com.android.swingmusic.player.domain.repository.PLayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistInfoViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val pLayerRepository: PLayerRepository,
    private val appSettingsRepository: AppSettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** Search On logos in brand colours instead of monochrome; switched by a hidden long press. */
    val searchOnBrandColours: StateFlow<Boolean> = appSettingsRepository.searchOnBrandColours
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun toggleSearchOnBrandColours() {
        viewModelScope.launch { appSettingsRepository.setSearchOnBrandColours(!searchOnBrandColours.value) }
    }
    private val _artistInfoState: MutableStateFlow<ArtistInfoState> =
        MutableStateFlow(ArtistInfoState())
    val artistInfoState: StateFlow<ArtistInfoState> = _artistInfoState

    init {
        savedStateHandle.get<String>("artistHash")?.let { hash ->
            getArtistInfo(hash)
            getSimilarArtists(hash)
        }
    }

    private fun getArtistInfo(artistHash: String) {
        viewModelScope.launch {
            artistRepository.getArtistInfo(artistHash).collectLatest { res ->
                _artistInfoState.value = _artistInfoState.value.copy(
                    infoResource = res
                )

                if (res is Resource.Success) {
                    _artistInfoState.value = _artistInfoState.value.copy(
                        requiresReload = false
                    )
                }
            }
        }
    }

    private fun getSimilarArtists(artistHash: String) {
        viewModelScope.launch {
            artistRepository.getSimilarArtists(artistHash).collectLatest { res ->
                _artistInfoState.value = _artistInfoState.value.copy(
                    similarArtistsResource = res
                )
            }
        }
    }

    private fun toggleArtistFavorite(artistHash: String, isFavorite: Boolean) {
        viewModelScope.launch {
            val emptyAlbumsAndAppearances = AlbumsAndAppearances(
                albums = emptyList(),
                appearances = emptyList(),
                artistName = "",
                compilations = emptyList(),
                singlesAndEps = emptyList()
            )
            val emptyArtistExpanded = ArtistExpanded(
                albumCount = 0,
                artistHash = "",
                color = "",
                duration = 0,
                genres = emptyList(),
                image = "",
                isFavorite = false,
                name = "",
                trackCount = 0
            )

            // Optimistically update the UI
            _artistInfoState.value = _artistInfoState.value.copy(
                infoResource = Resource.Success(
                    ArtistInfo(
                        albumsAndAppearances = _artistInfoState.value.infoResource.data?.albumsAndAppearances
                            ?: emptyAlbumsAndAppearances,
                        artist = _artistInfoState.value.infoResource.data?.artist?.copy(
                            isFavorite = !isFavorite
                        ) ?: emptyArtistExpanded,
                        tracks = _artistInfoState.value.infoResource.data?.tracks ?: emptyList()
                    )
                )
            )

            val request = if (isFavorite) {
                artistRepository.removeArtistFromFavorite(artistHash)
            } else {
                artistRepository.addArtistToFavorite(artistHash)
            }

            request.collectLatest {
                when (it) {
                    is Resource.Loading -> {}

                    is Resource.Success -> {
                        _artistInfoState.value = _artistInfoState.value.copy(
                            infoResource = Resource.Success(
                                ArtistInfo(
                                    albumsAndAppearances = _artistInfoState.value.infoResource.data?.albumsAndAppearances
                                        ?: emptyAlbumsAndAppearances,
                                    artist = _artistInfoState.value.infoResource.data?.artist?.copy(
                                        isFavorite = it.data ?: false
                                    ) ?: emptyArtistExpanded,
                                    tracks = _artistInfoState.value.infoResource.data?.tracks
                                        ?: emptyList()
                                )
                            )
                        )
                    }

                    is Resource.Error -> {
                        _artistInfoState.value = _artistInfoState.value.copy(
                            infoResource = Resource.Success(
                                ArtistInfo(
                                    albumsAndAppearances = _artistInfoState.value.infoResource.data?.albumsAndAppearances
                                        ?: emptyAlbumsAndAppearances,
                                    artist = _artistInfoState.value.infoResource.data?.artist?.copy(
                                        isFavorite = isFavorite
                                    ) ?: emptyArtistExpanded,
                                    tracks = _artistInfoState.value.infoResource.data?.tracks
                                        ?: emptyList()
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    private fun toggleArtistTrackFavorite(trackHash: String, isFavorite: Boolean) {
        viewModelScope.launch {
            val emptyAlbumsAndAppearances = AlbumsAndAppearances(
                albums = emptyList(),
                appearances = emptyList(),
                artistName = "",
                compilations = emptyList(),
                singlesAndEps = emptyList()
            )
            val emptyArtistExpanded = ArtistExpanded(
                albumCount = 0,
                artistHash = "",
                color = "",
                duration = 0,
                genres = emptyList(),
                image = "",
                isFavorite = false,
                name = "",
                trackCount = 0
            )

            // Optimistically update the UI
            _artistInfoState.value = _artistInfoState.value.copy(
                infoResource = Resource.Success(
                    ArtistInfo(
                        albumsAndAppearances = _artistInfoState.value.infoResource.data?.albumsAndAppearances
                            ?: emptyAlbumsAndAppearances,
                        artist = _artistInfoState.value.infoResource.data?.artist
                            ?: emptyArtistExpanded,
                        tracks = _artistInfoState.value.infoResource.data?.tracks?.map { track ->
                            if (track.trackHash == trackHash) {
                                track.copy(isFavorite = !isFavorite)
                            } else {
                                track
                            }
                        } ?: emptyList()
                    )
                )
            )

            val request = if (isFavorite) {
                pLayerRepository.removeTrackFromFavorite(trackHash)
            } else {
                pLayerRepository.addTrackToFavorite(trackHash)
            }

            request.collectLatest {
                when (it) {
                    is Resource.Loading -> {}

                    is Resource.Success -> {
                        _artistInfoState.value = _artistInfoState.value.copy(
                            infoResource = Resource.Success(
                                ArtistInfo(
                                    albumsAndAppearances = _artistInfoState.value.infoResource.data?.albumsAndAppearances
                                        ?: emptyAlbumsAndAppearances,
                                    artist = _artistInfoState.value.infoResource.data?.artist
                                        ?: emptyArtistExpanded,
                                    tracks = _artistInfoState.value.infoResource.data?.tracks?.map { track ->
                                        if (track.trackHash == trackHash) {
                                            track.copy(isFavorite = it.data ?: false)
                                        } else {
                                            track
                                        }
                                    } ?: emptyList()
                                )
                            )
                        )
                    }

                    is Resource.Error -> {
                        _artistInfoState.value = _artistInfoState.value.copy(
                            infoResource = Resource.Success(
                                ArtistInfo(
                                    albumsAndAppearances = _artistInfoState.value.infoResource.data?.albumsAndAppearances
                                        ?: emptyAlbumsAndAppearances,
                                    artist = _artistInfoState.value.infoResource.data?.artist
                                        ?: emptyArtistExpanded,
                                    tracks = _artistInfoState.value.infoResource.data?.tracks?.map { track ->
                                        if (track.trackHash == trackHash) {
                                            track.copy(isFavorite = isFavorite)
                                        } else {
                                            track
                                        }
                                    } ?: emptyList()
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun onArtistInfoUiEvent(event: ArtistInfoUiEvent) {
        when (event) {
            is ArtistInfoUiEvent.OnLoadArtistInfo -> {
                getArtistInfo(event.artistHash)
                getSimilarArtists(event.artistHash)
            }

            is ArtistInfoUiEvent.OnRefresh -> {
                onArtistInfoUiEvent(ArtistInfoUiEvent.OnLoadArtistInfo(event.artistHash))
            }

            is ArtistInfoUiEvent.OnToggleArtistFavorite -> {
                toggleArtistFavorite(event.artistHash, event.isFavorite)
            }

            is ArtistInfoUiEvent.ToggleArtistTrackFavorite -> {
                toggleArtistTrackFavorite(event.trackHash, event.isFavorite)
            }

            else -> {

            }
        }
    }
}
