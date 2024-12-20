package com.android.swingmusic.uicomponent.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.BottomSheetItemModel
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.BottomSheetAction
import com.android.swingmusic.uicomponent.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTrackBottomSheet(
    scope: CoroutineScope,
    sheetState: SheetState,
    clickedTrack: Track?,
    baseUrl: String,
    bottomSheetItems: List<BottomSheetItemModel>,
    currentArtisthash: String? = null,
    onToggleTrackFavorite: (isFavorite: Boolean, trackHash: String) -> Unit,
    onHideBottomSheet: (show: Boolean) -> Unit,
    onClickSheetItem: (track: Track, sheetAction: BottomSheetAction) -> Unit,
    onChooseArtist: (hash: String) -> Unit
) {
    var artistDialogExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        sheetState = sheetState,
        shape = RoundedCornerShape(16.dp),
        onDismissRequest = {
            onHideBottomSheet(false)
        },
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = .75F),
        tonalElevation = 16.dp,
    ) {
        clickedTrack?.let { track ->
            Column {
                Box(
                    modifier = Modifier.offset(x = (-8).dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    TrackItem(
                        track = track,
                        onClickTrackItem = {},
                        onClickMoreVert = {},
                        baseUrl = baseUrl
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(
                            modifier = Modifier
                                .clip(CircleShape),
                            onClick = {
                                onToggleTrackFavorite(track.isFavorite, track.trackHash)
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
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        bottomSheetItems.forEach { model ->
            BottomSheetItem(
                label = model.label,
                enabled = model.enabled,
                baseUrl = baseUrl,
                sheetAction = model.sheetAction,
                currentArtistHash = currentArtisthash,
                expandArtistDialog = artistDialogExpanded,
                iconPainter = painterResource(id = model.painterId),
                clickedTrack = clickedTrack,
                onClickSheetItem = { track ->
                    when (val action = model.sheetAction) {
                        is BottomSheetAction.OpenArtistsDialog -> {
                            if (action.artists.size == 1) {
                                onChooseArtist(action.artists[0].artistHash)
                            } else {
                                artistDialogExpanded = true
                            }
                        }

                        else -> {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) onHideBottomSheet(false)
                            }
                        }
                    }

                    if (track.trackHash == model.track.trackHash) {
                        onClickSheetItem(track, model.sheetAction)
                    }
                },
                onChooseArtist = {
                    onHideBottomSheet(false)
                    onChooseArtist(it)
                },
                onDialogDismissRequest = { expand ->
                    artistDialogExpanded = expand
                }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun BottomSheetItem(
    label: String,
    enabled: Boolean = true,
    baseUrl: String,
    expandArtistDialog: Boolean,
    sheetAction: BottomSheetAction,
    iconPainter: Painter,
    clickedTrack: Track?,
    currentArtistHash: String?,
    onDialogDismissRequest: (expand: Boolean) -> Unit,
    onClickSheetItem: (track: Track) -> Unit,
    onChooseArtist: (hash: String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                clickedTrack?.let { track ->
                    onClickSheetItem(track)
                }
            }
            .padding(all = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = iconPainter,
                tint = if (!enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = .30F) else
                    MaterialTheme.colorScheme.onSurface,
                contentDescription = "Icon"
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                color = if (!enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = .305F) else
                    MaterialTheme.colorScheme.onSurface
            )
        }.also {
            when (sheetAction) {
                is BottomSheetAction.OpenArtistsDialog -> {
                    if (expandArtistDialog) {
                        Dialog(
                            properties = DialogProperties(decorFitsSystemWindows = true),
                            onDismissRequest = { onDialogDismissRequest(false) })
                        {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                tonalElevation = 8.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "Choose Artist",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    sheetAction.artists.forEach { artist ->
                                        val clickable = artist.artistHash != currentArtistHash

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(
                                                    enabled = clickable,
                                                    indication = null,
                                                    interactionSource = interactionSource
                                                ) {
                                                    onDialogDismissRequest(false)
                                                    onChooseArtist(artist.artistHash)
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                modifier = Modifier
                                                    .padding(vertical = 8.dp)
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        MaterialTheme.colorScheme.inversePrimary.copy(
                                                            .5F
                                                        )
                                                    ),
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data("${baseUrl}img/artist/small/${artist.artistHash}.webp")
                                                    .build(),
                                                placeholder = painterResource(R.drawable.artist_fallback),
                                                fallback = painterResource(R.drawable.artist_fallback),
                                                error = painterResource(R.drawable.artist_fallback),
                                                contentScale = ContentScale.Crop,
                                                colorFilter = if (clickable) null else ColorMatrixColorFilter(
                                                    ColorMatrix().apply { setToSaturation(0f) }
                                                ),
                                                contentDescription = "Track Image"
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = artist.name,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = if (clickable) MaterialTheme.colorScheme.onSurface else
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = .20F)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}
