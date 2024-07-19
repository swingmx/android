package com.android.swingmusic.folder.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.folder.domain.FolderRepository
import com.android.swingmusic.folder.presentation.event.FolderUiEvent
import com.android.swingmusic.folder.presentation.state.FoldersAndTracksState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val folderRepository: FolderRepository
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

    private var _foldersAndTracks: MutableState<FoldersAndTracksState> =
        mutableStateOf(
            FoldersAndTracksState(
                foldersAndTracks = FoldersAndTracks(
                    folders = emptyList(),
                    tracks = emptyList()
                ),
                isLoading = false,
                isError = false
            )
        )

    val foldersAndTracks: State<FoldersAndTracksState> = _foldersAndTracks

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

    init {
        getFoldersAndTracks(homeDir.path)
    }

    fun onFolderUiEvent(event: FolderUiEvent) {
        when (event) {
            is FolderUiEvent.OnClickNavPath -> {
                if (event.folder.path != _currentFolder.value.path) {
                    resetUiToLoadingState()

                    _currentFolder.value = event.folder
                    getFoldersAndTracks(event.folder.path)
                }
            }

            is FolderUiEvent.OnClickFolder -> {
                resetUiToLoadingState()

                _currentFolder.value = event.folder
                getFoldersAndTracks(event.folder.path)

                if (!_navPaths.value.contains(event.folder)) {
                    _navPaths.value = listOf<Folder>(homeDir)
                        .plus(
                            (_navPaths.value.filter {
                                event.folder.path.contains(it.path)
                            }.plus(event.folder))
                                .toSet()
                                .toList()
                        )
                }
            }

            is FolderUiEvent.OnBackNav -> {
                if (_navPaths.value.size > 1) {
                    // Utilizing the fact that we can't have multiple folders with the same name
                    val currentPathIndex = _navPaths.value.indexOf(event.folder)
                    val backPathIndex = currentPathIndex - 1
                    if (backPathIndex > -1) { // Just to be safe
                        resetUiToLoadingState()

                        val backFolder = _navPaths.value[backPathIndex]
                        _currentFolder.value = backFolder

                        getFoldersAndTracks(backFolder.path)
                    }
                }
            }

            is FolderUiEvent.OnRetry -> {
                resetUiToLoadingState()
                getFoldersAndTracks(_currentFolder.value.path)
            }
        }
    }
}
