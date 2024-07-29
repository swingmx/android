@file:Suppress("UnusedReceiverParameter")

package com.android.swingmusic.uicomponent.presentation.component.slider


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderPositions
import androidx.compose.material3.SliderState
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastFirst
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

/*
 * Instead of directly exposing the following defaults as public properties,
 * we want to provide them in the SliderDefaults object so the user can access all the defaults
 * using that namespace. But SliderDefaults object is in Material library, and we cannot modify it.
 * So, we provide the defaults as extension properties of SliderDefaults object.
 */

/**
 * Default wave length
 */
val SliderDefaults.WaveLength: Dp get() = defaultWaveLength

/**
 * Default wave height
 */
val SliderDefaults.WaveHeight: Dp get() = defaultWaveHeight

/**
 * Default wave velocity (speed and direction)
 */
val SliderDefaults.WaveVelocity: WaveVelocity get() = defaultWaveVelocity

/**
 * Default wave thickness
 */
val SliderDefaults.WaveThickness: Dp get() = defaultTrackThickness

/**
 * Default track thickness
 */
val SliderDefaults.TrackThickness: Dp get() = defaultTrackThickness

/**
 * Default progression of wave height (whether gradual or not)
 */
val SliderDefaults.Incremental: Boolean get() = defaultIncremental

/**
 * Default animation configurations for various properties of the wave
 */
val SliderDefaults.WaveAnimationSpecs: WaveAnimationSpecs get() = defaultWaveAnimationSpecs

private val ThumbWidth = SliderTokens.HandleWidth
private val ThumbHeight = SliderTokens.HandleHeight
private val ThumbSize = DpSize(ThumbWidth, ThumbHeight)

@Composable
@ExperimentalMaterial3Api
fun SliderDefaults.Track(
    sliderState: SliderState,
    modifier: Modifier = Modifier,
    colors: SliderColors = colors(),
    enabled: Boolean = true,
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    waveVelocity: WaveVelocity = SliderDefaults.WaveVelocity,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs
) {
    // @Suppress("INVISIBLE_MEMBER") is required to be able to access and use
    // trackColor() function which is marked internal in Material library
    // See https://stackoverflow.com/q/62500464/8583692
    val inactiveTrackColor =
        @Suppress("INVISIBLE_MEMBER") colors.trackColor(enabled, active = false)
    val activeTrackColor = @Suppress("INVISIBLE_MEMBER") colors.trackColor(enabled, active = true)
    val waveSpreadAnimated by animateWaveSpread(animationSpecs.waveStartSpreadAnimationSpec)
    val waveHeightAnimated by animateWaveHeight(waveHeight, animationSpecs.waveHeightAnimationSpec)
    val waveShiftAnimated by animateWaveShift(
        waveVelocity,
        animationSpecs.waveVelocityAnimationSpec
    )
    val trackHeight = max(waveThickness + waveHeight.value.absoluteValue.dp, ThumbSize.height)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
    ) {
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(0f, center.y)
        val sliderRight = Offset(size.width, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight
        val sliderValueFraction = @Suppress("INVISIBLE_MEMBER") sliderState.coercedValueAsFraction
        val sliderValueOffset =
            Offset(sliderStart.x + (sliderEnd.x - sliderStart.x) * sliderValueFraction, center.y)
        drawTrack(
            waveLength = waveLength,
            waveHeight = waveHeightAnimated,
            waveSpread = waveSpreadAnimated,
            waveShift = waveShiftAnimated,
            waveThickness = waveThickness,
            trackThickness = trackThickness,
            sliderValueOffset = sliderValueOffset,
            sliderStart = sliderStart,
            sliderEnd = sliderEnd,
            incremental = incremental,
            inactiveTrackColor = inactiveTrackColor,
            activeTrackColor = activeTrackColor
        )
    }
}

@Suppress("DEPRECATION")
@Composable
@Deprecated("Use the variant that supports SliderState")
fun SliderDefaults.Track(
    sliderPositions: SliderPositions,
    modifier: Modifier = Modifier,
    colors: SliderColors = colors(),
    enabled: Boolean = true,
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    waveVelocity: WaveVelocity = SliderDefaults.WaveVelocity,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs
) {
    // @Suppress("INVISIBLE_MEMBER") is required to be able to access and use
    // trackColor() function which is marked internal in Material library
    // See https://stackoverflow.com/q/62500464/8583692
    val inactiveTrackColor =
        @Suppress("INVISIBLE_MEMBER") colors.trackColor(enabled, active = false)
    val activeTrackColor = @Suppress("INVISIBLE_MEMBER") colors.trackColor(enabled, active = true)
    val waveSpreadAnimated by animateWaveSpread(animationSpecs.waveStartSpreadAnimationSpec)
    val waveHeightAnimated by animateWaveHeight(waveHeight, animationSpecs.waveHeightAnimationSpec)
    val waveShiftAnimated by animateWaveShift(
        waveVelocity,
        animationSpecs.waveVelocityAnimationSpec
    )
    val trackHeight = max(waveThickness + waveHeight.value.absoluteValue.dp, ThumbSize.height)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
    ) {
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(0f, center.y)
        val sliderRight = Offset(size.width, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight
        val sliderValueOffset = Offset(
            sliderStart.x + (sliderEnd.x - sliderStart.x) * sliderPositions.activeRange.endInclusive,
            center.y
        )
        drawTrack(
            waveLength = waveLength,
            waveHeight = waveHeightAnimated,
            waveSpread = waveSpreadAnimated,
            waveShift = waveShiftAnimated,
            waveThickness = waveThickness,
            trackThickness = trackThickness,
            sliderValueOffset = sliderValueOffset,
            sliderStart = sliderStart,
            sliderEnd = sliderEnd,
            incremental = incremental,
            inactiveTrackColor = inactiveTrackColor,
            activeTrackColor = activeTrackColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    waveVelocity: WaveVelocity = SliderDefaults.WaveVelocity,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs
) {
    WavySlider_Internal(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        track = { sliderState ->
            SliderDefaults.Track(
                colors = colors,
                enabled = enabled,
                sliderState = sliderState,
                waveLength = waveLength,
                waveHeight = waveHeight,
                waveVelocity = waveVelocity,
                waveThickness = waveThickness,
                trackThickness = trackThickness,
                incremental = incremental,
                animationSpecs = animationSpecs
            )
        },
        valueRange = valueRange
    )
}

@Composable
@ExperimentalMaterial3Api
private fun WavySlider_Internal(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    waveVelocity: WaveVelocity = SliderDefaults.WaveVelocity,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs,
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(
            colors = colors,
            enabled = enabled,
            sliderState = sliderState,
            waveLength = waveLength,
            waveHeight = waveHeight,
            waveVelocity = waveVelocity,
            waveThickness = waveThickness,
            trackThickness = trackThickness,
            incremental = incremental,
            animationSpecs = animationSpecs
        )
    }
) {
    val state = remember(valueRange, onValueChangeFinished) {
        SliderState(value, 0, onValueChangeFinished, valueRange)
    }

    @Suppress("INVISIBLE_MEMBER")
    state.onValueChange = onValueChange
    state.value = value
    WavySlider_internal_2(
        state = state,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        track = track,
        waveLength = waveLength,
        waveHeight = waveHeight,
        waveVelocity = waveVelocity,
        waveThickness = waveThickness,
        trackThickness = trackThickness,
        incremental = incremental,
        animationSpecs = animationSpecs,
    )
}

@Composable
@ExperimentalMaterial3Api
private fun WavySlider_internal_2(
    state: SliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    waveVelocity: WaveVelocity = SliderDefaults.WaveVelocity,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs,
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(
            colors = colors,
            enabled = enabled,
            sliderState = sliderState,
            waveLength = waveLength,
            waveHeight = waveHeight,
            waveVelocity = waveVelocity,
            waveThickness = waveThickness,
            trackThickness = trackThickness,
            incremental = incremental,
            animationSpecs = animationSpecs
        )
    }
) {
    WavySliderImpl(
        state = state,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        track = track
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WavySliderImpl(
    state: SliderState,
    enabled: Boolean,
    modifier: Modifier,
    interactionSource: MutableInteractionSource,
    track: @Composable (SliderState) -> Unit
) {
    @Suppress("INVISIBLE_MEMBER")
    state.isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val press = Modifier.sliderTapModifier(
        state,
        interactionSource,
        enabled
    )
    val drag = Modifier.draggable(
        orientation = Orientation.Horizontal,
        reverseDirection = @Suppress("INVISIBLE_MEMBER") state.isRtl,
        enabled = enabled,
        interactionSource = interactionSource,
        onDragStopped = { @Suppress("INVISIBLE_MEMBER") state.gestureEndAction.invoke() },
        startDragImmediately = @Suppress("INVISIBLE_MEMBER") state.isDragging,
        state = state
    )

    Layout(
        content = {
            Box(modifier = Modifier.layoutId(SliderComponents.THUMB)) {
                val colors: SliderColors = SliderDefaults.colors()

                @Suppress("INVISIBLE_MEMBER")
                Box(
                    modifier = Modifier
                        .padding(start = 7.dp)
                        .height(32.dp)
                        .width(5.dp)
                        .clip(RoundedCornerShape(100))
                        .background(colors.thumbColor(enabled))
                )
            }
            Box(modifier = Modifier.layoutId(SliderComponents.TRACK)) { track(state) }
        },
        modifier = modifier
            .minimumInteractiveComponentSize()
            .requiredSizeIn(
                minWidth = SliderTokens.HandleWidth,
                minHeight = SliderTokens.HandleHeight
            )
            .sliderSemantics(state, enabled)
            .focusable(enabled, interactionSource)
            .then(press)
            .then(drag)
    ) { measurables, constraints ->

        val thumbPlaceable =
            measurables.fastFirst { it.layoutId == SliderComponents.THUMB }.measure(constraints)
        val trackPlaceable =
            measurables.fastFirst { it.layoutId == SliderComponents.TRACK }.measure(
                constraints.offset(horizontal = -thumbPlaceable.width).copy(minHeight = 0)
            )

        val sliderWidth = thumbPlaceable.width + trackPlaceable.width
        val sliderHeight = max(trackPlaceable.height, thumbPlaceable.height)

        @Suppress("INVISIBLE_MEMBER")
        state.updateDimensions(
            thumbPlaceable.width.toFloat(),
            sliderWidth
        )

        val trackOffsetX = thumbPlaceable.width / 2
        val thumbOffsetX =
            ((trackPlaceable.width) * @Suppress("INVISIBLE_MEMBER") state.coercedValueAsFraction).roundToInt()
        val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
        val thumbOffsetY = (sliderHeight - thumbPlaceable.height) / 2

        layout(sliderWidth, sliderHeight) {
            trackPlaceable.placeRelative(
                trackOffsetX,
                trackOffsetY
            )
            thumbPlaceable.placeRelative(
                thumbOffsetX,
                thumbOffsetY
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
// No need to name it wavySliderSemantics
private fun Modifier.sliderSemantics(
    state: SliderState,
    enabled: Boolean
): Modifier {
    return semantics {
        if (!enabled) disabled()
        setProgress(
            action = { targetValue ->
                var newValue = targetValue.coerceIn(
                    state.valueRange.start,
                    state.valueRange.endInclusive
                )
                val originalVal = newValue
                val resolvedValue = if (state.steps > 0) {
                    var distance: Float = newValue
                    for (i in 0..state.steps + 1) {
                        val stepValue = androidx.compose.ui.util.lerp(
                            state.valueRange.start,
                            state.valueRange.endInclusive,
                            i.toFloat() / (state.steps + 1)
                        )
                        if (abs(stepValue - originalVal) <= distance) {
                            distance = abs(stepValue - originalVal)
                            newValue = stepValue
                        }
                    }
                    newValue
                } else {
                    newValue
                }

                // This is to keep it consistent with AbsSeekbar.java: return false if no
                // change from current.
                if (resolvedValue == state.value) {
                    false
                } else {
                    if (resolvedValue != state.value) {
                        if (@Suppress("INVISIBLE_MEMBER") state.onValueChange != null) {
                            @Suppress("INVISIBLE_MEMBER") state.onValueChange?.let {
                                it(resolvedValue)
                            }
                        } else {
                            state.value = resolvedValue
                        }
                    }
                    state.onValueChangeFinished?.invoke()
                    true
                }
            }
        )
    }.progressSemantics(
        state.value,
        state.valueRange.start..state.valueRange.endInclusive,
        state.steps
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Stable
// No need to name it wavySliderTapModifier
private fun Modifier.sliderTapModifier(
    state: SliderState,
    interactionSource: MutableInteractionSource,
    enabled: Boolean
) = if (enabled) {
    pointerInput(state, interactionSource) {
        detectTapGestures(
            onPress = { @Suppress("INVISIBLE_MEMBER") state.onPress(it) },
            onTap = {
                state.dispatchRawDelta(0f)
                @Suppress("INVISIBLE_MEMBER")
                state.gestureEndAction.invoke()
            }
        )
    }
} else {
    this
}

// No need to name it WavySliderComponents
private enum class SliderComponents {
    THUMB,
    TRACK
}

// No need to name it WavySliderTokens
internal object SliderTokens {
    val HandleHeight = 20.0.dp
    val HandleWidth = 20.0.dp
}
