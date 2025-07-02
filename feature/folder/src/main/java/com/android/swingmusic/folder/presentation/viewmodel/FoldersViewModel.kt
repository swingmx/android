package com.android.swingmusic.folder.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.folder.domain.FolderRepository
import com.android.swingmusic.folder.presentation.event.FolderUiEvent
import com.android.swingmusic.folder.presentation.model.FolderContentItem
import com.android.swingmusic.folder.presentation.state.FoldersAndTracksState
import com.android.swingmusic.folder.presentation.state.FoldersContentPagingState
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

    fun resetNavPathsForGotoFolder(targetPath: String) {
        _navPaths.value = buildNavigationPaths(targetPath)
    }

    private fun fetchRootDirectories() {
        viewModelScope.launch {
            folderRepository.getRootDirectories().collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        _rootDirectories.value = result.data?.rootDirs ?: emptyList()
                    }

                    is Resource.Error -> {
                        _rootDirectories.value = emptyList()
                    }

                    is Resource.Loading -> {}
                }
            }
        }
    }

    private fun buildNavigationPaths(targetPath: String): List<Folder> {
        val rootDirs = _rootDirectories.value
        if (rootDirs.isEmpty() || targetPath == "\$home") {
            return listOf(homeDir)
        }

        // Find matching root directory and remove it from target path
        // Also handle /home as equivalent to $home
        val normalizedPath = when {
            targetPath.startsWith("/home") -> targetPath.removePrefix("/home")
            else -> rootDirs.find { rootDir ->
                targetPath.startsWith(rootDir)
            }?.let { matchingRootDir ->
                targetPath.removePrefix(matchingRootDir)
            } ?: targetPath
        }

        if (normalizedPath.isEmpty() || normalizedPath == "\$home") {
            return listOf(homeDir)
        }

        val pathSegments = normalizedPath.split("/").filter { it.isNotEmpty() }
        val paths = mutableListOf<Folder>()

        paths.add(homeDir)
        
        // Find the original root directory that matches this target path
        val matchingRootDir = when {
            targetPath.startsWith("/home") -> "/home"
            else -> rootDirs.find { rootDir ->
                targetPath.startsWith(rootDir)
            }
        }
        
        var pathRootDir = matchingRootDir ?: ""
        for (segment in pathSegments) {
            pathRootDir = if (pathRootDir.isEmpty()) segment else "$pathRootDir/$segment"
            paths.add(
                Folder(
                    name = segment,
                    path = pathRootDir,
                    trackCount = 0,
                    folderCount = 0,
                    isSym = false
                )
            )
        }

        return paths
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

    private val updatedTrackFavorites = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    private var _rootDirectories: MutableState<List<String>> = mutableStateOf(emptyList())
    val rootDirectories: State<List<String>> = _rootDirectories

    private var _foldersContentPaging: MutableState<FoldersContentPagingState> =
        mutableStateOf(FoldersContentPagingState())
    val foldersContentPaging: State<FoldersContentPagingState> = _foldersContentPaging

    init {
        fetchRootDirectories()
        getFoldersContentPaging(homeDir.path)
    }

    fun refreshRootDirectories() {
        fetchRootDirectories()
    }

    private fun getFoldersContentPaging(path: String) {
        viewModelScope.launch {
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
                    updatedTrackFavorites.update { emptyMap() }
                    getFoldersContentPaging(event.folder.path)
                }
            }

            is FolderUiEvent.OnClickFolder -> {
                _currentFolder.value = event.folder

                updatedTrackFavorites.update { emptyMap() }
                getFoldersContentPaging(event.folder.path)

                val normalizedEventPath = event.folder.path.trimEnd('/')
                val existingFolder = _navPaths.value.find { navFolder ->
                    navFolder.path.trimEnd('/') == normalizedEventPath
                }

                if (existingFolder != null) {
                    _currentFolder.value = existingFolder
                } else {
                    _navPaths.value = buildNavigationPaths(event.folder.path)
                    _currentFolder.value = _navPaths.value.lastOrNull() ?: event.folder
                }
            }

            is FolderUiEvent.OnBackNav -> {
                if (_navPaths.value.size > 1) {
                    val currentPathIndex = _navPaths.value.indexOf(event.folder)
                    val backPathIndex = currentPathIndex - 1
                    if (backPathIndex > -1) {
                        val backFolder = _navPaths.value[backPathIndex]
                        _currentFolder.value = backFolder
                        updatedTrackFavorites.update { emptyMap() }
                        getFoldersContentPaging(backFolder.path)
                    }
                }
            }

            is FolderUiEvent.OnRetry -> {
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
            val originalFavoriteStatus = updatedTrackFavorites.value[trackHash]

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
                        val serverFavoriteStatus = result.data ?: false
                        updatedTrackFavorites.update { it + (trackHash to serverFavoriteStatus) }
                    }

                    is Resource.Error -> {
                        if (originalFavoriteStatus != null) {
                            updatedTrackFavorites.update { it + (trackHash to originalFavoriteStatus) }
                        } else {
                            updatedTrackFavorites.update { it - trackHash }
                        }
                    }
                }
            }
        }
    }
}
