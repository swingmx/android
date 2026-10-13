package com.android.swingmusic.uicomponent.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.composed
import androidx.compose.material3.ripple
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.indication
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.uicomponent.R
import kotlinx.coroutines.launch

enum class OptionsItemType(val searchPath: String) { ALBUM("album"), ARTIST("artist") }

private data class SearchOnService(
    val name: String,
    @DrawableRes val icon: Int,
    val tint: Color?,
    /** One-colour version for the default monochrome look; null when [icon] already is one. */
    @DrawableRes val monoIcon: Int?,
    val url: (term: String, type: String) -> String
)

// Same services and URLs as the web client's "Search on" menu.
// Colour logos keep their own colours (Unspecified); single-colour marks are tinted, null follows the text.
private val searchOnServices = listOf(
    SearchOnService("Google", R.drawable.ic_brand_google, Color.Unspecified, R.drawable.ic_brand_google_mono) { q, _ ->
        "https://www.google.com/search?q=$q"
    },
    SearchOnService("YouTube", R.drawable.ic_brand_youtube, Color.Unspecified, R.drawable.ic_brand_youtube_mono) { q, _ ->
        "https://www.youtube.com/results?search_query=$q"
    },
    SearchOnService("Spotify", R.drawable.ic_brand_spotify, Color.Unspecified, R.drawable.ic_brand_spotify_mono) { q, t ->
        "https://open.spotify.com/search/$q/${t}s"
    },
    SearchOnService("Tidal", R.drawable.ic_brand_tidal, null, null) { q, t ->
        "https://listen.tidal.com/search/${t}s?q=$q"
    },
    SearchOnService("Apple Music", R.drawable.ic_brand_applemusic, Color.Unspecified, R.drawable.ic_brand_applemusic_mono) { q, _ ->
        "https://music.apple.com/search?term=$q"
    },
    SearchOnService("Deezer", R.drawable.ic_brand_deezer, Color.Unspecified, R.drawable.ic_brand_deezer_mono) { q, t ->
        "https://www.deezer.com/search/$q/$t"
    },
    SearchOnService("Wikipedia", R.drawable.ic_brand_wikipedia, null, null) { q, _ ->
        "https://en.wikipedia.org/wiki/Special:Search?search=$q"
    },
    SearchOnService("Last.fm", R.drawable.ic_brand_lastdotfm, Color(0xFFD51007), null) { q, t ->
        "https://www.last.fm/search/${t}s?q=$q"
    },
)

/** Options for a whole album or artist, opened from the ⋯ button in the top bar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemOptionsSheet(
    type: OptionsItemType,
    title: String,
    subtitle: String,
    imageUrl: String,
    searchTerm: String,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onSearchOn: (url: String) -> Unit,
    onAddToPlaylist: (() -> Unit)? = null,
    brandColours: Boolean = false,
    onToggleBrandColours: () -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSearchOn by remember { mutableStateOf(false) }

    fun hideThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            action()
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        shape = RoundedCornerShape(16.dp),
        onDismissRequest = onDismiss,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = .75F),
        tonalElevation = 16.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isArtist = type == OptionsItemType.ARTIST
            val fallback = if (isArtist) R.drawable.artist_fallback else R.drawable.audio_fallback
            AsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .clip(if (isArtist) CircleShape else RoundedCornerShape(8.dp)),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(fallback),
                fallback = painterResource(fallback),
                error = painterResource(fallback),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(4.dp))

        if (showSearchOn) {
            // Tap goes back; a 5 second hold switches between monochrome and brand colour logos
            val haptics = LocalHapticFeedback.current
            OptionRow(
                icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                label = "Search On",
                bold = true,
                onClick = { showSearchOn = false },
                gestures = Modifier.tapOrHold(
                    holdMillis = BRAND_COLOURS_HOLD_MILLIS,
                    onTap = { showSearchOn = false },
                    onHold = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleBrandColours()
                    }
                )
            )
            val encodedTerm = Uri.encode(searchTerm)
            searchOnServices.forEachIndexed { index, service ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06F)
                    )
                }
                val monochrome = !brandColours
                OptionRow(
                    icon = painterResource(if (monochrome) service.monoIcon ?: service.icon else service.icon),
                    iconTint = if (monochrome) MaterialTheme.colorScheme.onSurface
                    else service.tint ?: MaterialTheme.colorScheme.onSurface,
                    label = service.name,
                    iconSize = 20.dp,
                    verticalPadding = 16.dp,
                    showOpenInNew = true,
                    onClick = { hideThen { onSearchOn(service.url(encodedTerm, type.searchPath)) } }
                )
            }
        } else {
            OptionRow(
                icon = painterResource(R.drawable.play_next),
                label = "Play Next",
                onClick = { hideThen(onPlayNext) }
            )
            OptionRow(
                icon = painterResource(R.drawable.add_to_queue),
                label = "Add to Queue",
                onClick = { hideThen(onAddToQueue) }
            )
            onAddToPlaylist?.let { addToPlaylist ->
                OptionRow(
                    icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.PlaylistAdd),
                    label = "Add to Playlist",
                    onClick = { hideThen(addToPlaylist) }
                )
            }
            OptionRow(
                icon = painterResource(R.drawable.ic_search),
                label = "Search On",
                showChevron = true,
                onClick = { showSearchOn = true }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun OptionRow(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    iconSize: Dp = 24.dp,
    verticalPadding: Dp = 12.dp,
    bold: Boolean = false,
    showChevron: Boolean = false,
    showOpenInNew: Boolean = false,
    gestures: Modifier? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(gestures ?: Modifier.clickable(onClick = onClick))
            .padding(horizontal = 12.dp, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // A smaller icon keeps the same box, so labels stay aligned with the other rows
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            Icon(painter = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(iconSize))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontWeight = if (bold) FontWeight.SemiBold else null,
            modifier = Modifier.weight(1F)
        )
        if (showChevron) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showOpenInNew) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private const val BRAND_COLOURS_HOLD_MILLIS = 5_000L

/**
 * A quick tap runs [onTap]; holding for [holdMillis] runs [onHold] instead. Releasing in between
 * does nothing, so the hidden action only happens on purpose.
 */
private fun Modifier.tapOrHold(holdMillis: Long, onTap: () -> Unit, onHold: () -> Unit): Modifier =
    composed {
        val interactionSource = remember { MutableInteractionSource() }
        indication(interactionSource, ripple())
            .pointerInput(holdMillis) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val press = PressInteraction.Press(down.position)
                    interactionSource.tryEmit(press)
                    val up = withTimeoutOrNull(holdMillis) { waitForUpOrCancellation() }
                    interactionSource.tryEmit(PressInteraction.Release(press))
                    when {
                        up == null -> {
                            onHold()
                            waitForUpOrCancellation()
                        }

                        up.uptimeMillis - down.uptimeMillis < viewConfiguration.longPressTimeoutMillis -> onTap()
                    }
                }
            }
    }
