package com.android.swingmusic.album.presentation.screen

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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.android.swingmusic.album.presentation.event.AlbumsUiEvent
import com.android.swingmusic.album.presentation.navigator.AlbumNavigator
import com.android.swingmusic.album.presentation.state.AllAlbumsUiState
import com.android.swingmusic.album.presentation.util.pagingAlbums
import com.android.swingmusic.album.presentation.viewmodel.AllAlbumsViewModel
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.util.SortBy
import com.android.swingmusic.uicomponent.presentation.component.AlbumItem
import com.android.swingmusic.uicomponent.presentation.component.SortByChip
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.android.swingmusic.uicomponent.R as UiComponents

@Composable
private fun AllAlbums(
    pagingAlbums: LazyPagingItems<Album>,
    allAlbumsUiState: AllAlbumsUiState,
    sortByPairs: List<Pair<SortBy, String>>,
    onUpdateGridCount: (Int) -> Unit,
    onSortBy: (Pair<SortBy, String>) -> Unit,
    onClickAlbumItem: (albumHash: String) -> Unit,
    onRetry: () -> Unit,
    baseUrl: String
) {

    val gridState = rememberLazyGridState()
    val albumCount = when (val result = allAlbumsUiState.totalAlbums) {
        is Resource.Loading -> -1
        is Resource.Error -> 0
        is Resource.Success -> result.data!!
    }

    val loadingState = when {
        pagingAlbums.loadState.append is LoadState.Loading -> pagingAlbums.loadState.append as LoadState.Loading
        pagingAlbums.loadState.prepend is LoadState.Loading -> pagingAlbums.loadState.prepend as LoadState.Loading
        pagingAlbums.loadState.refresh is LoadState.Loading -> pagingAlbums.loadState.refresh as LoadState.Loading
        else -> null
    }

    val errorState = when {
        pagingAlbums.loadState.append is LoadState.Error -> pagingAlbums.loadState.append as LoadState.Error
        pagingAlbums.loadState.prepend is LoadState.Error -> pagingAlbums.loadState.prepend as LoadState.Error
        pagingAlbums.loadState.refresh is LoadState.Error -> pagingAlbums.loadState.refresh as LoadState.Error
        else -> null
    }

    var isGridCountMenuExpanded by remember { mutableStateOf(false) }

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
                            text = "Albums",
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
                                                    if (allAlbumsUiState.gridCount == count)
                                                        MaterialTheme.colorScheme.inverseSurface.copy(
                                                            alpha = .1F
                                                        )
                                                    else Color.Unspecified
                                                ),
                                            interactionSource = MutableInteractionSource(),
                                            onClick = {
                                                if (allAlbumsUiState.gridCount != count) {
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
                                                if (allAlbumsUiState.gridCount == count)
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
                        text = getAlbumCountHelperText(albumCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .80F)
                    )
                }
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    columns = GridCells.Fixed(allAlbumsUiState.gridCount),
                    state = gridState,
                ) {
                    item(span = { GridItemSpan(allAlbumsUiState.gridCount) }) {
                        LazyRow(
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(sortByPairs) { pair ->
                                SortByChip(
                                    labelPair = pair,
                                    sortOrder = allAlbumsUiState.sortOrder,
                                    isSelected = allAlbumsUiState.sortBy == pair
                                ) { clickedPair ->
                                    onSortBy(clickedPair)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                    }

                    if (pagingAlbums.loadState.refresh is LoadState.NotLoading) {
                        pagingAlbums(pagingAlbums) { album ->
                            if (album == null) return@pagingAlbums
                            AlbumItem(
                                modifier = Modifier.fillMaxSize(),
                                album = album,
                                baseUrl = baseUrl,
                                onClick = {
                                    onClickAlbumItem(it)
                                }
                            )
                        }
                    }

                    loadingState?.let {
                        item(span = { GridItemSpan(allAlbumsUiState.gridCount) }) {
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
                                    Text(text = "Loading albums...")

                                    Spacer(modifier = Modifier.height(8.dp))

                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }

                    errorState?.let {
                        item(span = { GridItemSpan(allAlbumsUiState.gridCount) }) {
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
                                        pagingAlbums.retry()
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

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun AllAlbumScreen(
    allAlbumsViewModel: AllAlbumsViewModel = hiltViewModel(),
    albumNavigator: AlbumNavigator
) {
    val pagingAlbums =
        allAlbumsViewModel.allAlbumsUiState.value.pagingAlbums.collectAsLazyPagingItems()
    val albumsUiState by remember { allAlbumsViewModel.allAlbumsUiState }
    val sortAlbumsByPairs by remember { derivedStateOf { allAlbumsViewModel.sortAlbumsByEntries.toList() } }

    val baseUrl by allAlbumsViewModel.baseUrl.collectAsState()

    var showAlbumWithInfoBottomSheet by rememberSaveable { mutableStateOf(false) }
    val albumWithInfoBottomSheetState = rememberModalBottomSheetState(true)

    SwingMusicTheme(navBarColor = MaterialTheme.colorScheme.inverseOnSurface) {
        AllAlbums(
            pagingAlbums = pagingAlbums,
            allAlbumsUiState = albumsUiState,
            sortByPairs = sortAlbumsByPairs,
            baseUrl = baseUrl ?: "https://default",
            onUpdateGridCount = { gridCount ->
                allAlbumsViewModel.onAlbumsUiEvent(AlbumsUiEvent.OnUpdateGridCount(gridCount))
            },
            onSortBy = { pair ->
                allAlbumsViewModel.onAlbumsUiEvent(AlbumsUiEvent.OnSortBy(pair))
            },
            onClickAlbumItem = {
                albumNavigator.gotoAlbumWithInfo(albumHash = it)
            },
            onRetry = {
                allAlbumsViewModel.onAlbumsUiEvent(AlbumsUiEvent.OnRetry)
            }
        )

        if (showAlbumWithInfoBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAlbumWithInfoBottomSheet = false },
                sheetState = albumWithInfoBottomSheetState,
                dragHandle = {}
            ) {

            }
        }
    }
}

private fun getAlbumCountHelperText(count: Int): String {
    return when (count) {
        -1 -> "Loading albums..."
        0 -> "No albums found!"
        1 -> "You have 1 album in your library"
        else -> {
            val formattedCount = count.toString().reversed().chunked(3).joinToString(",").reversed()
            "You have $formattedCount albums in your library"
        }
    }
}
