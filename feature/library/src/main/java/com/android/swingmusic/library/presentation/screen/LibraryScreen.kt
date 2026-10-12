package com.android.swingmusic.library.presentation.screen

import androidx.compose.foundation.layout.fillMaxWidth
import java.text.NumberFormat
import com.android.swingmusic.uicomponent.presentation.component.AlbumItem
import com.android.swingmusic.library.domain.LibraryCounts
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.library.presentation.component.LibraryGroup
import com.android.swingmusic.library.presentation.component.LibraryGroupDivider
import com.android.swingmusic.library.presentation.component.LibraryRow
import com.android.swingmusic.library.presentation.event.LibraryUiEffect
import com.android.swingmusic.library.presentation.event.LibraryUiEvent
import com.android.swingmusic.library.presentation.state.LibraryUiState
import com.android.swingmusic.library.presentation.viewmodel.LibraryViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.LargeTitle
import com.android.swingmusic.uicomponent.presentation.component.LargeTitleTopBar
import com.android.swingmusic.uicomponent.presentation.component.largeTitleTopBarHeight
import com.android.swingmusic.uicomponent.presentation.component.rememberLargeTitleState
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination

private const val LIBRARY_TITLE = "Library"

@Destination
@Composable
fun LibraryScreen(
    navigator: CommonNavigator,
) {
    Library(navigator = navigator)
}

@Composable
private fun Library(
    navigator: CommonNavigator,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(LibraryUiEvent.OnScreenResumed)
    }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            LibraryUiEffect.NavigateToProfile -> navigator.gotoProfile()
            LibraryUiEffect.NavigateToPlaylists -> navigator.gotoPlaylists()
            is LibraryUiEffect.NavigateToAlbum -> navigator.gotoAlbumWithInfo(effect.albumHash)
            LibraryUiEffect.NavigateToAlbums -> navigator.gotoAllAlbums()
            LibraryUiEffect.NavigateToArtists -> navigator.gotoAllArtists()
            LibraryUiEffect.NavigateToFolders -> navigator.gotoFolders()
        }
    }

    LibraryScreenContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun LibraryScreenContent(
    uiState: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val titleState = rememberLargeTitleState(listState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = largeTitleTopBarHeight(), bottom = 200.dp)
        ) {
            item(key = "title") {
                LargeTitle(text = LIBRARY_TITLE, state = titleState)
            }

            item(key = "entries") {
                LibraryGroup(modifier = Modifier.padding(horizontal = 16.dp)) {
                    LibraryRow(
                        icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.QueueMusic),
                        title = "Playlists",
                        subtitle = uiState.counts.playlists?.let { countLabel(it, "playlist") },
                        onClick = { onEvent(LibraryUiEvent.OnPlaylistsClicked) }
                    )
                    LibraryGroupDivider()
                    LibraryRow(
                        icon = painterResource(R.drawable.ic_album),
                        title = "Albums",
                        subtitle = uiState.counts.albums?.let { countLabel(it, "album") },
                        onClick = { onEvent(LibraryUiEvent.OnAlbumsClicked) }
                    )
                    LibraryGroupDivider()
                    LibraryRow(
                        icon = painterResource(R.drawable.ic_artist),
                        title = "Artists",
                        subtitle = uiState.counts.artists?.let { countLabel(it, "artist") },
                        onClick = { onEvent(LibraryUiEvent.OnArtistsClicked) }
                    )
                    LibraryGroupDivider()
                    LibraryRow(
                        icon = painterResource(R.drawable.folder_outlined),
                        title = "Folders",
                        subtitle = "Browse by folder",
                        onClick = { onEvent(LibraryUiEvent.OnFoldersClicked) }
                    )
                }
            }

            if (uiState.recentlyAdded.isNotEmpty()) {
                item(key = "recentlyAddedTitle") {
                    Text(
                        text = "Recently added",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 12.dp)
                    )
                }
                item(key = "recentlyAdded") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.recentlyAdded, key = { it.albumHash }) { album ->
                            // The card only sizes its image, so the row item gets a fixed width
                            Box(modifier = Modifier.width(166.dp)) {
                                AlbumItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    album = album,
                                    showDate = false,
                                    baseUrl = uiState.baseUrl,
                                    onClick = { onEvent(LibraryUiEvent.OnAlbumClicked(it)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        LargeTitleTopBar(
            title = LIBRARY_TITLE,
            state = titleState,
            avatarPath = uiState.avatarPath,
            onProfileClick = { onEvent(LibraryUiEvent.OnProfileClicked) }
        )
    }
}

private fun countLabel(count: Int, noun: String): String {
    val formatted = NumberFormat.getIntegerInstance().format(count)
    return if (count == 1) "$formatted $noun" else "$formatted ${noun}s"
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun LibraryScreenContentPreview() {
    SwingMusicTheme {
        LibraryScreenContent(
            uiState = LibraryUiState(counts = LibraryCounts(playlists = 24, albums = 7803, artists = 5972)),
            onEvent = {}
        )
    }
}
