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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.swingmusic.core.util.PlayerState
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
                val coroutineScope = rememberCoroutineScope()

                DisposableEffect(key1 = currentFolder, key2 = foldersAndTracksState) {
                    onDispose {
                        coroutineScope.launch {
                            //  lazyColumnState.animateScrollToItem(0)
                            val index = navPaths.indexOf(currentFolder)
                            pathsLazyRowState.animateScrollToItem(if (index > 0) index else 0)
                        }
                    }
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
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Always display a root dir item
                            item {
                                PathIndicatorItem(
                                    folder = foldersViewModel.rootFolder,
                                    isRootPath = true,
                                    isCurrentPath = foldersViewModel.rootFolder.path == currentFolder.path,
                                    onClick = {
                                        foldersViewModel.onFolderUiEvent(
                                            FolderUiEvent.ClickRootDir
                                        )
                                    }
                                )
                            }

                            items(navPaths) { folder ->
                                // Ignore root dir because it is already displayed
                                if (folder.path != "/") {
                                    PathIndicatorItem(
                                        folder = folder,
                                        isCurrentPath = folder.path == currentFolder.path,
                                        onClick = { navFolder ->
                                            foldersViewModel.onFolderUiEvent(
                                                FolderUiEvent.ClickNavPath(navFolder)
                                            )
                                        }
                                    )
                                }
                                if (navPaths.size != 1 &&
                                    navPaths.indexOf(folder) != navPaths.lastIndex
                                ) {
                                    /*Text(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        text = "/",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5F)
                                    )*/
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .5F),
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
                                        text = foldersAndTracksState.errorMsg,
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(onClick = {
                                        val event =
                                            if (currentFolder.path != "/")
                                                FolderUiEvent.ClickFolder(currentFolder)
                                            else FolderUiEvent.ClickRootDir

                                        foldersViewModel.onFolderUiEvent(FolderUiEvent.Retry(event))
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
                                    foldersViewModel.onFolderUiEvent(FolderUiEvent.ClickFolder(it))
                                },
                                onClickMoreVert = {

                                }
                            )

                            /*if (foldersAndTracksState.foldersAndTracks.folders.indexOf(folder) !=
                                foldersAndTracksState.foldersAndTracks.folders.lastIndex
                            ) {
                                Divider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .1F)
                                )
                            }*/
                        }

                        /*item {
                            if (foldersAndTracksState.foldersAndTracks.folders.isNotEmpty() &&
                                foldersAndTracksState.foldersAndTracks.tracks.isNotEmpty()
                            ) {
                                Divider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .1F)
                                )
                            }
                        }*/

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
                        // This could mean the root directories are not configured or are empty
                        // TODO: Show configure button
                        item { Text(text = "This directory is empty!") }
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
