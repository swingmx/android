package com.android.swingmusic.profile.presentation.screen

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RotateLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.profile.presentation.event.AvatarCropUiEffect
import com.android.swingmusic.profile.presentation.event.AvatarCropUiEvent
import com.android.swingmusic.profile.presentation.state.AvatarCropUiState
import com.android.swingmusic.profile.presentation.viewmodel.AvatarCropViewModel
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.DestinationStyle
import kotlin.math.max
import kotlin.math.roundToInt

private const val MAX_ZOOM = 5F
private val CircleMargin = 36.dp

/** Full-screen dialog, so the crop editor covers the nav bar and mini player. */
internal object FullScreenDialogStyle : DestinationStyle.Dialog {
    override val properties = DialogProperties(
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = false
    )
}

@Destination(style = FullScreenDialogStyle::class)
@Composable
internal fun AvatarCropScreen(
    navigator: CommonNavigator,
    @Suppress("UNUSED_PARAMETER") imageUri: String,
    viewModel: AvatarCropViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            AvatarCropUiEffect.NavigateBack -> navigator.navigateBack()
        }
    }

    AvatarCropScreenContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun AvatarCropScreenContent(
    uiState: AvatarCropUiState,
    onEvent: (AvatarCropUiEvent) -> Unit
) {
    val bitmap = uiState.bitmap
    val marginPx = with(LocalDensity.current) { CircleMargin.toPx() }
    val frame = remember(bitmap) { bitmap?.let { CropFrame(it, marginPx) } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            // A fresh node per photo, so a rotated photo gets measured again.
            frame != null -> key(frame) { CropArea(frame = frame, modifier = Modifier.fillMaxSize()) }

            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onEvent(AvatarCropUiEvent.OnCancel) }) {
                Icon(Icons.Rounded.Close, contentDescription = "Cancel", tint = Color.White)
            }
            Text(
                text = "Move and Scale",
                modifier = Modifier.weight(1F),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            IconButton(onClick = { onEvent(AvatarCropUiEvent.OnRotate) }, enabled = bitmap != null) {
                Icon(Icons.Rounded.RotateLeft, contentDescription = "Rotate", tint = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.error ?: "Pinch to zoom · drag to move",
                color = if (uiState.error != null) MaterialTheme.colorScheme.error else Color.White.copy(alpha = .75F),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onEvent(AvatarCropUiEvent.OnCancel) },
                    modifier = Modifier.weight(1F),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = .3F)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Cancel") }
                Button(
                    onClick = { frame?.cropRect()?.let { onEvent(AvatarCropUiEvent.OnSave(it)) } },
                    enabled = bitmap != null && !uiState.isSaving,
                    modifier = Modifier.weight(1F)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save")
                    }
                }
            }
        }
    }
}

/**
 * How the photo sits under the circle. The photo always covers the circle: zoom starts at
 * "fill the circle" and panning stops at the photo's edges.
 */
private class CropFrame(val bitmap: Bitmap, private val marginPx: Float) {
    var containerSize by mutableStateOf(IntSize.Zero)
    var scale by mutableFloatStateOf(1F)
    var offset by mutableStateOf(Offset.Zero)

    val diameter: Float get() = (containerSize.width - 2 * marginPx).coerceAtLeast(1F)
    private val baseScale: Float get() = max(diameter / bitmap.width, diameter / bitmap.height)
    val totalScale: Float get() = baseScale * scale

    fun transform(pan: Offset, zoom: Float) {
        scale = (scale * zoom).coerceIn(1F, MAX_ZOOM)
        val maxX = (bitmap.width * totalScale - diameter) / 2F
        val maxY = (bitmap.height * totalScale - diameter) / 2F
        val moved = offset + pan
        offset = Offset(moved.x.coerceIn(-maxX, maxX), moved.y.coerceIn(-maxY, maxY))
    }

    /** What is inside the circle, in bitmap pixels. */
    fun cropRect(): Rect? {
        if (containerSize == IntSize.Zero) return null
        val total = totalScale
        val size = diameter / total
        val left = (bitmap.width * total / 2F - offset.x - diameter / 2F) / total
        val top = (bitmap.height * total / 2F - offset.y - diameter / 2F) / total
        return Rect(
            left.roundToInt(),
            top.roundToInt(),
            (left + size).roundToInt(),
            (top + size).roundToInt()
        )
    }
}

@Composable
private fun CropArea(frame: CropFrame, modifier: Modifier = Modifier) {
    val image = remember(frame.bitmap) { frame.bitmap.asImageBitmap() }

    Canvas(
        modifier = modifier
            .onSizeChanged { frame.containerSize = it }
            .pointerInput(frame) {
                detectTransformGestures { _, pan, zoom, _ -> frame.transform(pan, zoom) }
            }
    ) {
        val total = frame.totalScale
        val width = frame.bitmap.width * total
        val height = frame.bitmap.height * total
        val center = Offset(size.width / 2F, size.height / 2F)
        val radius = frame.diameter / 2F

        drawImage(
            image = image,
            dstOffset = IntOffset(
                (center.x + frame.offset.x - width / 2F).roundToInt(),
                (center.y + frame.offset.y - height / 2F).roundToInt()
            ),
            dstSize = IntSize(width.roundToInt(), height.roundToInt()),
            filterQuality = FilterQuality.High
        )

        clipPath(Path().apply { addOval(ComposeRect(center = center, radius = radius)) }, clipOp = ClipOp.Difference) {
            drawRect(Color.Black.copy(alpha = .66F))
        }
        drawCircle(
            color = Color.White.copy(alpha = .85F),
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
