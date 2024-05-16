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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.swingmusic.folder.presentation.event.FolderUiEvent
import com.android.swingmusic.folder.presentation.viewmodel.FoldersViewModel
import com.android.swingmusic.uicomponent.presentation.component.FolderItem
import com.android.swingmusic.uicomponent.presentation.component.PathIndicatorItem
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FoldersAndTracksScreen(
    foldersViewModel: FoldersViewModel
) {
    val currentFolder by remember { foldersViewModel.currentFolder }
    val foldersAndTracksState by remember { foldersViewModel.foldersAndTracks }
    val navPaths by remember { foldersViewModel.navPaths }

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
                val pathsLazyRowState = rememberLazyListState()

                LaunchedEffect(key1 = currentFolder) {
                    val index = navPaths.indexOf(currentFolder)
                    pathsLazyRowState.animateScrollToItem(if (index >= 0) index else 0)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyColumnState
                ) {
                    // Navigation Paths
                    stickyHeader {
                        LazyRow(
                            state = pathsLazyRowState,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Always display a root dir item
                            item {
                                PathIndicatorItem(
                                    folder = foldersViewModel.homeDir,
                                    isRootPath = true,
                                    isCurrentPath = foldersViewModel.homeDir.path == currentFolder.path,
                                    onClick = {
                                        foldersViewModel.onFolderUiEvent(
                                            FolderUiEvent.OnClickNavPath(it)
                                        )
                                    }
                                )
                            }

                            itemsIndexed(navPaths) { index, folder ->
                                // Ignore home dir because it is already displayed
                                if (folder.path != "\$home") {
                                    PathIndicatorItem(
                                        folder = folder,
                                        isCurrentPath = folder.path == currentFolder.path,
                                        onClick = { navFolder ->
                                            foldersViewModel.onFolderUiEvent(
                                                FolderUiEvent.OnClickNavPath(navFolder)
                                            )
                                        }
                                    )
                                }
                                if (navPaths.size != 1 &&
                                    index != navPaths.lastIndex
                                ) {
                                    val tint = if (navPaths[index + 1].path == currentFolder.path)
                                        MaterialTheme.colorScheme.onSurface else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .30F)
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        tint = tint,
                                        contentDescription = "Arrow Right"
                                    )
                                }
                            }
                        }
                    }

                    item {
                        if (foldersAndTracksState.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // TODO: Use lottie/gif 💻 📲 loader
                                CircularProgressIndicator()
                            }
                        }
                    }
                    item {
                        if (foldersAndTracksState.isError) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxHeight()
                                    .padding(bottom = 48.dp),
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
                                        text = foldersAndTracksState.errorMessage,
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(onClick = {
                                        val event = FolderUiEvent.OnClickFolder(currentFolder)
                                        foldersViewModel.onFolderUiEvent(FolderUiEvent.OnRetry(event))
                                    }) {
                                        Text("RETRY")
                                    }
                                }
                            }
                        }
                    }

                    if (!foldersAndTracksState.isError && !foldersAndTracksState.isLoading) {
                        // Folders
                        items(foldersAndTracksState.foldersAndTracks.folders) { folder ->
                            FolderItem(
                                folder = folder,
                                onClickFolderItem = {
                                    foldersViewModel.onFolderUiEvent(FolderUiEvent.OnClickFolder(it))
                                },
                                onClickMoreVert = {

                                }
                            )
                        }

                        // Tracks
                        items(
                            foldersAndTracksState.foldersAndTracks.tracks,
                            key = {
                                it.filepath
                            }
                        ) { track ->
                            TrackItem(
                                track = track,
                                onClickTrackItem = {

                                },
                                onClickMoreVert = {

                                }
                            )
                        }
                    }
                    if (
                        foldersAndTracksState.foldersAndTracks.folders.isEmpty() &&
                        foldersAndTracksState.foldersAndTracks.tracks.isEmpty() &&
                        !foldersAndTracksState.isError &&
                        !foldersAndTracksState.isLoading
                    ) {
                        // This could mean the root directory is not configured or current directory is empty
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillParentMaxSize()
                                    .padding(bottom = 48.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "This directory is empty!",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                if (currentFolder.path == "\$home") {
                                    Button(onClick = {
                                        // TODO:Add Configure Dir Event
                                    }) {
                                        Text("Configure Root Directory")
                                    }
                                } else {
                                    OutlinedButton(onClick = {
                                        val event = FolderUiEvent.OnClickFolder(currentFolder)
                                        foldersViewModel.onFolderUiEvent(FolderUiEvent.OnRetry(event))
                                    }) {
                                        Text("RETRY")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val overrideSystemBackNav by remember {
        derivedStateOf { currentFolder.path != "\$home" }
    }
    BackHandler(enabled = overrideSystemBackNav) {
        foldersViewModel.onFolderUiEvent(FolderUiEvent.OnBackNav(currentFolder))
    }
}
