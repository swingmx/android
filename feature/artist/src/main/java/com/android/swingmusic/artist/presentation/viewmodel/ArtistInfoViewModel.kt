package com.android.swingmusic.artist.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.artist.domain.repository.ArtistRepository
import com.android.swingmusic.artist.presentation.event.ArtistInfoUiEvent
import com.android.swingmusic.artist.presentation.state.ArtistInfoState
import com.android.swingmusic.core.data.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistInfoViewModel @Inject constructor(
    private val artistRepository: ArtistRepository
) : ViewModel() {
    val artistInfoState: MutableState<ArtistInfoState> = mutableStateOf(ArtistInfoState())

    private fun getArtistInfo(artistHash: String) {
        viewModelScope.launch {
            artistRepository.getArtistInfo(artistHash).collectLatest { res ->
                artistInfoState.value = artistInfoState.value.copy(
                    infoResource = res
                )

                if (res is Resource.Success){
                    artistInfoState.value = artistInfoState.value.copy(
                        requiresReload = false
                    )
                }
            }
        }
    }

    private fun getSimilarArtists(artistHash: String) {
        viewModelScope.launch {
            artistRepository.getSimilarArtists(artistHash).collectLatest { res ->
                artistInfoState.value = artistInfoState.value.copy(
                    similarArtistsResource = res
                )
            }
        }
    }

    fun onArtistInfoUiEvent(event: ArtistInfoUiEvent) {
        when (event) {
            is ArtistInfoUiEvent.OnLoadArtistInfo -> {
                getArtistInfo(event.artistHash)
                getSimilarArtists(event.artistHash)
            }
        }
    }
}
