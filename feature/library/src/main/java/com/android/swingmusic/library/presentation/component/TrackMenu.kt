package com.android.swingmusic.library.presentation.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.android.swingmusic.core.domain.model.BottomSheetItemModel
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.BottomSheetAction
import com.android.swingmusic.playlist.domain.toPlaylistTarget
import com.android.swingmusic.playlist.presentation.screen.AddToPlaylistSheet
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.CustomTrackBottomSheet

/**
 * The track menu for Library screens, with Add to Playlist built in. [onRemoveFromPlaylist]
 * adds a Remove from Playlist item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackMenu(
    track: Track,
    isFavourite: Boolean,
    baseUrl: String,
    onDismiss: () -> Unit,
    onToggleFavourite: (trackHash: String, isFavourite: Boolean) -> Unit,
    onGotoAlbum: (albumHash: String) -> Unit,
    onGotoArtist: (artistHash: String) -> Unit,
    onGotoFolder: (name: String, path: String) -> Unit,
    onQueue: (track: Track, playNext: Boolean) -> Unit,
    onMessage: (String) -> Unit,
    onRemoveFromPlaylist: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(true) }
    var showAddToPlaylist by remember { mutableStateOf(false) }

    if (showMenu) {
        CustomTrackBottomSheet(
            scope = scope,
            sheetState = sheetState,
            clickedTrack = track,
            isFavorite = isFavourite,
            baseUrl = baseUrl,
            bottomSheetItems = buildList {
                add(item("Go to Artist", R.drawable.ic_artist, track, BottomSheetAction.OpenArtistsDialog(track.trackArtists)))
                add(item("Go to Album", R.drawable.ic_album, track, BottomSheetAction.GotoAlbum))
                add(
                    item(
                        "Go to Folder", R.drawable.folder_outlined_open, track,
                        BottomSheetAction.GotoFolder(name = track.folder.trimEnd('/').substringAfterLast('/'), path = track.folder)
                    )
                )
                add(item("Play Next", R.drawable.play_next, track, BottomSheetAction.PlayNext))
                add(item("Add to Queue", R.drawable.add_to_queue, track, BottomSheetAction.AddToQueue))
                add(item("Add to Playlist", R.drawable.playlist_add, track, BottomSheetAction.AddToPlaylist))
                if (onRemoveFromPlaylist != null) {
                    add(item("Remove from Playlist", R.drawable.playlist_remove, track, BottomSheetAction.RemoveFromPlaylist))
                }
            },
            onHideBottomSheet = { visible ->
                if (!visible) {
                    showMenu = false
                    if (!showAddToPlaylist) onDismiss()
                }
            },
            onClickSheetItem = { sheetTrack, action ->
                when (action) {
                    BottomSheetAction.GotoAlbum -> onGotoAlbum(sheetTrack.albumHash)
                    is BottomSheetAction.GotoFolder -> onGotoFolder(action.name, action.path)
                    BottomSheetAction.PlayNext -> onQueue(sheetTrack, true)
                    BottomSheetAction.AddToQueue -> onQueue(sheetTrack, false)
                    BottomSheetAction.AddToPlaylist -> showAddToPlaylist = true
                    BottomSheetAction.RemoveFromPlaylist -> onRemoveFromPlaylist?.invoke()
                    else -> Unit
                }
            },
            onChooseArtist = { hash ->
                onDismiss()
                onGotoArtist(hash)
            },
            onToggleTrackFavorite = onToggleFavourite
        )
    }

    if (showAddToPlaylist) {
        AddToPlaylistSheet(
            target = track.toPlaylistTarget(),
            baseUrl = baseUrl,
            onBack = { showMenu = true },
            onDismiss = {
                showAddToPlaylist = false
                if (!showMenu) onDismiss()
            },
            onDone = onMessage
        )
    }
}

private fun item(label: String, icon: Int, track: Track, action: BottomSheetAction) =
    BottomSheetItemModel(label = label, painterId = icon, track = track, sheetAction = action)
