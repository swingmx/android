package com.android.swingmusic.player.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.player.presentation.event.LyricsUiEvent
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.viewmodel.LyricsViewModel
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun LyricsScreen(
    mediaControllerViewModel: MediaControllerViewModel,
    navigator: CommonNavigator,
    lyricsViewModel: LyricsViewModel = hiltViewModel()
) {
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()
    val lyricsState by lyricsViewModel.state.collectAsState()
    val track = playerUiState.nowPlayingTrack

    LaunchedEffect(track?.trackHash) {
        track?.let { lyricsViewModel.onEvent(LyricsUiEvent.LoadLyrics(it)) }
    }

    LaunchedEffect(playerUiState.seekPosition, lyricsState.exists, lyricsState.synced) {
        if (lyricsState.exists && lyricsState.synced && track != null) {
            val positionMs = (playerUiState.seekPosition * track.duration * 1000F).toLong()
            lyricsViewModel.onEvent(LyricsUiEvent.PositionChanged(positionMs))
        }
    }

    Scaffold(
        topBar = {
            LyricsHeader(
                track = track,
                baseUrl = baseUrl ?: "",
                synced = lyricsState.synced,
                exists = lyricsState.exists,
                onBack = { navigator.navigateBack() },
                onClickArtist = { hash -> navigator.gotoArtistInfo(hash) }
            )
        }
    ) { padding ->
        LyricsBody(
            padding = padding,
            track = track,
            state = lyricsState,
            onSeek = { timeMs ->
                if (track != null) {
                    val durationMs = track.duration * 1000F
                    if (durationMs > 0F) {
                        val fraction = (timeMs.toFloat() / durationMs).coerceIn(0F, 1F)
                        mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnSeekPlayBack(fraction))
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
private fun LyricsHeader(
    track: Track?,
    baseUrl: String,
    synced: Boolean,
    exists: Boolean,
    onBack: () -> Unit,
    onClickArtist: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        if (track != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${baseUrl}img/thumbnail/${track.image}")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
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
                    maxLines = 1,
                    modifier = if (track.trackArtists.isNotEmpty()) {
                        Modifier.clickable {
                            onClickArtist(track.trackArtists.first().artistHash)
                        }
                    } else Modifier
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
            }
        }
    }
}

@Composable
private fun LyricsBody(
    padding: PaddingValues,
    track: Track?,
    state: com.android.swingmusic.player.presentation.state.LyricsUiState,
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

@Composable
private fun SyncedLyricsList(
    state: com.android.swingmusic.player.presentation.state.LyricsUiState,
    onSeek: (Long) -> Unit,
    onUserScrolled: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()

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
            val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
            listState.animateScrollToItem(target, scrollOffset = -(viewportHeight / 3))
            onUserScrolled(false)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(state.lines, key = { i, _ -> i }) { index, line ->
            val current = state.currentLine
            val alpha = when {
                index == current -> 1F
                index < current -> 0.55F
                index == current + 1 -> 0.9F
                index == current + 2 -> 0.8F
                else -> 0.7F
            }
            val color = if (index == current) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            }

            Text(
                text = line.text.ifBlank { "♪" },
                fontSize = 28.sp,
                fontWeight = if (index == current) FontWeight.Bold else FontWeight.SemiBold,
                color = color,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeek(line.time) }
                    .padding(vertical = 4.dp)
            )
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
        item { Spacer(Modifier.height(128.dp)) }
    }
}

@Composable
private fun UnsyncedLyricsList(
    state: com.android.swingmusic.player.presentation.state.LyricsUiState
) {
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
                color = MaterialTheme.colorScheme.onSurface
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
        item { Spacer(Modifier.height(128.dp)) }
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
