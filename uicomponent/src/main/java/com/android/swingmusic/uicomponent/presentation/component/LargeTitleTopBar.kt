package com.android.swingmusic.uicomponent.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File

private val LargeTitleBarHeight = 64.dp
private val LargeTitleBarFade = 68.dp

/** Scroll-driven values shared by [LargeTitle] and [LargeTitleTopBar]. */
@Stable
class LargeTitleState internal constructor(
    collapse: State<Float>,
    handover: State<Float>,
    scrim: State<Float>,
) {
    /** 0 while the large title is fully visible, 1 once it has scrolled under the bar. */
    val titleCollapse by collapse

    /** 0 shows the large title, 1 the small one in the bar. */
    val titleHandover by handover

    /** The bar stays clear at the top of the page and darkens once content scrolls under it. */
    val scrimAlpha by scrim
}

/** Expects the [LargeTitle] to be the list's first item. */
@Composable
fun rememberLargeTitleState(listState: LazyListState): LargeTitleState {
    val collapse = remember(listState) {
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
    val showSmallTitle = remember(listState) { derivedStateOf { collapse.value >= 0.95F } }
    val handover = animateFloatAsState(
        targetValue = if (showSmallTitle.value) 1F else 0F,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "largeTitleHandover"
    )

    val scrimFadeDistance = with(LocalDensity.current) { 24.dp.toPx() }
    val scrim = remember(listState, scrimFadeDistance) {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1F
            else (listState.firstVisibleItemScrollOffset / scrimFadeDistance).coerceIn(0F, 1F)
        }
    }

    return remember(listState) { LargeTitleState(collapse, handover, scrim) }
}

/** Top padding for list content, so it starts below the bar but scrolls up behind it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun largeTitleTopBarHeight(): Dp =
    TopAppBarDefaults.windowInsets.asPaddingValues().calculateTopPadding() + LargeTitleBarHeight

/** The page's large title, placed as the first list item. */
@Composable
fun LargeTitle(text: String, state: LargeTitleState, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .graphicsLayer {
                // Fade with the scroll, then drift up into the bar while fading out
                alpha = (1F - state.titleCollapse * 0.8F) * (1F - state.titleHandover)
                val scale = 1F - 0.04F * state.titleHandover
                scaleX = scale
                scaleY = scale
                translationY = -8.dp.toPx() * state.titleHandover
                transformOrigin = TransformOrigin(0F, 0.5F)
            }
    )
}

/** Overlay bar for tab roots: small title that takes over from [LargeTitle], and the profile photo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeTitleTopBar(
    title: String,
    state: LargeTitleState,
    avatarPath: String?,
    onProfileClick: () -> Unit,
) {
    val surface = MaterialTheme.colorScheme.surface
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(brush = topBarScrimBrush(surface), alpha = state.scrimAlpha)
            }
            .windowInsetsPadding(TopAppBarDefaults.windowInsets)
            .padding(bottom = LargeTitleBarFade)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(LargeTitleBarHeight)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1F)
                    .graphicsLayer {
                        // Rise into place while fading in
                        val progress = state.titleHandover
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
                    if (avatarPath != null) {
                        AsyncImage(
                            model = File(avatarPath),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
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
}
