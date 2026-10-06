package com.android.swingmusic.player.presentation.screen

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
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
import kotlin.math.min
import kotlinx.coroutines.isActive

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
    val positionMs = remember { mutableLongStateOf(0L) }
    val durationMs = (track?.duration ?: 0) * 1000L

    fun syncPosition() {
        val controller = mediaControllerViewModel.getMediaController() ?: return
        positionMs.longValue = controller.currentPosition
        lyricsViewModel.onEvent(LyricsUiEvent.PositionChanged(positionMs.longValue))
    }

    LaunchedEffect(visible, track?.trackHash) {
        if (visible) {
            track?.let { lyricsViewModel.onEvent(LyricsUiEvent.LoadLyrics(it)) }
        }
    }

    LaunchedEffect(visible, playerUiState.seekPosition, lyricsState.exists, lyricsState.synced) {
        if (visible && lyricsState.exists && lyricsState.synced) syncPosition()
    }

    LaunchedEffect(visible, playerUiState.playbackState, lyricsState.exists, lyricsState.synced) {
        if (!visible || !lyricsState.exists || !lyricsState.synced) return@LaunchedEffect
        if (playerUiState.playbackState != PlaybackState.PLAYING) return@LaunchedEffect
        while (isActive) {
            withFrameMillis { }
            syncPosition()
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
            positionMs = { positionMs.longValue },
            durationMs = durationMs,
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
    positionMs: () -> Long,
    durationMs: Long,
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

        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
        ) {
            LyricsBody(
                padding = PaddingValues(0.dp),
                track = track,
                state = state,
                positionMs = positionMs,
                durationMs = durationMs,
                onSeek = onSeek,
                onUserScrolled = onUserScrolled,
                onSearchOnline = onSearchOnline
            )

            // Soft fade below the header so lyrics melt into the top edge.
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
    positionMs: () -> Long,
    durationMs: Long,
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
                positionMs = positionMs,
                durationMs = durationMs,
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
    positionMs: () -> Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    onUserScrolled: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val isDragged by listState.interactionSource.collectIsDraggedAsState()
    var autoScrolling by remember { mutableStateOf(false) }
    val manualScroll by remember {
        derivedStateOf { isDragged || (listState.isScrollInProgress && !autoScrolling) }
    }

    LaunchedEffect(manualScroll) {
        if (manualScroll) onUserScrolled(true)
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
            val anchor = viewportHeight / 3
            val targetInfo = visible.firstOrNull { it.index == target }
            autoScrolling = true
            try {
                if (targetInfo != null) {
                    listState.animateScrollBy(
                        value = (targetInfo.offset - anchor).toFloat(),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                } else {
                    listState.animateScrollToItem(target, scrollOffset = -anchor)
                }
            } finally {
                autoScrolling = false
            }
            onUserScrolled(false)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 32.dp)
    ) {
        itemsIndexed(state.lines, key = { i, _ -> i }) { index, line ->
            val current = state.currentLine
            val distance = if (current < 0) Int.MAX_VALUE else abs(index - current)
            val nextTimeMs = state.lines.getOrNull(index + 1)?.time
                ?: durationMs.takeIf { it > line.time }
                ?: (line.time + 5_000L)

            SyncedLyricLine(
                line = line,
                nextTimeMs = nextTimeMs,
                isActive = index == current,
                distance = distance,
                blurEnabled = !manualScroll,
                wordSweep = state.wordSweep,
                positionMs = positionMs,
                onClick = { onSeek(line.time) },
                onLongClick = {
                    if (line.text.isNotBlank()) {
                        clipboard.setText(AnnotatedString(line.text))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        if (state.copyright.isNotBlank()) {
            item {
                Text(
                    text = state.copyright,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F),
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)
                )
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

private val LyricTextStyle = TextStyle(
    fontSize = 32.sp,
    lineHeight = 37.sp,
    fontWeight = FontWeight.Bold
)

private const val INACTIVE_LINE_ALPHA = 0.3F
private const val UNSUNG_ACTIVE_ALPHA = 0.45F

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SyncedLyricLine(
    line: LyricsLine,
    nextTimeMs: Long,
    isActive: Boolean,
    distance: Int,
    blurEnabled: Boolean,
    wordSweep: Boolean,
    positionMs: () -> Long,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val text = line.text.ifBlank { "• • •" }
    val textLayout = remember { mutableStateOf<TextLayoutResult?>(null) }

    val baseColor by animateColorAsState(
        targetValue = onSurface.copy(alpha = if (isActive) UNSUNG_ACTIVE_ALPHA else INACTIVE_LINE_ALPHA),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "lyricColor"
    )
    val highlightAlpha by animateFloatAsState(
        targetValue = if (isActive) 1F else 0F,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "lyricHighlight"
    )
    val blurRadius by animateDpAsState(
        targetValue = when {
            !blurEnabled || isActive -> 0.dp
            distance == 1 -> 1.5.dp
            distance == 2 -> 2.5.dp
            else -> 3.5.dp
        },
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "lyricBlur"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = LyricTextStyle,
            color = baseColor,
            onTextLayout = { textLayout.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .blur(blurRadius, BlurredEdgeTreatment.Unbounded)
        )

        if (highlightAlpha > 0F) {
            Text(
                text = text,
                style = LyricTextStyle,
                color = onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = highlightAlpha
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()
                        // Without the sweep the whole active line stays lit.
                        if (!wordSweep) return@drawWithContent
                        val layout = textLayout.value ?: return@drawWithContent
                        val progress = sweepProgress(line, nextTimeMs, positionMs())
                        eraseUnsung(layout, progress, feather = 16.dp.toPx())
                    }
            )
        }
    }
}

/**
 * Fraction of the line that has been sung. Paced by characters between this line and the
 * next, but capped by text length so a line followed by a long instrumental break doesn't
 * crawl. Blank (instrumental) lines fill across the whole gap.
 */
private fun sweepProgress(line: LyricsLine, nextTimeMs: Long, positionMs: Long): Float {
    val window = nextTimeMs - line.time
    if (window <= 0L) return 1F
    val span = if (line.text.isBlank()) window else min(window, line.text.length * 80L + 1_000L)
    return ((positionMs - line.time).toFloat() / span).coerceIn(0F, 1F)
}

/** Erases the not-yet-sung part of the highlight layer, wrapping row by row. */
private fun DrawScope.eraseUnsung(layout: TextLayoutResult, progress: Float, feather: Float) {
    val length = layout.layoutInput.text.length
    if (length == 0 || progress >= 1F) return
    if (progress <= 0F) {
        drawRect(Color.Black, blendMode = BlendMode.DstOut)
        return
    }

    val exact = progress * length
    val charIndex = exact.toInt().coerceIn(0, length - 1)
    val box = layout.getBoundingBox(charIndex)
    val edgeX = box.left + box.width * (exact - charIndex)
    val row = layout.getLineForOffset(charIndex)
    val rowTop = layout.getLineTop(row)
    val rowBottom = layout.getLineBottom(row)

    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Black),
            startX = edgeX - feather / 2,
            endX = edgeX + feather / 2
        ),
        topLeft = Offset(0F, rowTop),
        size = Size(size.width, rowBottom - rowTop),
        blendMode = BlendMode.DstOut
    )
    if (rowBottom < size.height) {
        drawRect(
            color = Color.Black,
            topLeft = Offset(0F, rowBottom),
            size = Size(size.width, size.height - rowBottom),
            blendMode = BlendMode.DstOut
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UnsyncedLyricsList(
    state: LyricsUiState
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 32.dp)
    ) {
        items(state.lines) { line ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { },
                        onLongClick = {
                            if (line.text.isNotBlank()) {
                                clipboard.setText(AnnotatedString(line.text))
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = line.text.ifBlank { "♪" },
                    style = LyricTextStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (state.copyright.isNotBlank()) {
            item {
                Text(
                    text = state.copyright,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F),
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)
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


    SwingMusicTheme {
        Surface() {
            LyricsOverlayContent(
                track = track,
                baseUrl = "",
                state = state,
                loading = false,
                playbackState = PlaybackState.PLAYING,
                positionMs = { 14_000L },
                durationMs = 240_000L,
                onDismiss = {},
                onSeek = {},
                onUserScrolled = {},
                onSearchOnline = {},
                onTogglePlayback = {}
            )
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
fun UnsyncedLyricsListPreview() {
    val lines = listOf(
        "Holding my breath against the cold",
        "Waiting for the morning to call",
        "But there's a fire in my chest",
        "Burning brighter than before",
        "",
        "I will rise above the noise",
        "And walk straight through the open door",
        "No more hiding, no more fear",
        "Let the silence break apart"
    ).map { text -> LyricsLine(time = 0L, text = text) }

    val state = LyricsUiState(
        lines = lines,
        synced = false,
        exists = true,
        currentLine = -1,
        copyright = "Lyrics licensed & provided by LyricFind",
        trackHash = "trackHash123"
    )

    SwingMusicTheme {
        Surface {
            UnsyncedLyricsList(state = state)
        }
    }
}
