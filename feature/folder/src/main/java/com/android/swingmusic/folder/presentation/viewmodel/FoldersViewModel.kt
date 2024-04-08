package com.android.swingmusic.folder.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.util.Resource
import com.android.swingmusic.folder.presentation.event.FolderUiEvent
import com.android.swingmusic.folder.presentation.state.FoldersAndTracksState
import com.android.swingmusic.network.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val networkRepository: NetworkRepository
) : ViewModel() {
    val rootFolder: Folder = Folder(
        path = "/",
        name = "", // Root
        trackCount = 0,
        folderCount = 0,
        isSym = false
    )

    private var _currentFolder: MutableState<Folder> = mutableStateOf(rootFolder)
    val currentFolder: State<Folder> = _currentFolder

    private var _navPaths: MutableState<List<Folder>> =
        mutableStateOf(listOf(rootFolder))
    val navPaths: State<List<Folder>> = _navPaths

    private var _foldersAndTracks: MutableState<FoldersAndTracksState> =
        mutableStateOf(
            FoldersAndTracksState(
                foldersAndTracks = FoldersAndTracks(
                    folders = emptyList(),
                    tracks = emptyList()
                ), isLoading = true, isError = false
            )
        )

    val foldersAndTracks: State<FoldersAndTracksState> = _foldersAndTracks

    private fun resetUiStates() {
        _foldersAndTracks.value = FoldersAndTracksState(
            foldersAndTracks = FoldersAndTracks(
                folders = emptyList(),
                tracks = emptyList()
            ),
            isLoading = true,
            isError = false
        )
    }

    private fun getRootDirectories() {


        viewModelScope.launch {
            when (val result = networkRepository.getRootDirectories()) {
                is Resource.Success -> {
                    val rootDirs: MutableList<Folder> = ArrayList()

                    Timber.e("Root Dirs: ${result.data!!.rootDirs}")

                    for (path in result.data!!.rootDirs) {
                        rootDirs.add(
                            Folder(
                                path = path.replaceFirst("$", "/"),
                                name = path.substringAfterLast("/").replace("$", ""),
                                trackCount = 0,
                                folderCount = 0,
                                isSym = true
                            )
                        )
                    }

                    Timber.e("Root Folders: $rootDirs")

                    _foldersAndTracks.value =
                        _foldersAndTracks.value.copy(
                            foldersAndTracks = FoldersAndTracks(
                                folders = rootDirs,
                                tracks = emptyList()
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
                            errorMsg = "Unable to fetch root directories"
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

    private fun getFoldersAndTracks(path: String) {
        viewModelScope.launch {
            val request = FoldersAndTracksRequest(path, false)

            when (val result = networkRepository.getFoldersAndTracks(request)) {
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
                            errorMsg = result.message ?: "Unable to fetch folders"
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

    init {
        getRootDirectories()
    }

    fun onFolderUiEvent(event: FolderUiEvent) {
        when (event) {
            is FolderUiEvent.ClickRootDir -> {
                if (_currentFolder.value.path != rootFolder.path) {
                    _currentFolder.value = rootFolder
                    resetUiStates()
                    getRootDirectories()
                }
            }

            is FolderUiEvent.ClickNavPath -> {
                Timber.e("NAV PATH: ${event.folder.path} -> ${event.folder.name}")

                if (event.folder.path != _currentFolder.value.path) {
                    resetUiStates()

                    _currentFolder.value = event.folder
                    getFoldersAndTracks(event.folder.path)
                }
            }

            is FolderUiEvent.ClickFolder -> {
                resetUiStates()

                _currentFolder.value = event.folder
                getFoldersAndTracks(event.folder.path)

                _navPaths.value =
                    _navPaths.value.filter {
                        event.folder.path.contains(it.path)
                    }.plus(event.folder).toSet().toList()
            }

            is FolderUiEvent.OnBackNav -> {

                if (_navPaths.value.size > 1) {
                    val currentPathIndex = _navPaths.value.indexOf(event.folder)
                    val backPathIndex = currentPathIndex - 1
                    if (backPathIndex > -1) { // Just to be safe
                        resetUiStates()

                        val backFolder = _navPaths.value[backPathIndex]
                        _currentFolder.value = backFolder

                        if (backFolder.path == rootFolder.path) {
                            getRootDirectories()
                        } else {
                            getFoldersAndTracks(backFolder.path)
                        }
                    }
                }
            }

            is FolderUiEvent.Retry -> {
                resetUiStates()
                if (event.event is FolderUiEvent.ClickRootDir) {
                    getRootDirectories()
                } else {
                    getFoldersAndTracks(_currentFolder.value.path)
                }
            }
        }
    }
}
