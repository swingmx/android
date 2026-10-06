package com.android.swingmusic.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.auth.data.avatar.AvatarStore
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.HomeItem
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.home.domain.HomeRepository
import com.android.swingmusic.home.presentation.event.HomeUiEffect
import com.android.swingmusic.home.presentation.event.HomeUiEvent
import com.android.swingmusic.home.presentation.state.HomeUiState
import com.android.swingmusic.home.presentation.state.key
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val avatarStore: AvatarStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<HomeUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var loadJob: Job? = null
    private var playJob: Job? = null
    private var lastLoadedAtMs = 0L

    init {
        getSections()
        viewModelScope.launch {
            avatarStore.avatar.collect { file -> _uiState.update { it.copy(avatarPath = file?.absolutePath) } }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.OnRefresh -> refresh()
            HomeUiEvent.OnRetry -> getSections()
            HomeUiEvent.OnScreenResumed -> refreshIfStale()
            HomeUiEvent.OnProfileClicked -> _uiEffect.trySend(HomeUiEffect.NavigateToProfile)
            is HomeUiEvent.OnItemClicked -> onItemClicked(event.item)
        }
    }

    fun refresh() {
        getSections(isRefreshing = true)
    }

    private fun refreshIfStale() {
        if (loadJob?.isActive == true) return
        if (System.currentTimeMillis() - lastLoadedAtMs < STALE_AFTER_MS) return
        getSections(isSilent = true)
    }

    private fun getSections(isRefreshing: Boolean = false, isSilent: Boolean = false) {
        updateUiState {
            copy(
                isRefreshing = isRefreshing,
                isLoadingSections = !isRefreshing && !isSilent && sections.isEmpty(),
                errorLoadingSections = if (isSilent) errorLoadingSections else null,
            )
        }

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (_uiState.value.baseUrl.isEmpty()) {
                val baseUrl = homeRepository.getBaseUrl()
                updateUiState { copy(baseUrl = baseUrl) }
            }

            when (val result = homeRepository.getHomeSections()) {
                is Resource.Success -> {
                    lastLoadedAtMs = System.currentTimeMillis()
                    updateUiState {
                        copy(
                            isRefreshing = false,
                            isLoadingSections = false,
                            errorLoadingSections = null,
                            sections = result.data.orEmpty(),
                        )
                    }
                }

                is Resource.Error -> {
                    val hasSections = _uiState.value.sections.isNotEmpty()
                    updateUiState {
                        copy(
                            isRefreshing = false,
                            isLoadingSections = false,
                            errorLoadingSections = if (hasSections) null else result.message,
                        )
                    }
                    if (hasSections && !isSilent) {
                        _uiEffect.trySend(HomeUiEffect.ShowSnackBar(result.message ?: "Couldn't refresh"))
                    }
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun onItemClicked(item: HomeItem) {
        when (item) {
            is HomeItem.AlbumItem ->
                _uiEffect.trySend(HomeUiEffect.NavigateToAlbum(item.album.albumHash))

            is HomeItem.ArtistItem ->
                _uiEffect.trySend(HomeUiEffect.NavigateToArtist(item.artist.artistHash))

            is HomeItem.TrackItem ->
                _uiEffect.trySend(HomeUiEffect.PlayTracks(listOf(item.track), QueueSource.UNKNOWN))

            is HomeItem.PlaylistItem -> playFetchedTracks(
                itemKey = item.key,
                source = QueueSource.PLAYLIST(id = item.playlist.id, name = item.playlist.name),
                fetch = { homeRepository.getPlaylistTracks(item.playlist.id) }
            )

            is HomeItem.MixItem -> playFetchedTracks(
                itemKey = item.key,
                source = QueueSource.MIX(
                    id = item.mix.id,
                    name = item.mix.title,
                    sourceHash = item.mix.sourceHash
                ),
                fetch = { homeRepository.getMixTracks(item.mix) }
            )
        }
    }

    private fun playFetchedTracks(
        itemKey: String,
        source: QueueSource,
        fetch: suspend () -> Resource<List<Track>>,
    ) {
        if (_uiState.value.loadingItemKey == itemKey) return

        playJob?.cancel()
        playJob = viewModelScope.launch {
            updateUiState { copy(loadingItemKey = itemKey) }
            when (val result = fetch()) {
                is Resource.Success -> _uiEffect.trySend(
                    HomeUiEffect.PlayTracks(result.data.orEmpty(), source)
                )

                is Resource.Error -> _uiEffect.trySend(
                    HomeUiEffect.ShowSnackBar(result.message ?: "Couldn't play this")
                )

                is Resource.Loading -> Unit
            }
            updateUiState { copy(loadingItemKey = null) }
        }
    }

    private fun updateUiState(block: HomeUiState.() -> HomeUiState) {
        _uiState.update(block)
    }

    private companion object {
        const val STALE_AFTER_MS = 60_000L
    }
}
