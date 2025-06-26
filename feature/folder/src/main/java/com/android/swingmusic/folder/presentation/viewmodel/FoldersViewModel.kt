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
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.folder.domain.FolderRepository
import com.android.swingmusic.folder.presentation.event.FolderUiEvent
import com.android.swingmusic.folder.presentation.state.FoldersAndTracksState
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

    // Unified content pagination state with immediate clearing
    private var _foldersContentPaging: MutableState<FoldersContentPagingState> =
        mutableStateOf(FoldersContentPagingState())
    val foldersContentPaging: State<FoldersContentPagingState> = _foldersContentPaging

    init {
        getFoldersContentPaging(homeDir.path)
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
                    getFoldersContentPaging(event.folder.path)
                }
            }

            is FolderUiEvent.OnClickFolder -> {
                _currentFolder.value = event.folder
                // Clear favorite updates when navigating to a new folder
                updatedTrackFavorites.update { emptyMap() }
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
                        getFoldersContentPaging(backFolder.path)
                    }
                }
            }

            is FolderUiEvent.OnRetry -> {
                // Clear favorite updates on retry
                updatedTrackFavorites.update { emptyMap() }
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
