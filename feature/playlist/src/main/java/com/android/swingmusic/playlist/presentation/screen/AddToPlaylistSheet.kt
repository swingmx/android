package com.android.swingmusic.playlist.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.Playlist
import com.android.swingmusic.playlist.domain.PlaylistTarget
import com.android.swingmusic.playlist.presentation.event.AddToPlaylistUiEffect
import com.android.swingmusic.playlist.presentation.event.AddToPlaylistUiEvent
import com.android.swingmusic.playlist.presentation.state.AddToPlaylistUiState
import com.android.swingmusic.playlist.presentation.viewmodel.AddToPlaylistViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.FilledTextField
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import kotlinx.coroutines.launch

/**
 * Picks a playlist to add [target] to, or creates a new one from it.
 * [onDone] receives a confirmation message once the sheet has closed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    target: PlaylistTarget,
    baseUrl: String,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    onDone: (message: String) -> Unit,
) {
    // A fresh key per opening, so the list is reloaded every time the sheet is shown.
    val key = remember { "add-to-playlist:${System.nanoTime()}" }
    val viewModel = hiltViewModel<AddToPlaylistViewModel, AddToPlaylistViewModel.Factory>(
        key = key,
        creationCallback = { factory -> factory.create(target) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            is AddToPlaylistUiEffect.Done -> {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    onDismiss()
                    onDone(effect.message)
                }
            }
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
        AddToPlaylistContent(
            uiState = uiState,
            baseUrl = baseUrl,
            onBack = {
                if (uiState.showNewPlaylistField) {
                    viewModel.onEvent(AddToPlaylistUiEvent.OnCloseNewPlaylist)
                } else {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        onDismiss()
                        onBack()
                    }
                }
            },
            onEvent = viewModel::onEvent
        )
    }
}

@Composable
private fun AddToPlaylistContent(
    uiState: AddToPlaylistUiState,
    baseUrl: String,
    onBack: () -> Unit,
    onEvent: (AddToPlaylistUiEvent) -> Unit,
) {
    // Back closes the new playlist field before it leaves the sheet.
    BackHandler(enabled = uiState.showNewPlaylistField) {
        onEvent(AddToPlaylistUiEvent.OnCloseNewPlaylist)
    }
    // Always full height, so loading or scrolling the list never resizes the sheet.
    Column(modifier = Modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Add to Playlist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(modifier = Modifier.height(2.dp)) {
            if (uiState.isSaving) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        uiState.errorAddingToPlaylist?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (uiState.showNewPlaylistField) {
            NewPlaylistField(uiState = uiState, onEvent = onEvent)
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(AddToPlaylistUiEvent.OnNewPlaylistClicked) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "New Playlist", style = MaterialTheme.typography.bodyLarge)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06F))

        when {
            uiState.isLoadingPlaylists -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            }

            uiState.errorLoadingPlaylists != null -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.errorLoadingPlaylists,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { onEvent(AddToPlaylistUiEvent.OnRetry) }) {
                    Text(text = "Retry")
                }
            }

            uiState.playlists.isEmpty() -> Text(
                text = "No playlists yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )

            else -> LazyColumn(modifier = Modifier.weight(1F)) {
                itemsIndexed(uiState.playlists, key = { _, playlist -> playlist.id }) { index, playlist ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 76.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06F)
                        )
                    }
                    PlaylistRow(
                        playlist = playlist,
                        baseUrl = baseUrl,
                        onClick = { onEvent(AddToPlaylistUiEvent.OnPlaylistClicked(playlist)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun NewPlaylistField(
    uiState: AddToPlaylistUiState,
    onEvent: (AddToPlaylistUiEvent) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        FilledTextField(
            value = uiState.newPlaylistName,
            onValueChange = { onEvent(AddToPlaylistUiEvent.OnNewPlaylistNameChanged(it)) },
            placeholder = "Playlist name",
            modifier = Modifier
                .weight(1F)
                .focusRequester(focusRequester),
            errorText = uiState.newPlaylistError,
            onDone = { onEvent(AddToPlaylistUiEvent.OnCreatePlaylist) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilledIconButton(
            onClick = { onEvent(AddToPlaylistUiEvent.OnCreatePlaylist) },
            enabled = !uiState.isSaving && uiState.newPlaylistName.isNotBlank(),
            modifier = Modifier
                .padding(top = 4.dp)
                .size(48.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Check, contentDescription = "Create playlist")
        }
    }
}

@Composable
private fun PlaylistRow(playlist: Playlist, baseUrl: String, onClick: () -> Unit) {
    val imageUrl = when {
        playlist.customImage != null -> "${baseUrl}img/playlist/${playlist.customImage}"
        playlist.images.isNotEmpty() -> "${baseUrl}img/thumbnail/small/${playlist.images.first()}"
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.audio_fallback),
            fallback = painterResource(R.drawable.audio_fallback),
            error = painterResource(R.drawable.audio_fallback),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (playlist.trackCount == 1) "1 track" else "${playlist.trackCount} tracks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun AddToPlaylistContentPreview() {
    SwingMusicTheme {
        AddToPlaylistContent(
            uiState = AddToPlaylistUiState(
                isLoadingPlaylists = false,
                playlists = listOf(
                    Playlist(id = "1", name = "Road trip", customImage = null, images = emptyList(), trackCount = 24),
                    Playlist(id = "2", name = "Focus", customImage = null, images = emptyList(), trackCount = 1),
                )
            ),
            baseUrl = "",
            onBack = {},
            onEvent = {}
        )
    }
}
