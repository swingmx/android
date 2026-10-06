package com.android.swingmusic.profile.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.rememberCoroutineScope
import com.android.swingmusic.profile.presentation.component.ProfileSnackbarHost
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.profile.presentation.component.SkeletonBlock
import com.android.swingmusic.profile.presentation.component.rememberSkeletonAlpha
import com.android.swingmusic.uicomponent.presentation.component.PlayingTrackIndicator
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.Chart
import com.android.swingmusic.core.domain.model.ChartEntry
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.Trend
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.profile.presentation.component.ProfileTopBar
import com.android.swingmusic.profile.presentation.event.StatsUiEffect
import com.android.swingmusic.profile.presentation.event.StatsUiEvent
import com.android.swingmusic.profile.presentation.state.ChartState
import com.android.swingmusic.profile.presentation.state.StatsOrder
import com.android.swingmusic.profile.presentation.state.StatsPeriod
import com.android.swingmusic.profile.presentation.state.StatsUiState
import com.android.swingmusic.profile.presentation.state.allFailed
import com.android.swingmusic.profile.presentation.state.chart
import com.android.swingmusic.profile.presentation.state.isEmpty
import com.android.swingmusic.profile.presentation.viewmodel.StatsViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
internal fun StatsScreen(
    navigator: CommonNavigator,
    mediaControllerViewModel: MediaControllerViewModel,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            StatsUiEffect.NavigateBack -> navigator.navigateBack()
            is StatsUiEffect.ShowSnackBar -> scope.launch {
                snackbarHostState.showSnackbar(effect.message)
            }
            is StatsUiEffect.PlayTracks -> mediaControllerViewModel.onQueueEvent(
                QueueEvent.RecreateQueue(
                    source = QueueSource.UNKNOWN,
                    queue = effect.tracks,
                    clickedTrackIndex = effect.startIndex
                )
            )

            is StatsUiEffect.NavigateToArtist -> navigator.gotoArtistInfo(effect.artistHash)
            is StatsUiEffect.NavigateToAlbum -> navigator.gotoAlbumWithInfo(effect.albumHash)
        }
    }

    StatsScreenContent(
        uiState = uiState,
        playingTrackHash = playerUiState.nowPlayingTrack?.trackHash,
        playbackState = playerUiState.playbackState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun StatsScreenContent(
    uiState: StatsUiState,
    onEvent: (StatsUiEvent) -> Unit,
    playingTrackHash: String? = null,
    playbackState: PlaybackState = PlaybackState.PAUSED,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { ProfileSnackbarHost(snackbarHostState) },
        topBar = { ProfileTopBar(title = "Listening Stats", onBack = { onEvent(StatsUiEvent.OnBackClicked) }) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { onEvent(StatsUiEvent.OnRefresh) },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 168.dp)
            ) {
                item {
                    PeriodSelector(
                        selected = uiState.period,
                        onSelect = { onEvent(StatsUiEvent.OnPeriodSelected(it)) }
                    )
                }

                item {
                    SummaryRow(
                        summary = uiState.tracks.chart?.summary.orEmpty(),
                        order = uiState.order,
                        onOrderSelected = { onEvent(StatsUiEvent.OnOrderSelected(it)) }
                    )
                }

                when {
                    uiState.allFailed -> item {
                        StatsMessage(
                            title = "Couldn't load your stats",
                            body = (uiState.tracks as ChartState.Error).message,
                            onRetry = { onEvent(StatsUiEvent.OnRetry) }
                        )
                    }

                    uiState.isEmpty -> item {
                        StatsMessage(
                            title = "Nothing played ${uiState.period.emptyPhrase}",
                            body = "Play some music and your top tracks, artists and albums show up here."
                        )
                    }

                    else -> {
                        chartSection("Top tracks", uiState.tracks, skeleton = { trackRowsSkeleton() }) { chart ->
                            itemsIndexed(chart.entries, key = { i, e -> "track:$i:${e.item.trackHash}" }) { index, entry ->
                                TopTrackRow(
                                    rank = index + 1,
                                    entry = entry,
                                    baseUrl = uiState.baseUrl,
                                    isPlaying = entry.item.trackHash == playingTrackHash,
                                    playbackState = playbackState,
                                    onClick = { onEvent(StatsUiEvent.OnTrackClicked(index)) }
                                )
                            }
                        }

                        chartSection("Top artists", uiState.artists, skeleton = { cardRowSkeleton(CircleShape, 96.dp) }) { chart ->
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(chart.entries, key = { it.item.artistHash }) { entry ->
                                        TopArtistCard(
                                            entry = entry,
                                            baseUrl = uiState.baseUrl,
                                            onClick = { onEvent(StatsUiEvent.OnArtistClicked(entry.item.artistHash)) }
                                        )
                                    }
                                }
                            }
                        }

                        chartSection("Top albums", uiState.albums, skeleton = { cardRowSkeleton(RoundedCornerShape(10.dp), 140.dp) }) { chart ->
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(chart.entries, key = { it.item.albumHash }) { entry ->
                                        TopAlbumCard(
                                            entry = entry,
                                            baseUrl = uiState.baseUrl,
                                            onClick = { onEvent(StatsUiEvent.OnAlbumClicked(entry.item.albumHash)) }
                                        )
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

private val StatsPeriod.emptyPhrase: String
    get() = when (this) {
        StatsPeriod.WEEK -> "this week"
        StatsPeriod.MONTH -> "this month"
        StatsPeriod.YEAR -> "this year"
        StatsPeriod.ALL_TIME -> "yet"
    }

/** Loading and error show in place; an empty chart hides its section. */
private fun <T> LazyListScope.chartSection(
    title: String,
    state: ChartState<T>,
    skeleton: LazyListScope.() -> Unit,
    content: LazyListScope.(Chart<T>) -> Unit
) {
    when (state) {
        ChartState.Loading -> {
            item(key = "$title:title") { SectionTitle(title) }
            skeleton()
        }

        is ChartState.Error -> {
            item(key = "$title:title") { SectionTitle(title) }
            item(key = "$title:error") {
                Text(
                    text = state.message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        is ChartState.Loaded -> if (state.chart.entries.isNotEmpty()) {
            item(key = "$title:title") { SectionTitle(title) }
            content(state.chart)
        }
    }
}

private fun LazyListScope.trackRowsSkeleton() {
    items(5, key = { "track-skeleton:$it" }) {
        val alpha = rememberSkeletonAlpha()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { this.alpha = alpha }
                .padding(horizontal = 28.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBlock(modifier = Modifier.size(width = 12.dp, height = 12.dp))
            Spacer(modifier = Modifier.width(24.dp))
            SkeletonBlock(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(6.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBlock(modifier = Modifier.size(width = 150.dp, height = 12.dp))
                SkeletonBlock(modifier = Modifier.size(width = 96.dp, height = 10.dp))
            }
        }
    }
}

private fun LazyListScope.cardRowSkeleton(shape: Shape, size: Dp) {
    item(key = "card-skeleton:$size") {
        val alpha = rememberSkeletonAlpha()
        Row(
            modifier = Modifier
                .graphicsLayer { this.alpha = alpha }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SkeletonBlock(modifier = Modifier.size(size), shape = shape)
                    Spacer(modifier = Modifier.height(8.dp))
                    SkeletonBlock(modifier = Modifier.size(width = size * .7F, height = 10.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(selected: StatsPeriod, onSelect: (StatsPeriod) -> Unit) {
    val periods = StatsPeriod.entries
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        periods.forEachIndexed { index, period ->
            SegmentedButton(
                selected = period == selected,
                onClick = { onSelect(period) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    activeContentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(text = period.label, maxLines = 1)
            }
        }
    }
}

@Composable
private fun SummaryRow(
    summary: String,
    order: StatsOrder,
    onOrderSelected: (StatsOrder) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = summary,
            modifier = Modifier.weight(1F),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box {
            OutlinedButton(
                onClick = { menuOpen = true },
                contentPadding = PaddingValues(start = 12.dp, end = 6.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .18F)),
                modifier = Modifier.height(32.dp)
            ) {
                Text(text = order.label, style = MaterialTheme.typography.labelMedium)
                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                StatsOrder.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            menuOpen = false
                            onOrderSelected(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 8.dp),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun TopTrackRow(
    rank: Int,
    entry: ChartEntry<Track>,
    baseUrl: String,
    isPlaying: Boolean,
    playbackState: PlaybackState,
    onClick: () -> Unit
) {
    val track = entry.item
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPlaying) MaterialTheme.colorScheme.onSurface.copy(alpha = .14F)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            modifier = Modifier.width(24.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "${baseUrl}img/thumbnail/small/${track.image}",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.audio_fallback),
                error = painterResource(R.drawable.audio_fallback),
                modifier = Modifier.fillMaxSize()
            )
            if (isPlaying) PlayingTrackIndicator(playbackState = playbackState)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1F)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.trackArtists.joinToString(", ") { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = entry.helpText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(8.dp))
        TrendIcon(entry.trend)
    }
}

@Composable
private fun TopArtistCard(entry: ChartEntry<Artist>, baseUrl: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = "${baseUrl}img/artist/small/${entry.item.image}",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.artist_fallback),
            error = painterResource(R.drawable.artist_fallback),
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = entry.item.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = entry.helpText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun TopAlbumCard(entry: ChartEntry<Album>, baseUrl: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = "${baseUrl}img/thumbnail/medium/${entry.item.image}",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.audio_fallback),
            error = painterResource(R.drawable.audio_fallback),
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = entry.item.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = listOf(
                entry.item.albumArtists.joinToString(", ") { it.name },
                entry.helpText
            ).filter { it.isNotBlank() }.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TrendIcon(trend: Trend) {
    val (icon, tint, label) = when (trend) {
        Trend.RISING -> Triple(Icons.Rounded.ArrowUpward, Color(0xFF86EFAC), "Rising")
        Trend.FALLING -> Triple(Icons.Rounded.ArrowDownward, Color(0xFFFCA5A5), "Falling")
        Trend.STABLE -> Triple(Icons.Rounded.Remove, MaterialTheme.colorScheme.onSurfaceVariant, "Steady")
    }
    Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(16.dp))
}

@Composable
private fun StatsMessage(title: String, body: String, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Medium)
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        onRetry?.let {
            OutlinedButton(
                onClick = it,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .3F))
            ) { Text("Retry") }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, heightDp = 400)
@Composable
private fun StatsScreenEmptyPreview() {
    SwingMusicTheme {
        StatsScreenContent(
            uiState = StatsUiState(
                tracks = ChartState.Loaded(Chart(emptyList(), "0 total plays (0 seconds)", Trend.STABLE)),
                artists = ChartState.Loaded(Chart(emptyList(), "0 new artists", Trend.STABLE)),
                albums = ChartState.Loaded(Chart(emptyList(), "0 new albums played", Trend.STABLE))
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, heightDp = 400)
@Composable
private fun StatsScreenErrorPreview() {
    SwingMusicTheme {
        StatsScreenContent(
            uiState = StatsUiState(
                tracks = ChartState.Error("Check your connection to the server."),
                artists = ChartState.Error("Check your connection to the server."),
                albums = ChartState.Error("Check your connection to the server.")
            ),
            onEvent = {}
        )
    }
}
