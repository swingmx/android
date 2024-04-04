package com.android.swingmusic.folder.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.swingmusic.folder.presentation.event.FolderUiEvent
import com.android.swingmusic.folder.presentation.viewmodel.FoldersViewModel
import com.android.swingmusic.uicomponent.presentation.component.FolderItem
import com.android.swingmusic.uicomponent.presentation.component.PathIndicatorItem
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FoldersAndTracksScreen(
    foldersViewModel: FoldersViewModel
) {
    val currentFolder by foldersViewModel.currentFolder
    val foldersAndTracksState by foldersViewModel.foldersAndTracks
    val navPaths by foldersViewModel.navPaths

    SwingMusicTheme {
        Scaffold(
            topBar = {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Folders",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        ) { paddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {
                val lazyColumnState = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()

                DisposableEffect(key1 = foldersAndTracksState) {
                    onDispose {
                        coroutineScope.launch {
                            if (foldersAndTracksState.foldersAndTracks.folders.isNotEmpty() or
                                foldersAndTracksState.foldersAndTracks.tracks.isNotEmpty()
                            ) {
                                lazyColumnState.scrollToItem(0)
                            }
                        }
                    }
                }

                if (foldersAndTracksState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                if (foldersAndTracksState.isError) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = foldersAndTracksState.errorMsg,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(onClick = {
                                foldersViewModel.onFolderUiEvent(FolderUiEvent.Retry)
                            }) {
                                Text("RETRY")
                            }
                        }
                    }
                }
                if (!foldersAndTracksState.isError && !foldersAndTracksState.isLoading) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyColumnState
                    ) {
                        // Navigation Paths
                        stickyHeader {
                            LazyRow(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(navPaths) { folder ->
                                    PathIndicatorItem(
                                        folder = folder,
                                        isRootPath = folder.path == navPaths.first().path,
                                        isCurrentPath = folder.path == currentFolder.path,
                                        onClick = { navFolder ->
                                            foldersViewModel.onFolderUiEvent(
                                                FolderUiEvent.ClickNavPath(
                                                    navFolder
                                                )
                                            )
                                        }
                                    )
                                    if (navPaths.size != 1 &&
                                        navPaths.indexOf(folder) != navPaths.lastIndex
                                    ) {
                                        Icon(
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                            imageVector = Icons.Default.KeyboardArrowRight,
                                            contentDescription = "Arrow Right"
                                        )
                                    }
                                }
                            }
                        }

                        // Folders
                        items(foldersAndTracksState.foldersAndTracks.folders) { folder ->
                            FolderItem(
                                folder = folder,
                                onClickFolderItem = {
                                    foldersViewModel.onFolderUiEvent(FolderUiEvent.ClickFolder(it))
                                },
                                onClickMoreVert = {

                                }
                            )
                        }

                        // Tracks
                        items(foldersAndTracksState.foldersAndTracks.tracks) { track ->
                            TrackItem(track = track, onClickTrackItem = {}, onClickMoreVert = {})

                        }
                    }
                }
            }
        }

        // Override the system back handler
        BackHandler(enabled = true) {
            foldersViewModel.onFolderUiEvent(FolderUiEvent.OnBackNav(currentFolder))
        }
    }
}
