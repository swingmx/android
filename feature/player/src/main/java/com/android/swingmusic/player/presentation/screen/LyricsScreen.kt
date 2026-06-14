package com.android.swingmusic.player.presentation.screen

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.LyricsLine
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.player.presentation.event.LyricsUiEvent
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.state.LyricsUiState
import com.android.swingmusic.player.presentation.viewmodel.LyricsViewModel
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import kotlin.math.abs

/**
 * Edge-to-edge lyrics overlay shown on top of the animated player sheet.
 *
 * It renders above the player without collapsing it, so dismissing returns to the
 * expanded player rather than popping the back stack. The overlay is non-dismissable
 * by tap/swipe — it is closed only via the header chevron or the back gesture.
 */
@Composable
fun LyricsOverlay(
    visible: Boolean,
    mediaControllerViewModel: MediaControllerViewModel,
    onDismiss: () -> Unit,
    lyricsViewModel: LyricsViewModel = hiltViewModel()
) {
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()
    val lyricsState by lyricsViewModel.state.collectAsState()
    val track = playerUiState.nowPlayingTrack

    LaunchedEffect(visible, track?.trackHash) {
        if (visible) {
            track?.let { lyricsViewModel.onEvent(LyricsUiEvent.LoadLyrics(it)) }
        }
    }

    LaunchedEffect(visible, playerUiState.seekPosition, lyricsState.exists, lyricsState.synced) {
        if (visible && lyricsState.exists && lyricsState.synced && track != null) {
            val positionMs = (playerUiState.seekPosition * track.duration * 1000F).toLong()
            lyricsViewModel.onEvent(LyricsUiEvent.PositionChanged(positionMs))
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(280)) { it / 6 },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(220)) { it / 6 }
    ) {
        BackHandler(enabled = true) { onDismiss() }

        LyricsOverlayContent(
            track = track,
            baseUrl = baseUrl ?: "",
            state = lyricsState,
            loading = lyricsState.isLoading || playerUiState.isBuffering,
            playbackState = playerUiState.playbackState,
            progress = playerUiState.seekPosition,
            onDismiss = onDismiss,
            onTogglePlayback = {
                mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnTogglePlayerState)
            },
            onSeek = { timeMs ->
                if (track != null) {
                    val durationMs = track.duration * 1000F
                    if (durationMs > 0F) {
                        val fraction = (timeMs.toFloat() / durationMs).coerceIn(0F, 1F)
                        mediaControllerViewModel.onPlayerUiEvent(
                            PlayerUiEvent.OnSeekPlayBack(fraction)
                        )
                    }
                }
            },
            onUserScrolled = { lyricsViewModel.onEvent(LyricsUiEvent.SetUserScrolled(it)) },
            onSearchOnline = {
                track?.let { lyricsViewModel.onEvent(LyricsUiEvent.SearchOnline(it)) }
            }
        )
    }
}

@Composable
private fun LyricsOverlayContent(
    track: Track?,
    baseUrl: String,
    state: LyricsUiState,
    loading: Boolean,
    playbackState: PlaybackState,
    progress: Float,
    onDismiss: () -> Unit,
    onSeek: (Long) -> Unit,
    onUserScrolled: (Boolean) -> Unit,
    onSearchOnline: () -> Unit,
    onTogglePlayback: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            // Swallow taps on empty areas so they don't fall through to the player sheet
            // behind. Drags are handled by disabling the sheet's swipe while the overlay
            // is open (see AnimatedPlayerSheet); this clickable ignores drags, so the
            // lyrics list scrolls normally.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LyricsOverlayHeader(
            track = track,
            baseUrl = baseUrl,
            synced = state.synced,
            exists = state.exists,
            loading = loading,
            playbackState = playbackState,
            onDismiss = onDismiss,
            onTogglePlayback = onTogglePlayback
        )

        // Progress bar doubling as the divider: unfilled track mimics the divider
        // color, filled portion shows how far into the track playback is.
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            gapSize = 0.dp,
            drawStopIndicator = {},
            strokeCap = StrokeCap.Square
        )

        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
        ) {
            LyricsBody(
                padding = PaddingValues(0.dp),
                track = track,
                state = state,
                onSeek = onSeek,
                onUserScrolled = onUserScrolled,
                onSearchOnline = onSearchOnline
            )

            // Soft fade just below the divider so lyrics melt into the top edge
            // instead of touching the divider line.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                Color.Transparent
                            )
                        )
                    )
            )

            // Matching fade at the bottom so lyrics melt out instead of cutting off.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun LyricsOverlayHeader(
    track: Track?,
    baseUrl: String,
    synced: Boolean,
    exists: Boolean,
    loading: Boolean,
    playbackState: PlaybackState,
    onDismiss: () -> Unit,
    onTogglePlayback: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (track != null) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTogglePlayback() },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("${baseUrl}img/thumbnail/${track.image}")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35F))
                )
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        painter = painterResource(
                            id = if (playbackState == PlaybackState.PLAYING)
                                R.drawable.pause_icon else R.drawable.play_arrow
                        ),
                        contentDescription = if (playbackState == PlaybackState.PLAYING) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1F)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                val artistText = track.trackArtists.joinToString(", ") { it.name }
                Text(
                    text = artistText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7F),
                    maxLines = 1
                )
            }
            if (exists && !synced) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "unsynced",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.size(8.dp))
            }
        } else {
            Spacer(Modifier.weight(1F))
        }

        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Close lyrics"
            )
        }
    }
}

@Composable
private fun LyricsBody(
    padding: PaddingValues,
    track: Track?,
    state: LyricsUiState,
    onSeek: (Long) -> Unit,
    onUserScrolled: (Boolean) -> Unit,
    onSearchOnline: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        when {
            state.isLoading && state.lines.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.lines.isEmpty() -> {
                EmptyLyricsState(
                    message = state.errorMessage ?: "No lyrics available",
                    pluginSearching = state.pluginSearching,
                    pluginError = state.pluginError,
                    onSearchOnline = onSearchOnline
                )
            }

            state.synced -> SyncedLyricsList(
                state = state,
                onSeek = onSeek,
                onUserScrolled = onUserScrolled
            )

            else -> UnsyncedLyricsList(state = state)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SyncedLyricsList(
    state: LyricsUiState,
    onSeek: (Long) -> Unit,
    onUserScrolled: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) onUserScrolled(true)
    }

    LaunchedEffect(state.currentLine, state.trackHash) {
        if (state.currentLine < 0) return@LaunchedEffect
        val visible = listState.layoutInfo.visibleItemsInfo
        val isCentered = visible.any { it.index == state.currentLine }
                && visible.firstOrNull { it.index == state.currentLine }?.let { info ->
            val viewportStart = listState.layoutInfo.viewportStartOffset
            val viewportEnd = listState.layoutInfo.viewportEndOffset
            val third = (viewportEnd - viewportStart) / 3
            info.offset >= viewportStart + third && info.offset <= viewportEnd - third
        } == true

        if (!state.userScrolled || !isCentered) {
            val target = state.currentLine.coerceAtLeast(0)
            val viewportHeight =
                listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
            listState.animateScrollToItem(target, scrollOffset = -(viewportHeight / 3))
            onUserScrolled(false)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        itemsIndexed(state.lines, key = { i, _ -> i }) { index, line ->
            val current = state.currentLine
            val isActive = index == current
            val distance = if (current < 0) Int.MAX_VALUE else abs(index - current)
            val isPast = current >= 0 && index < current

            val targetScale = when {
                isActive -> 1F
                distance == 1 -> 0.72F
                distance == 2 -> 0.62F
                else -> 0.56F
            }
            val targetAlpha = when {
                isActive -> 1F
                isPast -> 0.35F
                distance == 1 -> 0.75F
                distance == 2 -> 0.55F
                else -> 0.4F
            }

            val animatedScale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                label = "lyricScale"
            )
            val animatedColor by animateColorAsState(
                targetValue = MaterialTheme.colorScheme.onSurface.copy(alpha = targetAlpha),
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                label = "lyricColor"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onSeek(line.time) },
                        onLongClick = {
                            if (line.text.isNotBlank()) {
                                clipboard.setText(AnnotatedString(line.text.trim()))
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            ) {
                Text(
                    text = line.text.ifBlank { "♪" },
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                    color = animatedColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                            transformOrigin = TransformOrigin(0F, 0.5F)
                        }
                )
            }
        }

        if (state.copyright.isNotBlank()) {
            item {
                Text(
                    text = state.copyright,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F),
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UnsyncedLyricsList(
    state: com.android.swingmusic.player.presentation.state.LyricsUiState
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.lines) { line ->
            Text(
                text = line.text.ifBlank { " " },
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.combinedClickable(
                    onClick = { },
                    onLongClick = {
                        if (line.text.isNotBlank()) {
                            clipboard.setText(AnnotatedString(line.text.trim()))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            )
        }
        if (state.copyright.isNotBlank()) {
            item {
                Text(
                    text = state.copyright,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F)
                )
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun EmptyLyricsState(
    message: String,
    pluginSearching: Boolean,
    pluginError: String?,
    onSearchOnline: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7F)
            )
            Spacer(Modifier.height(16.dp))
            if (pluginSearching) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Searching online…", style = MaterialTheme.typography.bodySmall)
            } else if (!pluginError.isNullOrBlank()) {
                Text(
                    text = pluginError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                TextButton(onClick = onSearchOnline) {
                    Text("Search online")
                }
            }
        }
    }
}

@PreviewDynamicColors
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL_5,
    showBackground = true
)
@Composable
fun LyricsOverlayPreview() {
    val sia = TrackArtist(
        artistHash = "sia123",
        image = "sia.jpg",
        name = "Sia"
    )

    val track = Track(
        album = "This Is Acting",
        albumTrackArtists = listOf(sia),
        albumHash = "albumHash123",
        trackArtists = listOf(sia),
        bitrate = 320,
        duration = 240,
        filepath = "/path/to/track.mp3",
        folder = "/path/to/folder",
        image = "/path/to/album/artwork.jpg",
        isFavorite = false,
        title = "Bird Set Free",
        trackHash = "trackHash123",
        disc = 1,
        trackNumber = 1
    )

    val lines = listOf(
        "Holding my breath against the cold",
        "Waiting for the morning to call",
        "But there's a fire in my chest",
        "Burning brighter than before",
        "I will rise above the noise",
        "And walk straight through the open door",
        "No more hiding, no more fear",
        "Let the silence break apart"
    ).mapIndexed { index, text -> LyricsLine(time = index * 6000L, text = text) }

    val state = LyricsUiState(
        lines = lines,
        synced = true,
        exists = true,
        currentLine = 2,
        trackHash = track.trackHash
    )


    SwingMusicTheme(dynamicColor = true) {
        Surface() {
            LyricsOverlayContent(
                track = track,
                baseUrl = "",
                state = state,
                loading = false,
                playbackState = PlaybackState.PLAYING,
                progress = 0.35F,
                onDismiss = {},
                onSeek = {},
                onUserScrolled = {},
                onSearchOnline = {},
                onTogglePlayback = {}
            )
        }
    }
}
