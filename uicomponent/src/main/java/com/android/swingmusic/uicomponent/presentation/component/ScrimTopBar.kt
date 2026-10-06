package com.android.swingmusic.uicomponent.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private val ScrimTopBarHeight = 64.dp
private val ScrimTopBarFade = 68.dp

/**
 * One continuous fade from opaque at the top of the screen to transparent below the bar.
 * Follows 1 - t², which leaves the top flat (no visible start) and keeps the bar area dark.
 */
fun topBarScrimBrush(color: Color): Brush {
    val steps = 32
    val stops = Array(steps + 1) { i ->
        val t = i / steps.toFloat()
        t to color.copy(alpha = 1F - t * t)
    }
    return Brush.verticalGradient(*stops)
}

/**
 * Overlay bar for detail screens: a back button over the same gradient as Home's top bar.
 * The gradient stays clear at the top of the page and fades in once content scrolls under it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrimTopBar(
    listState: LazyListState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrimFadeDistance = with(LocalDensity.current) { 24.dp.toPx() }
    val scrimAlpha by remember(listState) {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1F
            else (listState.firstVisibleItemScrollOffset / scrimFadeDistance).coerceIn(0F, 1F)
        }
    }
    val surface = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind { drawRect(brush = topBarScrimBrush(surface), alpha = scrimAlpha) }
            .windowInsetsPadding(TopAppBarDefaults.windowInsets)
            .padding(bottom = ScrimTopBarFade)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ScrimTopBarHeight)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    }
}
