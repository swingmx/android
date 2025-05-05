package com.android.swingmusic.artist.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.swingmusic.artist.presentation.event.ArtistInfoUiEvent
import com.android.swingmusic.artist.presentation.event.ArtistUiEvent
import com.android.swingmusic.artist.presentation.state.ArtistsUiState
import com.android.swingmusic.artist.presentation.util.pagingArtists
import com.android.swingmusic.artist.presentation.viewmodel.ArtistInfoViewModel
import com.android.swingmusic.artist.presentation.viewmodel.ArtistsViewModel
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.util.SortBy
import com.android.swingmusic.uicomponent.presentation.component.ArtistItem
import com.android.swingmusic.uicomponent.presentation.component.SortByChip
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.android.swingmusic.uicomponent.R as UiComponents

@Composable
private fun AllArtists(
    pagingArtists: LazyPagingItems<Artist>,
    artistsUiState: ArtistsUiState,
    sortByPairs: List<Pair<SortBy, String>>,
    onUpdateGridCount: (Int) -> Unit,
    showOnRefreshIndicator: Boolean,
    onNavigateToInfo: (String) -> Unit,
    onSortBy: (Pair<SortBy, String>) -> Unit,
    onRetry: () -> Unit,
    baseUrl: String
) {
    val gridState = rememberLazyGridState()
    val artistCount = when (val result = artistsUiState.totalArtists) {
        is Resource.Loading -> -1
        is Resource.Error -> 0
        is Resource.Success -> result.data!!
    }

    val loadingState = when {
        pagingArtists.loadState.append is LoadState.Loading -> pagingArtists.loadState.append as LoadState.Loading
        pagingArtists.loadState.prepend is LoadState.Loading -> pagingArtists.loadState.prepend as LoadState.Loading
        pagingArtists.loadState.refresh is LoadState.Loading -> pagingArtists.loadState.refresh as LoadState.Loading
        else -> null
    }

    val errorState = when {
        pagingArtists.loadState.append is LoadState.Error -> pagingArtists.loadState.append as LoadState.Error
        pagingArtists.loadState.prepend is LoadState.Error -> pagingArtists.loadState.prepend as LoadState.Error
        pagingArtists.loadState.refresh is LoadState.Error -> pagingArtists.loadState.refresh as LoadState.Error
        else -> null
    }

    var isGridCountMenuExpanded by remember { mutableStateOf(false) }

    // TODO: Add pinch to reduce Grid count... as seen in Google Photos
    Scaffold {
        Scaffold(
            modifier = Modifier.padding(it),
            topBar = {
                Column(verticalArrangement = Arrangement.Center) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Artists",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        IconButton(
                            onClick = {
                                isGridCountMenuExpanded = isGridCountMenuExpanded.not()
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = UiComponents.drawable.grid),
                                contentDescription = "Sort Order Icon"
                            ).also {
                                DropdownMenu(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    expanded = isGridCountMenuExpanded,
                                    onDismissRequest = {
                                        isGridCountMenuExpanded = isGridCountMenuExpanded.not()
                                    }
                                ) {
                                    Text(
                                        text = "Grid count",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    (2..4).forEach { count ->
                                        DropdownMenuItem(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12))
                                                .background(
                                                    if (artistsUiState.gridCount == count)
                                                        MaterialTheme.colorScheme.inverseSurface.copy(
                                                            alpha = .1F
                                                        )
                                                    else Color.Unspecified
                                                ),
                                            interactionSource = MutableInteractionSource(),
                                            onClick = {
                                                if (artistsUiState.gridCount != count) {
                                                    isGridCountMenuExpanded = false
                                                    onUpdateGridCount(count)
                                                }
                                            }, text = {
                                                Text(
                                                    maxLines = 1,
                                                    text = count.toString(),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = MaterialTheme.colorScheme.inverseSurface.copy(
                                                        alpha = .84F
                                                    )
                                                )
                                            },
                                            trailingIcon = {
                                                if (artistsUiState.gridCount == count)
                                                    Icon(
                                                        modifier = Modifier.padding(start = 12.dp),
                                                        imageVector = Icons.Rounded.CheckCircle,
                                                        contentDescription = "Checked"
                                                    )
                                            }
                                        )
                                        if (count < 4) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            bottom = 8.dp
                        ),
                        text = getArtistCountHelperText(artistCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .80F)
                    )
                }
            }) { paddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {

                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    columns = GridCells.Fixed(artistsUiState.gridCount),
                    state = gridState,
                ) {
                    item(span = { GridItemSpan(artistsUiState.gridCount) }) {
                        LazyRow(
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(sortByPairs) { pair ->
                                SortByChip(
                                    labelPair = pair,
                                    sortOrder = artistsUiState.sortOrder,
                                    isSelected = artistsUiState.sortBy == pair
                                ) { clickedPair ->
                                    onSortBy(clickedPair)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                    }

                    if (pagingArtists.loadState.refresh is LoadState.NotLoading) {
                        pagingArtists(pagingArtists) { artist ->
                            if (artist == null) return@pagingArtists
                            ArtistItem(
                                modifier = Modifier.fillMaxSize(),
                                artist = artist,
                                baseUrl = baseUrl,
                                onClick = { artistHash ->
                                    onNavigateToInfo(artistHash)
                                }
                            )
                        }

                        item(span = { GridItemSpan(artistsUiState.gridCount) }) {
                            Spacer(modifier = Modifier.height(250.dp))
                        }
                    }

                    loadingState?.let {
                        if (!showOnRefreshIndicator) {
                            item(span = { GridItemSpan(artistsUiState.gridCount) }) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {

                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "Loading artists...")

                                        Spacer(modifier = Modifier.height(8.dp))

                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }

                    errorState?.let {
                        item(span = { GridItemSpan(artistsUiState.gridCount) }) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = it.error.message!!,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(onClick = {
                                        pagingArtists.retry()
                                        onRetry()
                                    }) {
                                        Text(text = "RETRY")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * This Composable is heavily coupled with ArtistsViewModel. [AllArtists] Compsable has no ties.
 **/
@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun AllArtistsScreen(
    navigator: CommonNavigator,
    artistsViewModel: ArtistsViewModel = hiltViewModel(),
    artistInfoViewModel: ArtistInfoViewModel
) {
    val pagingArtists =
        artistsViewModel.artistsUiState.value.pagingArtists.collectAsLazyPagingItems()
    val artistsUiState by remember { artistsViewModel.artistsUiState }
    val sortByPairs by remember { derivedStateOf { artistsViewModel.sortArtistsByEntries.toList() } }

    val baseUrl by remember { artistsViewModel.baseUrl() }

    var showOnRefreshIndicator by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()

    val errorState = when {
        pagingArtists.loadState.append is LoadState.Error -> pagingArtists.loadState.append as LoadState.Error
        pagingArtists.loadState.prepend is LoadState.Error -> pagingArtists.loadState.prepend as LoadState.Error
        pagingArtists.loadState.refresh is LoadState.Error -> pagingArtists.loadState.refresh as LoadState.Error
        else -> null
    }

    if (pagingArtists.loadState.refresh is LoadState.NotLoading) {
        showOnRefreshIndicator = false
    }

    errorState?.let {
        showOnRefreshIndicator = false
    }

    SwingMusicTheme(navBarColor = MaterialTheme.colorScheme.inverseOnSurface) {
        PullToRefreshBox(
            isRefreshing = showOnRefreshIndicator,
            state = refreshState,
            onRefresh = {
                showOnRefreshIndicator = true

                artistsViewModel.onArtistUiEvent(ArtistUiEvent.OnPullToRefresh)
            },
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier
                        .padding(top = 76.dp)
                        .align(Alignment.TopCenter),
                    isRefreshing = showOnRefreshIndicator,
                    state = refreshState
                )
            }
        ) {
            AllArtists(
                pagingArtists = pagingArtists,
                artistsUiState = artistsUiState,
                sortByPairs = sortByPairs,
                baseUrl = baseUrl ?: "",
                showOnRefreshIndicator = showOnRefreshIndicator,
                onUpdateGridCount = { count ->
                    artistsViewModel.onArtistUiEvent(ArtistUiEvent.OnUpdateGridCount(count))
                },
                onSortBy = { pair ->
                    artistsViewModel.onArtistUiEvent(ArtistUiEvent.OnSortBy(pair))
                },
                onRetry = {
                    artistsViewModel.onArtistUiEvent(ArtistUiEvent.OnRetry)
                },
                onNavigateToInfo = {
                    artistInfoViewModel.onArtistInfoUiEvent(
                        ArtistInfoUiEvent.OnLoadArtistInfo(it)
                    )
                    navigator.gotoArtistInfo(it)
                }
            )
        }
    }
}

private fun getArtistCountHelperText(count: Int): String {
    return when (count) {
        -1 -> "Loading artists..."
        0 -> "No artists found!"
        1 -> "You have 1 artist in your library"
        else -> {
            val formattedCount =
                count.toString().reversed().chunked(3).joinToString(" ,").reversed()
            "You have $formattedCount artists in your library"
        }
    }
}
