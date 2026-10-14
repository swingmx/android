package com.android.swingmusic.library.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.library.presentation.component.LibraryMessage
import com.android.swingmusic.library.presentation.component.LibraryTopBar
import com.android.swingmusic.library.presentation.event.FavouriteCardsUiEffect
import com.android.swingmusic.library.presentation.event.FavouriteCardsUiEvent
import com.android.swingmusic.library.presentation.state.FavouriteCardsKind
import com.android.swingmusic.library.presentation.state.FavouriteCardsUiState
import com.android.swingmusic.library.presentation.viewmodel.FavouriteCardsViewModel
import com.android.swingmusic.uicomponent.presentation.component.AlbumItem
import com.android.swingmusic.uicomponent.presentation.component.ArtistItem
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination

/** [kind] is a [FavouriteCardsKind] name: ALBUMS or ARTISTS. */
@Destination
@Composable
fun FavouriteCardsScreen(
    kind: String,
    navigator: CommonNavigator,
) {
    FavouriteCards(navigator = navigator)
}

@Composable
private fun FavouriteCards(
    navigator: CommonNavigator,
    viewModel: FavouriteCardsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(FavouriteCardsUiEvent.OnScreenResumed)
    }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            FavouriteCardsUiEffect.NavigateBack -> navigator.navigateBack()
            is FavouriteCardsUiEffect.NavigateToAlbum -> navigator.gotoAlbumWithInfo(effect.albumHash)
            is FavouriteCardsUiEffect.NavigateToArtist -> navigator.gotoArtistInfo(effect.artistHash)
        }
    }

    FavouriteCardsScreenContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun FavouriteCardsScreenContent(
    uiState: FavouriteCardsUiState,
    onEvent: (FavouriteCardsUiEvent) -> Unit,
) {
    val gridState = rememberLazyGridState()
    val nearEnd by remember {
        derivedStateOf {
            val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= gridState.layoutInfo.totalItemsCount - 6
        }
    }
    LaunchedEffect(nearEnd, uiState.itemCount) {
        if (nearEnd && !uiState.endReached) onEvent(FavouriteCardsUiEvent.OnLoadMoreItems)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { LibraryTopBar(title = uiState.kind.title, onBack = { onEvent(FavouriteCardsUiEvent.OnBackClicked) }) }
    ) { paddingValues ->
        when {
            uiState.isLoadingItems -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.errorLoadingItems != null -> LibraryMessage(
                title = "Couldn't load ${uiState.kind.title.lowercase()}",
                message = uiState.errorLoadingItems,
                actionLabel = "Retry",
                onAction = { onEvent(FavouriteCardsUiEvent.OnRetry) },
                modifier = Modifier.padding(paddingValues)
            )

            uiState.itemCount == 0 -> LibraryMessage(
                title = "No ${uiState.kind.title.lowercase()}",
                message = "Tap the heart on an ${uiState.kind.noun} page and it shows up here.",
                actionLabel = null,
                onAction = {},
                modifier = Modifier.padding(paddingValues)
            )

            else -> LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 200.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                uiState.total?.let { total ->
                    item(key = "count", span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = if (total == 1) "1 ${uiState.kind.noun}" else "$total ${uiState.kind.noun}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                if (uiState.kind == FavouriteCardsKind.ALBUMS) {
                    items(uiState.albums, key = { it.albumHash }) { album ->
                        AlbumItem(
                            modifier = Modifier.fillMaxWidth(),
                            album = album,
                            showDate = false,
                            baseUrl = uiState.baseUrl,
                            onClick = { onEvent(FavouriteCardsUiEvent.OnItemClicked(it)) }
                        )
                    }
                } else {
                    items(uiState.artists, key = { it.artistHash }) { artist ->
                        ArtistItem(
                            modifier = Modifier.fillMaxWidth(),
                            artist = artist,
                            baseUrl = uiState.baseUrl,
                            onClick = { onEvent(FavouriteCardsUiEvent.OnItemClicked(it)) }
                        )
                    }
                }
                if (uiState.isLoadingMoreItems) {
                    item(key = "loadingMore", span = { GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}
