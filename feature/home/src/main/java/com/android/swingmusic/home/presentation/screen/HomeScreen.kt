package com.android.swingmusic.home.presentation.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.HomeItem
import com.android.swingmusic.core.domain.model.HomeSection
import com.android.swingmusic.core.domain.model.Mix
import com.android.swingmusic.core.domain.model.MixImage
import com.android.swingmusic.core.domain.model.Playlist
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.home.presentation.component.HomeCard
import com.android.swingmusic.home.presentation.component.HomeCardWidth
import com.android.swingmusic.home.presentation.event.HomeUiEffect
import com.android.swingmusic.home.presentation.event.HomeUiEvent
import com.android.swingmusic.home.presentation.state.HomeUiState
import com.android.swingmusic.home.presentation.state.key
import com.android.swingmusic.home.presentation.viewmodel.HomeViewModel
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@Destination
@Composable
internal fun HomeScreen(
    navigator: CommonNavigator,
    mediaControllerViewModel: MediaControllerViewModel,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        mediaControllerViewModel.refreshBaseUrl()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(HomeUiEvent.OnScreenResumed)
    }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            is HomeUiEffect.NavigateToAlbum -> navigator.gotoAlbumWithInfo(effect.albumHash)
            is HomeUiEffect.NavigateToArtist -> navigator.gotoArtistInfo(effect.artistHash)
            is HomeUiEffect.PlayTracks -> mediaControllerViewModel.onQueueEvent(
                QueueEvent.RecreateQueue(
                    source = effect.source,
                    queue = effect.tracks,
                    clickedTrackIndex = 0
                )
            )

            is HomeUiEffect.ShowSnackBar -> scope.launch {
                snackbarHostState.showSnackbar(effect.message)
            }

            HomeUiEffect.NavigateToProfile -> navigator.gotoProfile()
        }
    }

    HomeScreenContent(
        uiState = uiState,
        nowPlayingTrackHash = playerUiState.nowPlayingTrack?.trackHash,
        playbackState = playerUiState.playbackState,
        onEvent = viewModel::onEvent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    nowPlayingTrackHash: String?,
    playbackState: PlaybackState,
    onEvent: (HomeUiEvent) -> Unit,
    snackbarHost: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // 0 while the large title is fully visible, 1 once it has scrolled under the top bar
    val titleCollapse by remember {
        derivedStateOf {
            val first = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            when {
                listState.firstVisibleItemIndex > 0 -> 1F
                first == null || first.size == 0 -> 0F
                else -> (listState.firstVisibleItemScrollOffset / first.size.toFloat()).coerceIn(0F, 1F)
            }
        }
    }

    // Like iOS: hand over from the large title to the small one in a single quick fade,
    // so only one is ever visible
    val showSmallTitle by remember { derivedStateOf { titleCollapse >= 0.95F } }
    val titleHandover by animateFloatAsState(
        targetValue = if (showSmallTitle) 1F else 0F,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "homeTitleHandover"
    )

    // The bar stays clear at the top of the page and darkens once content scrolls under it
    val scrimFadeDistance = with(LocalDensity.current) { 24.dp.toPx() }
    val scrimAlpha by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1F
            else (listState.firstVisibleItemScrollOffset / scrimFadeDistance).coerceIn(0F, 1F)
        }
    }

    val topBarHeight = TopAppBarDefaults.windowInsets.asPaddingValues()
        .calculateTopPadding() + HomeTopBarHeight
    val refreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = snackbarHost,
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { onEvent(HomeUiEvent.OnRefresh) },
            state = refreshState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = refreshState,
                    isRefreshing = uiState.isRefreshing,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = topBarHeight)
                )
            }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                // Content starts below the bar but scrolls up behind it
                contentPadding = PaddingValues(top = topBarHeight, bottom = 200.dp)
            ) {
                item(key = "title") {
                    Text(
                        text = HOME_TITLE,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            .graphicsLayer {
                                // Fade with the scroll, then drift up into the bar while fading out
                                alpha = (1F - titleCollapse * 0.8F) * (1F - titleHandover)
                                val scale = 1F - 0.04F * titleHandover
                                scaleX = scale
                                scaleY = scale
                                translationY = -8.dp.toPx() * titleHandover
                                transformOrigin = TransformOrigin(0F, 0.5F)
                            }
                    )
                }

                when {
                    uiState.isLoadingSections -> homeSkeleton()

                    uiState.sections.isNotEmpty() -> homeSections(
                        sections = uiState.sections,
                        baseUrl = uiState.baseUrl,
                        loadingItemKey = uiState.loadingItemKey,
                        nowPlayingTrackHash = nowPlayingTrackHash,
                        playbackState = playbackState,
                        onItemClick = { onEvent(HomeUiEvent.OnItemClicked(it)) }
                    )

                    uiState.errorLoadingSections != null -> item {
                        HomeMessage(
                            icon = Icons.Rounded.CloudOff,
                            title = "Couldn't load your home",
                            message = uiState.errorLoadingSections,
                            actionLabel = "Retry",
                            onAction = { onEvent(HomeUiEvent.OnRetry) }
                        )
                    }

                    else -> item {
                        HomeMessage(
                            icon = Icons.Rounded.LibraryMusic,
                            title = "Nothing here yet",
                            message = "Play some music and your recent plays, top artists and new additions will show up here.",
                            actionLabel = "Refresh",
                            onAction = { onEvent(HomeUiEvent.OnRetry) }
                        )
                    }
                }
            }

            HomeTopBar(
                titleHandover = { titleHandover },
                scrimAlpha = { scrimAlpha },
                onProfileClick = { onEvent(HomeUiEvent.OnProfileClicked) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    titleHandover: () -> Float,
    scrimAlpha: () -> Float,
    onProfileClick: () -> Unit,
) {
    val surface = MaterialTheme.colorScheme.surface
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(brush = scrimBrush(surface), alpha = scrimAlpha())
            }
            .windowInsetsPadding(TopAppBarDefaults.windowInsets)
            .padding(bottom = HomeTopBarFade)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HomeTopBarHeight)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = HOME_TITLE,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1F)
                    .graphicsLayer {
                        // Rise into place while fading in
                        val progress = titleHandover()
                        alpha = progress
                        val scale = 0.96F + 0.04F * progress
                        scaleX = scale
                        scaleY = scale
                        translationY = 8.dp.toPx() * (1F - progress)
                        transformOrigin = TransformOrigin(0F, 0.5F)
                    }
            )
            IconButton(onClick = onProfileClick) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Profile",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

private val HomeTopBarHeight = 64.dp
private val HomeTopBarFade = 68.dp

/**
 * One continuous fade from opaque at the top of the screen to transparent below the bar.
 * Follows 1 - t², which leaves the top flat (no visible start) and keeps the title area dark.
 */
private fun scrimBrush(color: Color): Brush {
    val steps = 32
    val stops = Array(steps + 1) { i ->
        val t = i / steps.toFloat()
        t to color.copy(alpha = 1F - t * t)
    }
    return Brush.verticalGradient(*stops)
}

private fun LazyListScope.homeSections(
    sections: List<HomeSection>,
    baseUrl: String,
    loadingItemKey: String?,
    nowPlayingTrackHash: String?,
    playbackState: PlaybackState,
    onItemClick: (HomeItem) -> Unit,
) {
    items(sections) { section ->
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = section.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                if (section.description.isNotBlank()) {
                    Text(
                        text = section.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(section.items) { item ->
                    HomeCard(
                        item = item,
                        baseUrl = baseUrl,
                        isLoading = loadingItemKey == item.key,
                        onClick = { onItemClick(item) },
                        nowPlayingState = playbackState.takeIf {
                            item is HomeItem.TrackItem &&
                                    item.track.trackHash == nowPlayingTrackHash
                        }
                    )
                }
            }
        }
    }
}

private fun LazyListScope.homeSkeleton() {
    items(3) {
        val pulse by rememberInfiniteTransition(label = "homeSkeleton").animateFloat(
            initialValue = 0.5F,
            targetValue = 1F,
            animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
            label = "homeSkeletonAlpha"
        )
        val block = MaterialTheme.colorScheme.surfaceContainerHigh

        Column(
            modifier = Modifier
                .graphicsLayer { alpha = pulse }
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 170.dp, height = 22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(block)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(HomeCardWidth)
                                .clip(RoundedCornerShape(12.dp))
                                .background(block)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 110.dp, height = 12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(block)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeMessage(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text(text = actionLabel, fontWeight = FontWeight.SemiBold)
        }
    }
}

private const val HOME_TITLE = "Home"

private val previewArtist = Artist(
    artistHash = "juice",
    colors = emptyList(),
    createdDate = 0.0,
    helpText = "",
    image = "",
    name = "Juice WRLD"
)

private val previewTrack = Track(
    album = "Certified Lover Boy",
    albumTrackArtists = emptyList(),
    albumHash = "clb",
    trackArtists = listOf(TrackArtist(artistHash = "drake", image = "", name = "Drake")),
    bitrate = 320,
    duration = 248,
    filepath = "",
    folder = "",
    image = "",
    isFavorite = false,
    title = "Toosie Slide",
    trackHash = "toosie",
    disc = 1,
    trackNumber = 1
)

private val previewSections = listOf(
    HomeSection(
        key = "recently_played",
        title = "Recently played",
        description = "",
        items = listOf(
            HomeItem.TrackItem(previewTrack),
            HomeItem.ArtistItem(previewArtist),
            HomeItem.MixItem(
                Mix(
                    id = "1",
                    title = "Juice WRLD Radio",
                    description = "",
                    sourceHash = "juice",
                    ogSourceHash = "juice",
                    type = "artist",
                    image = null,
                    color = "rgb(168, 116, 58)",
                    images = listOf(MixImage("a", true), MixImage("b", false), MixImage("c", false)),
                    trackCount = 40
                )
            ),
            HomeItem.AlbumItem(
                Album(
                    albumArtists = listOf(previewArtist),
                    albumHash = "lnd",
                    colors = emptyList(),
                    createdDate = 0.0,
                    date = 2020,
                    helpText = "",
                    image = "",
                    title = "Legends Never Die",
                    versions = emptyList()
                )
            ),
            HomeItem.PlaylistItem(
                Playlist(
                    id = "7",
                    name = "Late Night Drive",
                    customImage = null,
                    images = listOf("a", "b", "c", "d"),
                    trackCount = 32
                )
            )
        )
    ),
    HomeSection(
        key = "top_artists",
        title = "Top artists this week",
        description = "Your most played artists since Monday",
        items = listOf(HomeItem.ArtistItem(previewArtist.copy(artistHash = "sia", name = "Sia")))
    )
)

@Composable
private fun HomeScreenContentPreview(uiState: HomeUiState) {
    SwingMusicTheme {
        HomeScreenContent(
            uiState = uiState,
            nowPlayingTrackHash = previewTrack.trackHash,
            playbackState = PlaybackState.PLAYING,
            onEvent = {},
            snackbarHost = {},
        )
    }
}

@Preview(name = "Loaded", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HomeScreenLoadedPreview() {
    HomeScreenContentPreview(HomeUiState(isLoadingSections = false, sections = previewSections))
}

@Preview(name = "Loading", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HomeScreenLoadingPreview() {
    HomeScreenContentPreview(HomeUiState(isLoadingSections = true))
}

@Preview(name = "Empty", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HomeScreenEmptyPreview() {
    HomeScreenContentPreview(HomeUiState(isLoadingSections = false))
}

@Preview(name = "Error", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HomeScreenErrorPreview() {
    HomeScreenContentPreview(
        HomeUiState(
            isLoadingSections = false,
            errorLoadingSections = "Check your connection to the server and try again."
        )
    )
}
