package com.android.swingmusic.uicomponent.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SoundSignalBars(animate: Boolean) {
    val initialHeights = listOf(0.1F, 0.45F, 0.75F)
    val barStates = remember {
        initialHeights.map { mutableFloatStateOf(it) }
    }

    LaunchedEffect(Unit) {
        if (animate) {
            while (true) {
                barStates.forEach { barState ->
                    barState.floatValue = Random.nextFloat()
                }
                delay(300)
            }
        }
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .fillMaxSize()
            .background(Color.Transparent),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        barStates.forEach { barState ->
            Bar(heightFraction = barState.floatValue, animate = animate)
        }
    }
}

@Composable
private fun Bar(heightFraction: Float, animate: Boolean) {
    val animatedHeightFraction by animateFloatAsState(
        targetValue = heightFraction,
        animationSpec = tween(durationMillis = 500),
        label = "Anim Bar Height"
    )

    val height = if (animate) animatedHeightFraction else heightFraction
    val barColor = MaterialTheme.colorScheme.onSurface

    Canvas(
        modifier = Modifier
            .height(32.dp)
            .width(8.dp)
    ) {
        drawRect(
            color = barColor,
            topLeft = Offset(0f, size.height * (1 - height)),
            size = size.copy(height = size.height * height)
        )
    }
}
