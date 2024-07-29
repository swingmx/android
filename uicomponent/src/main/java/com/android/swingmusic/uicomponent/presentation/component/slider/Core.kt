package com.android.swingmusic.uicomponent.presentation.component.slider

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.sin

/**
 * The horizontal movement (shift) of the whole wave.
 */
enum class WaveDirection(internal inline val factor: (LayoutDirection) -> Float) {
    /**
     * Always shift toward left (regardless of layout direction).
     */
    LEFT({ 1f }),

    /**
     * Always shift toward right (regardless of layout direction).
     */
    RIGHT({ -1f }),

    /**
     * Shift toward the start (depends on layout direction).
     */
    TAIL({ if (it == LayoutDirection.Ltr) 1f else -1f }),

    /**
     * Shift toward the thumb (depends on layout direction).
     */
    HEAD({ if (it == LayoutDirection.Ltr) -1f else 1f })
}

/**
 * Custom animation configurations for various properties of the wave.
 *
 * @param waveHeightAnimationSpec used for **changes** in wave height.
 * @param waveVelocityAnimationSpec used for **changes** in wave velocity (whether in speed or direction).
 * @param waveStartSpreadAnimationSpec Used for wave expansion at the start of the composition.
 */

data class WaveAnimationSpecs(
    /**
     * Used for **changes** in wave height.
     */
    val waveHeightAnimationSpec: AnimationSpec<Dp>,
    /**
     * Used for **changes** in wave velocity (whether in speed or direction).
     */
    val waveVelocityAnimationSpec: AnimationSpec<Dp>,
    /**
     * Used for wave expansion at the start of the composition.
     */
    val waveStartSpreadAnimationSpec: AnimationSpec<Float>,
)

typealias WaveVelocity = Pair<Dp, WaveDirection>

internal const val defaultIncremental = false
internal val defaultTrackThickness = 4.dp
internal val defaultWaveLength = 20.dp
internal val defaultWaveHeight = 6.dp
internal val defaultWaveVelocity = 10.dp to WaveDirection.TAIL
internal val defaultWaveAnimationSpecs = WaveAnimationSpecs(
    waveHeightAnimationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
    waveVelocityAnimationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
    waveStartSpreadAnimationSpec = tween(durationMillis = 6000, easing = EaseOutQuad)
)

@Composable
internal fun animateWaveShift(
    waveVelocity: WaveVelocity,
    animationSpec: AnimationSpec<Dp>
): State<Dp> {
    val shift = remember { mutableStateOf(0.dp) }
    val speed = waveVelocity.first.coerceAtLeast(0.dp)
    val factor = waveVelocity.second.factor(LocalLayoutDirection.current)
    val amount by animateDpAsState(speed * factor, animationSpec, label = "")
    LaunchedEffect(waveVelocity, LocalLayoutDirection.current) {
        val startShift = shift.value
        val startTime = withFrameNanos { it }
        while (true /* Android itself uses true instead of isActive */) {
            val playTime = (withFrameNanos { it } - startTime) / 1_000_000_000f
            shift.value = startShift + (amount * playTime)
        }
    }
    return shift
}

@Composable
internal fun animateWaveHeight(
    waveHeight: Dp,
    animationSpec: AnimationSpec<Dp>
): State<Dp> = animateDpAsState(
    targetValue = waveHeight,
    animationSpec = animationSpec, label = ""
)

@Composable
internal fun animateWaveSpread(
    animationSpec: AnimationSpec<Float>
): State<Float> {
    var spreadFactor by remember { mutableFloatStateOf(0f) }
    val spreadFactorAnimated = animateFloatAsState(
        targetValue = spreadFactor,
        animationSpec = animationSpec, label = ""
    )
    LaunchedEffect(Unit) { spreadFactor = 1f }
    return spreadFactorAnimated
}

internal fun DrawScope.drawTrack(
    sliderStart: Offset,
    sliderValueOffset: Offset,
    sliderEnd: Offset,
    waveLength: Dp,
    waveHeight: Dp,
    waveSpread: Float,
    waveShift: Dp,
    waveThickness: Dp,
    trackThickness: Dp,
    incremental: Boolean,
    inactiveTrackColor: Color,
    activeTrackColor: Color
) {
    drawTrackActivePart(
        startOffset = sliderStart,
        valueOffset = sliderValueOffset,
        waveLength = waveLength,
        waveHeight = waveHeight,
        waveSpread = waveSpread,
        waveShift = waveShift,
        waveThickness = waveThickness,
        incremental = incremental,
        color = activeTrackColor
    )
    drawTrackInactivePart(
        color = inactiveTrackColor,
        thickness = trackThickness,
        startOffset = sliderValueOffset,
        endOffset = sliderEnd,
    )
}

private fun DrawScope.drawTrackInactivePart(
    color: Color,
    thickness: Dp,
    startOffset: Offset,
    endOffset: Offset
) {
    if (thickness <= 0.dp) return
    drawLine(
        strokeWidth = thickness.toPx(),
        color = color,
        start = startOffset,
        end = endOffset,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawTrackActivePart(
    startOffset: Offset,
    valueOffset: Offset,
    waveLength: Dp,
    waveHeight: Dp,
    waveSpread: Float,
    waveShift: Dp,
    waveThickness: Dp,
    incremental: Boolean,
    color: Color
) {
    if (waveThickness <= 0.dp) return
    val path = if (waveLength <= 0.dp || waveHeight == 0.dp) {
        createFlatPath(
            startOffset,
            valueOffset
        )
    } else {
        createWavyPath(
            startOffset,
            valueOffset,
            waveLength,
            waveHeight,
            waveSpread,
            waveShift,
            incremental
        )
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = waveThickness.toPx(),
            join = StrokeJoin.Round,
            cap = StrokeCap.Round
        )
    )
}

private fun DrawScope.createFlatPath(
    startOffset: Offset,
    valueOffset: Offset
): Path = Path().apply {
    moveTo(startOffset.x, center.y)
    lineTo(valueOffset.x, center.y)
}

private fun DrawScope.createWavyPath(
    startOffset: Offset,
    valueOffset: Offset,
    waveLength: Dp,
    waveHeight: Dp,
    waveSpread: Float,
    waveShift: Dp,
    incremental: Boolean
): Path = Path().apply {
    val waveShiftPx = waveShift.toPx()
    val waveLengthPx = waveLength.toPx()
    val waveHeightPx = waveHeight.toPx().absoluteValue
    val startRadians = waveSpread * (waveShiftPx) / waveLengthPx * (2 * PI)
    val startHeightFactor = if (incremental) 0f else 1f
    val startY = (sin(startRadians) * startHeightFactor * waveHeightPx + size.height) / 2
    moveTo(startOffset.x, startY.toFloat())
    val range = if (layoutDirection == LayoutDirection.Rtl) {
        startOffset.x.toInt() downTo valueOffset.x.toInt()
    } else {
        startOffset.x.toInt()..valueOffset.x.toInt()
    }
    for (x in range) {
        val heightFactor =
            if (incremental) (x - range.first).toFloat() / (range.last - range.first) else 1f
        val radians = waveSpread * (x - range.first + waveShiftPx) / waveLengthPx * (2 * PI)
        val y = (sin(radians) * heightFactor * waveHeightPx + size.height) / 2
        lineTo(x.toFloat(), y.toFloat())
    }
}
