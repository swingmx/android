@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.swingmusic.player.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.core.domain.util.RepeatMode
import com.android.swingmusic.core.domain.util.ShuffleMode
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.util.calculateCurrentOffsetForPage
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.SoundSignalBars
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.util.BlurTransformation
import com.android.swingmusic.uicomponent.presentation.util.formatDuration
import ir.mahozad.multiplatform.wavyslider.WaveAnimationSpecs
import ir.mahozad.multiplatform.wavyslider.WaveDirection
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

// Constants for sheet sizing (matching SheetDemo)
private val INITIAL_IMAGE_SIZE = 38.dp
private val INITIAL_PADDING = 8.dp
private val TOTAL_INITIAL_SIZE = INITIAL_IMAGE_SIZE + INITIAL_PADDING

/**
 * Animated Bottom Sheet Player that transforms from a mini player to full player.
 *
 * Animation Behavior:
 * - Image transforms from 38dp square to full width based on expansion progress
 * - Content fades in at 20% expansion with dynamic spacing and padding
 * - Sheet corners transition from rounded to square during expansion
 * - Navigation bar in parent should animate out based on onProgressChange callback
 *
 * Queue Sheet:
 * - Appears when primary sheet is 95%+ expanded
 * - Custom draggable implementation with spring animations
 * - Reverse animation effect: queue expansion reverses primary sheet visuals
 */
@Composable
fun AnimatedPlayerSheet(
    paddingValues: PaddingValues,
    mediaControllerViewModel: MediaControllerViewModel,
    onProgressChange: (progress: Float) -> Unit = {},
    onClickArtist: (artistHash: String) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()

    val track = playerUiState.nowPlayingTrack

    // If no track playing, just show content without the sheet
    if (track == null) {
        content(paddingValues)
        return
    }

    // Dynamic sheet corner shape
    var dynamicShape by remember {
        mutableStateOf(
            RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        )
    }

    // Track primary sheet progress for queue sheet trigger
    var primarySheetProgress by remember { mutableFloatStateOf(0f) }

    // Queue sheet calculations
    val configuration = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.containerSize.height.dp.toPx() }
    val queueInitialOffset = screenHeightPx * 1f // push sheet off-screen

    // Calculate expanded offset based on initial image size + padding + system bar
    val systemBarHeight = paddingValues.calculateTopPadding()
    val imageHeightWithPadding = INITIAL_IMAGE_SIZE + (INITIAL_PADDING * 2) + systemBarHeight
    val queueExpandedOffset = with(density) { imageHeightWithPadding.toPx() }

    val queueSheetOffset = remember { Animatable(queueInitialOffset, Float.VectorConverter) }

    val coroutineScope = rememberCoroutineScope()

    // Peek height: image size + paddings (sits on top of bottom nav)
    val calculatedPeekHeight =
        TOTAL_INITIAL_SIZE + INITIAL_PADDING + (INITIAL_PADDING / 2) + paddingValues.calculateBottomPadding()

    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true,
            confirmValueChange = { newValue ->
                newValue != SheetValue.Hidden
            }
        )
    )

    // Check if queue sheet is open
    val isQueueSheetOpen = queueSheetOffset.value < (screenHeightPx * 0.25f)

    // Calculate queue progress for primary sheet visual effects
    val queueProgress by remember {
        derivedStateOf {
            val progress =
                (queueInitialOffset - queueSheetOffset.value) / (queueInitialOffset - queueExpandedOffset)
            progress.coerceIn(0f, 1f)
        }
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetPeekHeight = calculatedPeekHeight,
        sheetMaxWidth = Dp.Unspecified,
        sheetDragHandle = {},
        sheetShape = dynamicShape,
        sheetContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
        sheetSwipeEnabled = !isQueueSheetOpen,
        sheetContent = {
            AnimatedSheetContent(
                track = track,
                queue = playerUiState.queue,
                playingTrackIndex = playerUiState.playingTrackIndex,
                seekPosition = playerUiState.seekPosition,
                playbackDuration = playerUiState.playbackDuration,
                trackDuration = playerUiState.trackDuration,
                playbackState = playerUiState.playbackState,
                isBuffering = playerUiState.isBuffering,
                repeatMode = playerUiState.repeatMode,
                shuffleMode = playerUiState.shuffleMode,
                baseUrl = baseUrl ?: "",
                bottomSheetState = bottomSheetState,
                systemBarHeight = systemBarHeight,
                onShapeChange = { shape -> dynamicShape = shape },
                onProgressChange = { progress ->
                    primarySheetProgress = progress
                    onProgressChange(progress)
                },
                primarySheetProgress = primarySheetProgress,
                queueSheetOffset = queueSheetOffset,
                queueInitialOffset = queueInitialOffset,
                queueExpandedOffset = queueExpandedOffset,
                queueProgress = queueProgress,
                onPageSelect = { page ->
                    mediaControllerViewModel.onQueueEvent(QueueEvent.SeekToQueueItem(page))
                },
                onClickArtist = onClickArtist,
                onToggleRepeatMode = {
                    mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnToggleRepeatMode)
                },
                onClickPrev = {
                    mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnPrev)
                },
                onTogglePlayerState = {
                    mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnTogglePlayerState)
                },
                onResumePlayBackFromError = {
                    mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnResumePlaybackFromError)
                },
                onClickNext = {
                    mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnNext)
                },
                onToggleShuffleMode = {
                    mediaControllerViewModel.onPlayerUiEvent(
                        PlayerUiEvent.OnToggleShuffleMode(toggleShuffle = true)
                    )
                },
                onSeekPlayBack = { value ->
                    mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnSeekPlayBack(value))
                },
                onToggleFavorite = { isFavorite, trackHash ->
                    mediaControllerViewModel.onPlayerUiEvent(
                        PlayerUiEvent.OnToggleFavorite(isFavorite, trackHash)
                    )
                }
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }

    // Queue Sheet - appears when primary sheet is fully expanded
    if (primarySheetProgress >= 0.95f) {
        QueueSheetOverlay(
            queue = playerUiState.queue,
            source = playerUiState.source,
            playingTrackIndex = playerUiState.playingTrackIndex,
            playingTrack = track,
            playbackState = playerUiState.playbackState,
            baseUrl = baseUrl ?: "",
            animatedOffset = queueSheetOffset,
            initialOffset = queueInitialOffset,
            expandedOffset = queueExpandedOffset,
            onClickQueueItem = { index ->
                mediaControllerViewModel.onQueueEvent(QueueEvent.SeekToQueueItem(index))
            },
            onTogglePlayerState = {
                mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnTogglePlayerState)
            }
        )
    }
}

@Composable
private fun AnimatedSheetContent(
    track: Track,
    queue: List<Track>,
    playingTrackIndex: Int,
    seekPosition: Float,
    playbackDuration: String,
    trackDuration: String,
    playbackState: PlaybackState,
    isBuffering: Boolean,
    repeatMode: RepeatMode,
    shuffleMode: ShuffleMode,
    baseUrl: String,
    bottomSheetState: BottomSheetScaffoldState,
    systemBarHeight: Dp,
    onShapeChange: (RoundedCornerShape) -> Unit,
    onProgressChange: (Float) -> Unit,
    primarySheetProgress: Float,
    queueSheetOffset: Animatable<Float, AnimationVector1D>,
    queueInitialOffset: Float,
    queueExpandedOffset: Float,
    queueProgress: Float,
    onPageSelect: (Int) -> Unit,
    onClickArtist: (String) -> Unit,
    onToggleRepeatMode: () -> Unit,
    onClickPrev: () -> Unit,
    onTogglePlayerState: () -> Unit,
    onResumePlayBackFromError: () -> Unit,
    onClickNext: () -> Unit,
    onToggleShuffleMode: () -> Unit,
    onSeekPlayBack: (Float) -> Unit,
    onToggleFavorite: (Boolean, String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthDp = configuration.screenWidthDp.dp

    // Horizontal swipe state for collapsed mode
    var swipeDistance by remember { mutableFloatStateOf(0f) }

    // Store initial offset when first available
    val initialOffset = remember { mutableStateOf<Float?>(null) }

    // Calculate progress using actual initial offset
    val progress = remember {
        derivedStateOf {
            try {
                val currentOffset = bottomSheetState.bottomSheetState.requireOffset()

                if (initialOffset.value == null && currentOffset.isFinite() && currentOffset > 0f) {
                    initialOffset.value = currentOffset
                }

                val collapsedOffset = initialOffset.value ?: currentOffset
                val expandedOffset = 0f
                val range = collapsedOffset - expandedOffset

                // Avoid division by zero or NaN
                if (range <= 0f || !range.isFinite()) {
                    when (bottomSheetState.bottomSheetState.currentValue) {
                        SheetValue.PartiallyExpanded -> 0f
                        SheetValue.Expanded -> 1f
                        SheetValue.Hidden -> 0f
                    }
                } else {
                    val rawProgress = (collapsedOffset - currentOffset) / range
                    rawProgress.coerceIn(0f, 1f)
                }
            } catch (e: Exception) {
                when (bottomSheetState.bottomSheetState.currentValue) {
                    SheetValue.PartiallyExpanded -> 0f
                    SheetValue.Expanded -> 1f
                    SheetValue.Hidden -> 0f
                }
            }
        }
    }

    // Effective progress considers queue sheet position
    // When queue is fully up, effective progress becomes 0 (image shrinks back)
    val effectiveProgress = progress.value * (1f - queueProgress)

    // Image size interpolation with easing curve
    val fraction = effectiveProgress.pow(0.75f)
    val imageSize = lerp(INITIAL_IMAGE_SIZE, screenWidthDp, fraction).coerceAtMost(screenWidthDp)

    // Image corner radius
    val imageCornerRadius = lerp(8.dp, 16.dp, effectiveProgress)

    // Dynamic top padding to avoid status bar overlay
    val imageTopPadding = lerp(0.dp, systemBarHeight, progress.value)

    // Dynamic spacing between image and content
    val imageContentSpacing = lerp(60.dp, 16.dp, progress.value)

    // Dynamic sheet corner radius: 12dp at peek → 0dp at 50% progress
    val sheetCornerRadius = lerp(12.dp, 0.dp, (progress.value / 0.5f).coerceIn(0f, 1f))

    // Dynamic container padding
    val containerPadding = lerp(INITIAL_PADDING, 24.dp, effectiveProgress)

    // Content opacity: starts at 20%, full at 80%
    val contentOpacity = ((effectiveProgress - 0.2f) / 0.6f).coerceIn(0f, 1f)

    // Mini player elements opacity (inverse)
    // Visible when: collapsed OR queue sheet is nearly fully expanded
    val collapsedOpacity = (1f - (progress.value / 0.3f)).coerceIn(0f, 1f)
    val queueExpandedOpacity = ((queueProgress - 0.7f) / 0.3f).coerceIn(0f, 1f)
    val miniPlayerOpacity = maxOf(collapsedOpacity, queueExpandedOpacity)

    // Update the sheet shape
    LaunchedEffect(sheetCornerRadius) {
        onShapeChange(RoundedCornerShape(topStart = sheetCornerRadius, topEnd = sheetCornerRadius))
    }

    // Notify progress changes
    LaunchedEffect(progress.value) {
        onProgressChange(progress.value)
    }

    // Track info for file type badge
    val fileType by remember(track.filepath) {
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

    val animateWave = playbackState == PlaybackState.PLAYING && !isBuffering
    val repeatModeIcon = when (repeatMode) {
        RepeatMode.REPEAT_ONE -> R.drawable.repeat_one
        else -> R.drawable.repeat_all
    }
    val playbackStateIcon = when (playbackState) {
        PlaybackState.PLAYING -> R.drawable.pause_icon
        PlaybackState.PAUSED -> R.drawable.play_arrow
        PlaybackState.ERROR -> R.drawable.error
    }

    // Pager state for expanded mode artwork swiping
    val pagerState = rememberPagerState(
        initialPage = playingTrackIndex,
        pageCount = { if (queue.isEmpty()) 1 else queue.size }
    )

    var isInitialComposition by remember { mutableStateOf(true) }

    LaunchedEffect(playingTrackIndex, pagerState) {
        if (playingTrackIndex in queue.indices) {
            if (playingTrackIndex != pagerState.currentPage) {
                pagerState.animateScrollToPage(playingTrackIndex)
            }
        }

        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (isInitialComposition) {
                isInitialComposition = false
            } else {
                if (playingTrackIndex != page) {
                    onPageSelect(page)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        // Blurred background (only visible when expanded)
        if (effectiveProgress > 0f) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(effectiveProgress),
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${baseUrl}img/thumbnail/${track.image}")
                    .crossfade(true)
                    .transformations(listOf(BlurTransformation(scale = 0.25f, radius = 25)))
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(effectiveProgress)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = .75f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 1f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 1f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            // Image container with transformation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = imageTopPadding)
                    .padding(horizontal = containerPadding)
                    .then(
                        // Horizontal swipe for prev/next in collapsed state
                        if (progress.value < 0.3f) {
                            Modifier.pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (swipeDistance > 50) {
                                            onClickPrev()
                                        } else if (swipeDistance < -50) {
                                            onClickNext()
                                        }
                                        swipeDistance = 0f
                                    }
                                ) { change, dragAmount ->
                                    change.consume()
                                    swipeDistance += dragAmount
                                }
                            }
                        } else Modifier
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (progress.value < 0.5f && queueProgress < 0.1f) {
                            coroutineScope.launch {
                                bottomSheetState.bottomSheetState.expand()
                            }
                        }
                    }
            ) {
                // Unified layout: Pager + Title + Play/Pause in a Row
                // Title and icon fade out as sheet expands
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset((swipeDistance / 3).roundToInt(), 0) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pager takes only the space it needs when collapsed, expands when opened
                    val pagerWidth = lerp(
                        INITIAL_IMAGE_SIZE,
                        screenWidthDp - (containerPadding * 2),
                        effectiveProgress
                    )

                    HorizontalPager(
                        modifier = Modifier.width(pagerWidth),
                        state = pagerState,
                        pageSize = androidx.compose.foundation.pager.PageSize.Fill,
                        beyondViewportPageCount = 1,
                        userScrollEnabled = effectiveProgress > 0.5f,
                        verticalAlignment = Alignment.CenterVertically
                    ) { page ->
                        val imageData = if (page == playingTrackIndex) {
                            "${baseUrl}img/thumbnail/${queue.getOrNull(playingTrackIndex)?.image ?: track.image}"
                        } else {
                            "${baseUrl}img/thumbnail/${queue.getOrNull(page)?.image ?: track.image}"
                        }

                        val pageOffset = pagerState.calculateCurrentOffsetForPage(page)

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                modifier = Modifier
                                    .width(imageSize)
                                    .heightIn(max = imageSize)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(imageCornerRadius))
                                    .graphicsLayer {
                                        val scale = androidx.compose.ui.util.lerp(
                                            1f,
                                            1f + (0.25f * effectiveProgress),
                                            pageOffset
                                        )
                                        scaleX = scale
                                        scaleY = scale
                                        clip = true
                                        shape = RoundedCornerShape(imageCornerRadius)
                                    },
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageData)
                                    .size(coil.size.Size.ORIGINAL)
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

                    // Title and Play/Pause - fade out when expanding
                    if (miniPlayerOpacity > 0f) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .alpha(miniPlayerOpacity),
                            text = track.title,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            overflow = TextOverflow.Ellipsis,
                            color = if (swipeDistance.toInt() != 0)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = .25f)
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = .84f)
                        )

                        IconButton(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .alpha(miniPlayerOpacity),
                            onClick = {
                                if (playbackState == PlaybackState.ERROR) {
                                    onResumePlayBackFromError()
                                } else {
                                    onTogglePlayerState()
                                }
                            }
                        ) {
                            if (isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 0.75.dp,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                            Icon(
                                painter = painterResource(
                                    id = if (playbackState == PlaybackState.PLAYING)
                                        R.drawable.pause_icon else R.drawable.play_arrow
                                ),
                                contentDescription = "Play/Pause"
                            )
                        }
                    }
                }
            }

            // Progress bar for collapsed state
            AnimatedVisibility(
                visible = progress.value < 0.01f,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .alpha(miniPlayerOpacity),
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                    progress = { seekPosition },
                    strokeCap = StrokeCap.Square
                )
            }

            // Image and Content spacer
            Spacer(modifier = Modifier.height(imageContentSpacing))

            // Expanded content (fades in at 20% progress)
            AnimatedVisibility(
                visible = effectiveProgress > 0.2f,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Box(modifier = Modifier.alpha(contentOpacity)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Track title and artist
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.fillMaxWidth(.78f)) {
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
                                                    modifier = Modifier.clickable(
                                                        onClick = { onClickArtist(trackArtist.artistHash) },
                                                        indication = null,
                                                        interactionSource = remember { MutableInteractionSource() }
                                                    ),
                                                    text = trackArtist.name,
                                                    maxLines = 1,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = .84f
                                                    ),
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (index != track.trackArtists.lastIndex) {
                                                    Text(
                                                        text = ", ",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                                            alpha = .84f
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                IconButton(
                                    modifier = Modifier.clip(CircleShape),
                                    onClick = {
                                        onToggleFavorite(
                                            track.isFavorite,
                                            track.trackHash
                                        )
                                    }
                                ) {
                                    val icon = if (track.isFavorite) R.drawable.fav_filled
                                    else R.drawable.fav_not_filled
                                    Icon(
                                        painter = painterResource(id = icon),
                                        contentDescription = "Favorite"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Seek bar
                            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                WavySlider(
                                    modifier = Modifier.height(12.dp),
                                    value = seekPosition,
                                    onValueChangeFinished = {},
                                    onValueChange = { value -> onSeekPlayBack(value) },
                                    waveLength = 32.dp,
                                    waveHeight = if (animateWave) 8.dp else 0.dp,
                                    waveVelocity = 16.dp to WaveDirection.HEAD,
                                    waveThickness = 4.dp,
                                    trackThickness = 4.dp,
                                    incremental = false,
                                    animationSpecs = WaveAnimationSpecs(
                                        waveHeightAnimationSpec = tween(
                                            durationMillis = 300,
                                            easing = FastOutSlowInEasing
                                        ),
                                        waveVelocityAnimationSpec = tween(
                                            durationMillis = 300,
                                            easing = LinearOutSlowInEasing
                                        ),
                                        waveAppearanceAnimationSpec = tween(
                                            durationMillis = 300,
                                            easing = EaseOutQuad
                                        )
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = playbackDuration,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84f)
                                    )
                                    Text(
                                        text = if (playbackState == PlaybackState.ERROR)
                                            track.duration.formatDuration() else trackDuration,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Playback controls
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
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5f)
                                        ),
                                    onClick = { onClickPrev() }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.prev),
                                        contentDescription = "Previous"
                                    )
                                }

                                Box(
                                    modifier = Modifier.clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = {
                                            if (playbackState != PlaybackState.ERROR) {
                                                onTogglePlayerState()
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
                                                    MaterialTheme.colorScheme.onErrorContainer.copy(
                                                        alpha = .25f
                                                    )
                                                else
                                                    MaterialTheme.colorScheme.onErrorContainer.copy(
                                                        alpha = .75f
                                                    ),
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
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5f)
                                        ),
                                    onClick = { onClickNext() }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.next),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        contentDescription = "Next"
                                    )
                                }
                            }
                        }

                        // Queue drag zone - contains bitrate badge
                        var lastDragOffset by remember { mutableFloatStateOf(queueSheetOffset.value) }
                        var isDraggingUp by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { lastDragOffset = queueSheetOffset.value },
                                        onDragEnd = {
                                            coroutineScope.launch {
                                                val queueSheetProgress =
                                                    (queueInitialOffset - queueSheetOffset.value) /
                                                            (queueInitialOffset - queueExpandedOffset)

                                                val threshold = if (isDraggingUp) 0.12f else 0.88f

                                                val targetOffset =
                                                    if (queueSheetProgress > threshold) {
                                                        queueExpandedOffset
                                                    } else {
                                                        queueInitialOffset
                                                    }

                                                queueSheetOffset.animateTo(
                                                    targetValue = targetOffset,
                                                    animationSpec = spring(
                                                        dampingRatio = 0.8f,
                                                        stiffness = 400f
                                                    )
                                                )
                                            }
                                        }
                                    ) { _, dragAmount ->
                                        coroutineScope.launch {
                                            // Multiplier speeds up drag response (2.5x faster)
                                            val newOffset =
                                                (queueSheetOffset.value + (dragAmount.y * 2.5f))
                                                    .coerceIn(
                                                        queueExpandedOffset,
                                                        queueInitialOffset
                                                    )

                                            isDraggingUp = newOffset < lastDragOffset
                                            lastDragOffset = newOffset

                                            queueSheetOffset.snapTo(newOffset)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Bitrate badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24))
                                    .background(
                                        if (isDarkTheme) fileTypeTextColor.copy(alpha = .075f)
                                        else fileTypeBadgeColor
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
                        }

                        // Navigation and Control Icons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .background(MaterialTheme.colorScheme.inverseOnSurface)
                                .navigationBarsPadding()
                                .padding(vertical = 12.dp, horizontal = 32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = { onToggleRepeatMode() }) {
                                Icon(
                                    painter = painterResource(id = repeatModeIcon),
                                    tint = if (repeatMode == RepeatMode.REPEAT_OFF)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .3f)
                                    else MaterialTheme.colorScheme.onSurface,
                                    contentDescription = "Repeat"
                                )
                            }

                            // Queue icon - triggers queue sheet
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    queueSheetOffset.animateTo(
                                        targetValue = queueExpandedOffset,
                                        animationSpec = spring(
                                            dampingRatio = 0.8f,
                                            stiffness = 400f
                                        )
                                    )
                                }
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.play_list),
                                    contentDescription = "Queue"
                                )
                            }

                            IconButton(onClick = { onToggleShuffleMode() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.shuffle),
                                    tint = if (shuffleMode == ShuffleMode.SHUFFLE_OFF)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .3f)
                                    else MaterialTheme.colorScheme.onSurface,
                                    contentDescription = "Shuffle"
                                )
                            }
                        }
                    }
                }
            }

        }
    } // End of Box wrapper
}

/**
 * Queue Sheet Overlay - Secondary sheet that appears when primary player is fully expanded.
 *
 * Behavior:
 * - Draggable from bottom with spring animations
 * - Direction-aware snapping (20% threshold up, 90% down)
 * - Opacity based on drag progress
 */
@Composable
private fun QueueSheetOverlay(
    queue: List<Track>,
    source: QueueSource,
    playingTrackIndex: Int,
    playingTrack: Track,
    playbackState: PlaybackState,
    baseUrl: String,
    animatedOffset: Animatable<Float, AnimationVector1D>,
    initialOffset: Float,
    expandedOffset: Float,
    onClickQueueItem: (Int) -> Unit,
    onTogglePlayerState: () -> Unit
) {
    val configuration = LocalWindowInfo.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val screenHeightPx = with(density) { configuration.containerSize.height.dp.toPx() }
    val lazyColumnState = rememberLazyListState()

    // Track drag direction
    var lastOffset by remember { mutableFloatStateOf(animatedOffset.value) }
    var isDraggingUp by remember { mutableStateOf(false) }

    // Calculate drag progress
    val queueProgress by remember {
        derivedStateOf {
            val progress = (initialOffset - animatedOffset.value) / (initialOffset - expandedOffset)
            progress.coerceIn(0f, 1f)
        }
    }

    // Opacity based on drag progress - reaches 1.0 at 50% drag
    val queueOpacity by remember {
        derivedStateOf {
            (queueProgress / 0.5f).coerceIn(0f, 1f)
        }
    }

    // Scroll to playing track when sheet opens
    LaunchedEffect(queueProgress) {
        if (queueProgress > 0.9f && (playingTrackIndex - 1) in queue.indices) {
            lazyColumnState.scrollToItem(playingTrackIndex - 1)
        }
    }

    // Main Queue Sheet Container - uses direct offset like primary sheet
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(queueOpacity)
            .offset { IntOffset(0, animatedOffset.value.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { lastOffset = animatedOffset.value },
                    onDragEnd = {
                        coroutineScope.launch {
                            val threshold = if (isDraggingUp) 0.20f else 0.90f
                            val targetOffset = if (queueProgress > threshold) {
                                expandedOffset
                            } else {
                                initialOffset
                            }

                            animatedOffset.animateTo(
                                targetValue = targetOffset,
                                animationSpec = spring(
                                    dampingRatio = 0.8f,
                                    stiffness = 400f
                                )
                            )
                        }
                    },
                    onDrag = { _, dragAmount ->
                        coroutineScope.launch {
                            val newOffset = (animatedOffset.value + dragAmount.y)
                                .coerceIn(expandedOffset, initialOffset)

                            isDraggingUp = newOffset < lastOffset
                            lastOffset = newOffset

                            animatedOffset.snapTo(newOffset)
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Queue",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Currently playing track card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .14f))
                    .clickable { onTogglePlayerState() }
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("${baseUrl}img/thumbnail/${playingTrack.image}")
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.audio_fallback),
                            fallback = painterResource(R.drawable.audio_fallback),
                            error = painterResource(R.drawable.audio_fallback),
                            contentDescription = "Track Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )

                        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                            Text(
                                text = playingTrack.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = playingTrack.trackArtists.joinToString(", ") { it.name },
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .80f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Sound Bars
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .padding(end = 8.dp)
                    ) {
                        SoundSignalBars(animate = playbackState == PlaybackState.PLAYING)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Queue items
            LazyColumn(
                state = lazyColumnState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(
                    items = queue,
                    key = { index, track -> "$index:${track.filepath}" }
                ) { index, track ->
                    TrackItem(
                        track = track,
                        playbackState = playbackState,
                        isCurrentTrack = index == playingTrackIndex,
                        baseUrl = baseUrl,
                        showMenuIcon = false,
                        onClickTrackItem = { onClickQueueItem(index) },
                        onClickMoreVert = {}
                    )

                    if (index == queue.lastIndex) {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
