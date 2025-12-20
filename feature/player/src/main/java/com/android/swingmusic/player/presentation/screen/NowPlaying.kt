package com.android.swingmusic.player.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers.RED_DOMINATED_EXAMPLE
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.RepeatMode
import com.android.swingmusic.core.domain.util.ShuffleMode
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.util.calculateCurrentOffsetForPage
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.BlurTransformation
import com.android.swingmusic.uicomponent.presentation.util.formatDuration
import com.ramcosta.composedestinations.annotation.Destination
import ir.mahozad.multiplatform.wavyslider.WaveDirection
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider
import java.util.Locale

@Composable
private fun NowPlaying(
    track: Track?,
    playingTrackIndex: Int,
    queue: List<Track>,
    seekPosition: Float = 0F,
    playbackDuration: String,
    trackDuration: String,
    playbackState: PlaybackState,
    isBuffering: Boolean,
    repeatMode: RepeatMode,
    shuffleMode: ShuffleMode,
    baseUrl: String,
    onPageSelect: (page: Int) -> Unit,
    onClickArtist: (artistHash: String) -> Unit,
    onToggleRepeatMode: (RepeatMode) -> Unit,
    onClickPrev: () -> Unit,
    onTogglePlayerState: (PlaybackState) -> Unit,
    onResumePlayBackFromError: () -> Unit,
    onClickNext: () -> Unit,
    onToggleShuffleMode: (ShuffleMode) -> Unit,
    onSeekPlayBack: (Float) -> Unit,
    onClickMore: () -> Unit,
    onClickLyricsIcon: () -> Unit,
    onToggleFavorite: (Boolean, String) -> Unit,
    onClickQueueIcon: () -> Unit
) {
    if (track == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Playing track will appear here!",
                style = MaterialTheme.typography.titleSmall
            )
        }

        return
    }

    val fileType by remember {
        derivedStateOf {
            track.filepath.substringAfterLast(".").uppercase(Locale.ROOT)
        }
    }

    val isDarkTheme = isSystemInDarkTheme()
    val inverseOnSurface = MaterialTheme.colorScheme.inverseOnSurface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val fileTypeBadgeColor = when (track.bitrate) {
        in 321..1023 -> if (isDarkTheme) Color(0xFF172B2E) else Color(0xFFAEFAF4)
        in 1024..Int.MAX_VALUE -> if (isDarkTheme) Color(0XFF443E30) else Color(0xFFFFFBCC)
        else -> inverseOnSurface
    }
    val fileTypeTextColor = when (track.bitrate) {
        in 321..1023 -> if (isDarkTheme) Color(0XFF33FFEE) else Color(0xFF172B2E)
        in 1024..Int.MAX_VALUE -> if (isDarkTheme) Color(0XFFEFE143) else Color(0xFF221700)
        else -> onSurface
    }

    val animateWave = playbackState == PlaybackState.PLAYING && isBuffering.not()
    val repeatModeIcon = when (repeatMode) {
        RepeatMode.REPEAT_ONE -> R.drawable.repeat_one
        else -> R.drawable.repeat_all
    }
    val playbackStateIcon = when (playbackState) {
        PlaybackState.PLAYING -> R.drawable.pause_icon
        PlaybackState.PAUSED -> R.drawable.play_arrow
        PlaybackState.ERROR -> R.drawable.error
    }

    val pagerState = rememberPagerState(
        initialPage = playingTrackIndex,
        pageCount = { if (queue.isEmpty()) 1 else queue.size }
    )

    var isInitialComposition by remember { mutableStateOf(true) }

    LaunchedEffect(
        key1 = playingTrackIndex,
        key2 = pagerState
    ) {
        if (playingTrackIndex in queue.indices) {
            if (playingTrackIndex != pagerState.currentPage) {
                pagerState.animateScrollToPage(playingTrackIndex)
            }
        }

        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (isInitialComposition) {
                isInitialComposition = false // Skip the first run
            } else {
                if (playingTrackIndex != page) {
                    onPageSelect(page)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.inverseOnSurface)
            )
        }
    ) { paddingValues ->
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1F),
            model = ImageRequest.Builder(LocalContext.current)
                .data("${baseUrl}img/thumbnail/${track.image}")
                .crossfade(true)
                .transformations(
                    listOf(
                        BlurTransformation(
                            scale = 0.25f,
                            radius = 25
                        )
                    )
                )
                .build(),
            contentDescription = "Track Image",
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1F)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = .75F),
                            MaterialTheme.colorScheme.surface.copy(alpha = 1F),
                            MaterialTheme.colorScheme.surface.copy(alpha = 1F)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Artwork, SeekBar...
                HorizontalPager(
                    modifier = Modifier.fillMaxWidth(),
                    state = pagerState,
                    beyondViewportPageCount = 2,
                    verticalAlignment = Alignment.CenterVertically,
                ) { page ->
                    val imageData = if (page == playingTrackIndex) {
                        "${baseUrl}img/thumbnail/${queue.getOrNull(playingTrackIndex)?.image ?: track.image}"
                    } else {
                        "${baseUrl}img/thumbnail/${queue.getOrNull(page)?.image ?: track.image}"
                    }
                    val pageOffset = pagerState.calculateCurrentOffsetForPage(page)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        // Artwork
                        AsyncImage(
                            modifier = Modifier
                                .size(356.dp)
                                .clip(RoundedCornerShape(7))
                                .graphicsLayer {
                                    val scale = lerp(1f, 1.25f, pageOffset)
                                    scaleX = scale
                                    scaleY = scale
                                    clip = true
                                    shape = RoundedCornerShape(7)
                                },
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageData)
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.audio_fallback),
                            fallback = painterResource(R.drawable.audio_fallback),
                            error = painterResource(R.drawable.audio_fallback),
                            contentDescription = "Track Image",
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.fillMaxWidth(.78F)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(modifier = Modifier.fillMaxWidth()) {
                            track.trackArtists.forEachIndexed { index, trackArtist ->
                                item {
                                    Text(
                                        modifier = Modifier
                                            .clickable(
                                                onClick = { onClickArtist(trackArtist.artistHash) },
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ),
                                        text = trackArtist.name,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F),
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (index != track.trackArtists.lastIndex) {
                                        Text(
                                            text = ", ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = .84F
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    IconButton(
                        modifier = Modifier
                            .clip(CircleShape),
                        onClick = {
                            onToggleFavorite(track.isFavorite, track.trackHash)
                        }) {
                        val icon =
                            if (track.isFavorite) R.drawable.fav_filled
                            else R.drawable.fav_not_filled
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = "Favorite"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    // TODO: Only seek playback onValueChangeFinished
                    WavySlider(
                        modifier = Modifier.height(12.dp),
                        value = seekPosition,
                        onValueChangeFinished = {},
                        onValueChange = { value ->
                            onSeekPlayBack(value)
                        },
                        waveLength = 32.dp,
                        waveHeight = if (animateWave) 8.dp else 0.dp,
                        waveVelocity = 16.dp to WaveDirection.HEAD,
                        waveThickness = 4.dp,
                        trackThickness = 4.dp,
                        incremental = false
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = playbackDuration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F)
                        )
                        Text(
                            text = if (playbackState == PlaybackState.ERROR)
                                track.duration.formatDuration() else trackDuration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5F)
                            ), onClick = {
                            onClickPrev()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.prev),
                            contentDescription = "Prev"
                        )
                    }

                    Box(
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                if (playbackState != PlaybackState.ERROR) {
                                    onTogglePlayerState(playbackState)
                                } else {
                                    onResumePlayBackFromError()
                                }
                            }
                        )
                    ) {
                        Box(
                            modifier = Modifier.wrapContentSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (playbackState == PlaybackState.ERROR) {
                                Icon(
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .size(70.dp),
                                    painter = painterResource(id = playbackStateIcon),
                                    tint = if (isBuffering)
                                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = .25F) else
                                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = .75F),
                                    contentDescription = "Error state"
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .height(70.dp)
                                        .width(80.dp)
                                        .clip(RoundedCornerShape(32))
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        modifier = Modifier.size(44.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        painter = painterResource(id = playbackStateIcon),
                                        contentDescription = "Play/Pause"
                                    )
                                }
                            }

                            if (isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(50.dp),
                                    strokeCap = StrokeCap.Round,
                                    strokeWidth = 1.dp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    IconButton(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5F)
                            ), onClick = {
                            onClickNext()
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.next),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            contentDescription = "Next"
                        )
                    }
                }

            }
            // Bitrate, Track format
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24))
                    .background(
                        if (isDarkTheme) fileTypeTextColor.copy(alpha = .075F) else fileTypeBadgeColor
                    )
                    .wrapContentSize()
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = fileType,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = fileTypeTextColor
                    )

                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = fileTypeTextColor
                    )

                    Text(
                        text = "${track.bitrate} Kbps",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = fileTypeTextColor
                    )
                }
            }

            // Navigation and Control Icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.inverseOnSurface)
                    .padding(vertical = 12.dp, horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // TODO: Return this when lyrics is ready
                /*IconButton(onClick = {
                    onClickLyricsIcon()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.lyrics_icon),
                        contentDescription = "Lyrics"
                    )
                }*/

                IconButton(onClick = {
                    onToggleRepeatMode(repeatMode)
                }) {
                    Icon(
                        painter = painterResource(id = repeatModeIcon),
                        tint = if (repeatMode == RepeatMode.REPEAT_OFF)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = .3F)
                        else MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Repeat"
                    )
                }

                IconButton(onClick = {
                    onClickQueueIcon()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.play_list),
                        contentDescription = "Queue"
                    )
                }

                IconButton(onClick = {
                    onToggleShuffleMode(shuffleMode)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.shuffle),
                        tint = if (shuffleMode == ShuffleMode.SHUFFLE_OFF)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = .3F)
                        else MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Shuffle"
                    )
                }

                // TODO: Return this when contextual menu is ready
                /*IconButton(onClick = {
                    onClickMore()
                }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More"
                    )
                }*/
            }
        }
    }
}

/**
 * Expose a public Composable tied to MediaControllerViewModel
 * **/

@Destination
@Composable
fun NowPlayingScreen(
    mediaControllerViewModel: MediaControllerViewModel,
    navigator: CommonNavigator
) {
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()

    NowPlaying(
        track = playerUiState.nowPlayingTrack,
        playingTrackIndex = playerUiState.playingTrackIndex,
        queue = playerUiState.queue,
        seekPosition = playerUiState.seekPosition,
        playbackDuration = playerUiState.playbackDuration,
        trackDuration = playerUiState.trackDuration,
        playbackState = playerUiState.playbackState,
        repeatMode = playerUiState.repeatMode,
        shuffleMode = playerUiState.shuffleMode,
        isBuffering = playerUiState.isBuffering,
        baseUrl = baseUrl ?: "",
        onPageSelect = { page ->
            // treat this as clicking a track in queue
            mediaControllerViewModel.onQueueEvent(
                QueueEvent.SeekToQueueItem(page)
            )
        },
        onClickArtist = {
            navigator.gotoArtistInfo(it)
        },
        onToggleRepeatMode = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnToggleRepeatMode
            )
        },
        onClickPrev = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnPrev
            )
        },
        onTogglePlayerState = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnTogglePlayerState
            )
        },
        onResumePlayBackFromError = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnResumePlaybackFromError
            )
        },
        onClickNext = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnNext
            )
        },
        onToggleShuffleMode = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnToggleShuffleMode(
                    toggleShuffle = true
                )
            )
        },
        onSeekPlayBack = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnSeekPlayBack(it)
            )
        },
        onClickLyricsIcon = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnClickLyricsIcon
            )
        },
        onToggleFavorite = { isFavorite, trackHash ->
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnToggleFavorite(isFavorite, trackHash)
            )
        },
        onClickQueueIcon = {
            navigator.gotoQueueScreen()
        },
        onClickMore = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnClickMore
            )
        }
    )
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = RED_DOMINATED_EXAMPLE,
    device = Devices.PIXEL_5
)
@Composable
fun FullPlayerPreview() {
    val lilPeep = TrackArtist(
        artistHash = "lilpeep123",
        image = "lilpeep.jpg",
        name = "Lil Peep"
    )

    val juice = TrackArtist(
        artistHash = "juice123",
        image = "juice.jpg",
        name = "Juice WRLD"
    )
    val young = TrackArtist(
        artistHash = "young123",
        image = "young.jpg",
        name = "Young Thug"
    )

    val albumArtists = listOf(lilPeep, juice)
    val artists = listOf(juice, young)

    val track = Track(
        album = "Sample Album",
        albumTrackArtists = albumArtists,
        albumHash = "albumHash123",
        trackArtists = artists,
        bitrate = 320,
        duration = 454, // Sample duration in seconds
        filepath = "/path/to/track.mp3",
        folder = "/path/to/folder",
        image = "/path/to/album/artwork.jpg",
        isFavorite = true,
        title = "Save Your Tears",
        trackHash = "trackHash123",
        disc = 1,
        trackNumber = 1
    )

    SwingMusicTheme() {
        NowPlaying(
            track = track,
            playingTrackIndex = 0,
            queue = emptyList(),
            seekPosition = .22F,
            playbackDuration = "01:23",
            trackDuration = "02:59",
            playbackState = PlaybackState.PLAYING,
            isBuffering = false,
            repeatMode = RepeatMode.REPEAT_OFF,
            shuffleMode = ShuffleMode.SHUFFLE_OFF,
            baseUrl = "",
            onPageSelect = {},
            onClickArtist = {},
            onToggleRepeatMode = {},
            onResumePlayBackFromError = {},
            onClickPrev = {},
            onTogglePlayerState = {},
            onClickNext = {},
            onToggleShuffleMode = {},
            onSeekPlayBack = {},
            onClickLyricsIcon = {},
            onToggleFavorite = { _, _ -> },
            onClickQueueIcon = {},
            onClickMore = {}
        )
    }
}
