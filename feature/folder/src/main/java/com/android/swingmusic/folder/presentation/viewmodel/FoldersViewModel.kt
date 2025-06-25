package com.android.swingmusic.folder.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.folder.domain.FolderRepository
import com.android.swingmusic.folder.presentation.event.FolderUiEvent
import com.android.swingmusic.folder.presentation.state.FoldersAndTracksState
import com.android.swingmusic.folder.presentation.state.FoldersWithPagingTracksState
import com.android.swingmusic.folder.presentation.state.FoldersContentPagingState
import com.android.swingmusic.folder.presentation.model.FolderContentItem
import com.android.swingmusic.player.domain.repository.PLayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val pLayerRepository: PLayerRepository
) : ViewModel() {
    val homeDir: Folder = Folder(
        path = "\$home",
        name = "",
        trackCount = 0,
        folderCount = 0,
        isSym = false
    )

    private var _currentFolder: MutableState<Folder> = mutableStateOf(homeDir)
    val currentFolder: State<Folder> = _currentFolder

    private var _navPaths: MutableState<List<Folder>> =
        mutableStateOf(listOf(homeDir))
    val navPaths: State<List<Folder>> = _navPaths

    fun resetNavPaths() {
        _navPaths.value = listOf(homeDir)
    }

    private var _foldersAndTracks: MutableState<FoldersAndTracksState> =
        mutableStateOf(
            FoldersAndTracksState(
                foldersAndTracks = FoldersAndTracks(
                    folders = emptyList(),
                    tracks = emptyList()
                ),
                isLoading = true,
                isError = false
            )
        )

    val foldersAndTracks: State<FoldersAndTracksState> = _foldersAndTracks

    // Track favorite status updates for optimistic UI updates
    private val updatedTrackFavorites = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    // Paging flow for tracks
    private val tracksFlow = MutableStateFlow<PagingData<Track>>(PagingData.empty())

    // New pagination state
    private var _foldersWithPagingTracks: MutableState<FoldersWithPagingTracksState> =
        mutableStateOf(FoldersWithPagingTracksState())
    val foldersWithPagingTracks: State<FoldersWithPagingTracksState> = _foldersWithPagingTracks
    
    // Unified content pagination state with immediate clearing
    private var _foldersContentPaging: MutableState<FoldersContentPagingState> =
        mutableStateOf(FoldersContentPagingState())
    val foldersContentPaging: State<FoldersContentPagingState> = _foldersContentPaging

    init {
        // Load initial folder data
        getFoldersWithPagingTracks(homeDir.path)
        getFoldersContentPaging(homeDir.path)
    }

    private fun resetUiToLoadingState() {
        _foldersAndTracks.value = FoldersAndTracksState(
            foldersAndTracks = FoldersAndTracks(
                folders = emptyList(),
                tracks = emptyList()
            ),
            isLoading = false,
            isError = false
        )
    }

    private fun getFoldersAndTracks(path: String) {
        viewModelScope.launch {
            val request = FoldersAndTracksRequest(path, false)
            val folderResult = folderRepository.getFoldersAndTracks(request)

            folderResult.collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        _foldersAndTracks.value = FoldersAndTracksState(
                            foldersAndTracks = result.data ?: FoldersAndTracks(
                                emptyList(),
                                emptyList()
                            ),
                            isLoading = false,
                            isError = false
                        )
                    }

                    is Resource.Error -> {
                        _foldersAndTracks.value =
                            _foldersAndTracks.value.copy(
                                foldersAndTracks = FoldersAndTracks(
                                    emptyList(),
                                    emptyList()
                                ),
                                isLoading = false,
                                isError = true,
                                errorMessage = result.message!!
                            )
                    }

                    is Resource.Loading -> {
                        _foldersAndTracks.value =
                            _foldersAndTracks.value.copy(
                                foldersAndTracks = FoldersAndTracks(
                                    emptyList(),
                                    emptyList()
                                ),
                                isLoading = true,
                                isError = false
                            )
                    }
                }
            }
        }
    }

    private fun getFoldersWithPagingTracks(path: String) {
        viewModelScope.launch {
            // Get folders (only first page needed since folders aren't paginated)
            val foldersFlow = folderRepository.getFolders(path)
            foldersFlow.collectLatest { foldersResource ->
                _foldersWithPagingTracks.value = _foldersWithPagingTracks.value.copy(
                    folders = foldersResource
                )
            }
        }

        viewModelScope.launch {
            // Get paginated tracks and combine with favorite updates
            folderRepository.getPagingTracks(path)
                .cachedIn(viewModelScope)
                .combine(updatedTrackFavorites) { pagingData, updatedFavorites ->
                    pagingData.map { track ->
                        val updatedFavorite = updatedFavorites[track.trackHash]
                        if (updatedFavorite != null) {
                            track.copy(isFavorite = updatedFavorite)
                        } else {
                            track
                        }
                    }
                }
                .collectLatest { pagingData ->
                    tracksFlow.value = pagingData
                    _foldersWithPagingTracks.value = _foldersWithPagingTracks.value.copy(
                        pagingTracks = tracksFlow
                    )
                }
        }
    }
    
    private fun getFoldersContentPaging(path: String) {
        viewModelScope.launch {
            // Create fresh pagination flow following Albums pattern
            val newPagingFlow = folderRepository.getPagingContent(path)
                .cachedIn(viewModelScope)
                .combine(updatedTrackFavorites) { pagingData, updatedFavorites ->
                    pagingData.map { contentItem ->
                        when (contentItem) {
                            is FolderContentItem.TrackItem -> {
                                val track = contentItem.track
                                val updatedFavorite = updatedFavorites[track.trackHash]
                                if (updatedFavorite != null) {
                                    FolderContentItem.TrackItem(track.copy(isFavorite = updatedFavorite))
                                } else {
                                    contentItem
                                }
                            }
                            is FolderContentItem.FolderItem -> contentItem
                        }
                    }
                }
            
            // Update state following Albums pattern
            _foldersContentPaging.value = FoldersContentPagingState(
                pagingContent = newPagingFlow
            )
        }
    }

    fun onFolderUiEvent(event: FolderUiEvent) {
        when (event) {
            is FolderUiEvent.OnClickNavPath -> {
                if (event.folder.path != _currentFolder.value.path) {
                    _currentFolder.value = event.folder
                    // Clear favorite updates when navigating to a new folder
                    updatedTrackFavorites.update { emptyMap() }
                    getFoldersWithPagingTracks(event.folder.path)
                    getFoldersContentPaging(event.folder.path)
                }
            }

            is FolderUiEvent.OnClickFolder -> {
                _currentFolder.value = event.folder
                // Clear favorite updates when navigating to a new folder
                updatedTrackFavorites.update { emptyMap() }
                getFoldersWithPagingTracks(event.folder.path)
                getFoldersContentPaging(event.folder.path)

                if (!_navPaths.value.contains(event.folder)) {
                    _navPaths.value = listOf<Folder>(homeDir)
                        .plus(
                            (_navPaths.value.filter {
                                event.folder.path.contains(it.path)
                            }.plus(event.folder))
                        ).distinctBy { it.path }
                }
            }

            is FolderUiEvent.OnBackNav -> {
                if (_navPaths.value.size > 1) {
                    // Utilizing the fact that we can't have multiple folders with the same name
                    val currentPathIndex = _navPaths.value.indexOf(event.folder)
                    val backPathIndex = currentPathIndex - 1
                    if (backPathIndex > -1) { // Just to be safe
                        val backFolder = _navPaths.value[backPathIndex]
                        _currentFolder.value = backFolder
                        // Clear favorite updates when navigating to a new folder
                        updatedTrackFavorites.update { emptyMap() }
                        getFoldersWithPagingTracks(backFolder.path)
                        getFoldersContentPaging(backFolder.path)
                    }
                }
            }

            is FolderUiEvent.OnRetry -> {
                // Clear favorite updates on retry
                updatedTrackFavorites.update { emptyMap() }
                getFoldersWithPagingTracks(_currentFolder.value.path)
                getFoldersContentPaging(_currentFolder.value.path)
            }

            is FolderUiEvent.ToggleTrackFavorite -> {
                toggleTrackFavorite(event.trackHash, event.isFavorite)
            }
        }
    }

    private fun toggleTrackFavorite(trackHash: String, isFavorite: Boolean) {
        viewModelScope.launch {
            // Store original favorite status for potential revert
            val originalFavoriteStatus = updatedTrackFavorites.value[trackHash]

            // Optimistically update the favorite status
            val newFavoriteStatus = !isFavorite
            updatedTrackFavorites.update { it + (trackHash to newFavoriteStatus) }

            val request = if (isFavorite) {
                pLayerRepository.removeTrackFromFavorite(trackHash)
            } else {
                pLayerRepository.addTrackToFavorite(trackHash)
            }

            request.collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {}

                    is Resource.Success -> {
                        // Update with actual result from server
                        val serverFavoriteStatus = result.data ?: false
                        updatedTrackFavorites.update { it + (trackHash to serverFavoriteStatus) }
                    }

                    is Resource.Error -> {
                        // Revert the optimistic update on error
                        if (originalFavoriteStatus != null) {
                            // Restore to the previous local state
                            updatedTrackFavorites.update { it + (trackHash to originalFavoriteStatus) }
                        } else {
                            // Remove from updates to fall back to original track data
                            updatedTrackFavorites.update { it - trackHash }
                        }
                    }
                }
            }
        }
    }
}
