package com.android.swingmusic.library.presentation.screen

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

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            LibraryUiEffect.NavigateToProfile -> navigator.gotoProfile()
            LibraryUiEffect.NavigateToPlaylists -> Unit
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
                        subtitle = "Coming soon",
                        enabled = false,
                        onClick = { onEvent(LibraryUiEvent.OnPlaylistsClicked) }
                    )
                    LibraryGroupDivider()
                    LibraryRow(
                        icon = painterResource(R.drawable.ic_album),
                        title = "Albums",
                        subtitle = null,
                        onClick = { onEvent(LibraryUiEvent.OnAlbumsClicked) }
                    )
                    LibraryGroupDivider()
                    LibraryRow(
                        icon = painterResource(R.drawable.ic_artist),
                        title = "Artists",
                        subtitle = null,
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
        }

        LargeTitleTopBar(
            title = LIBRARY_TITLE,
            state = titleState,
            avatarPath = uiState.avatarPath,
            onProfileClick = { onEvent(LibraryUiEvent.OnProfileClicked) }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun LibraryScreenContentPreview() {
    SwingMusicTheme {
        LibraryScreenContent(uiState = LibraryUiState(), onEvent = {})
    }
}
