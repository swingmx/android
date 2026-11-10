package com.android.swingmusic.uicomponent.presentation.component

import com.android.swingmusic.core.domain.model.BottomSheetItemModel
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.BottomSheetAction
import com.android.swingmusic.uicomponent.R

enum class TrackMenuOption {
    PlayNext, AddToQueue, AddToPlaylist, RemoveFromPlaylist, RemoveFromQueue, GotoAlbum, GotoArtist, GotoFolder
}

/**
 * The items of a track's context menu, in the order used everywhere in the app:
 * queue actions, then add-to actions, then go-to actions.
 *
 * [hide] drops options that make no sense on the current screen (e.g. Go to Album on the
 * album's own page). [removeFromPlaylist] and [removeFromQueue] add the screen-specific items.
 */
fun trackMenuItems(
    track: Track,
    hide: Set<TrackMenuOption> = emptySet(),
    removeFromPlaylist: Boolean = false,
    removeFromQueue: Boolean = false,
    gotoArtistEnabled: Boolean = true,
): List<BottomSheetItemModel> = buildList {
    fun item(
        option: TrackMenuOption,
        label: String,
        icon: Int,
        action: BottomSheetAction,
        enabled: Boolean = true,
    ) {
        if (option in hide) return
        add(BottomSheetItemModel(enabled = enabled, label = label, painterId = icon, track = track, sheetAction = action))
    }

    item(TrackMenuOption.PlayNext, "Play Next", R.drawable.play_next, BottomSheetAction.PlayNext)
    item(TrackMenuOption.AddToQueue, "Add to Queue", R.drawable.add_to_queue, BottomSheetAction.AddToQueue)
    item(TrackMenuOption.AddToPlaylist, "Add to Playlist", R.drawable.playlist_add, BottomSheetAction.AddToPlaylist)
    if (removeFromPlaylist) {
        item(TrackMenuOption.RemoveFromPlaylist, "Remove from Playlist", R.drawable.playlist_remove, BottomSheetAction.RemoveFromPlaylist)
    }
    if (removeFromQueue) {
        item(TrackMenuOption.RemoveFromQueue, "Remove from Queue", R.drawable.playlist_remove, BottomSheetAction.RemoveFromQueue)
    }
    item(TrackMenuOption.GotoAlbum, "Go to Album", R.drawable.ic_album, BottomSheetAction.GotoAlbum)
    item(
        TrackMenuOption.GotoArtist, "Go to Artist", R.drawable.ic_artist,
        BottomSheetAction.OpenArtistsDialog(track.trackArtists), enabled = gotoArtistEnabled
    )
    item(
        TrackMenuOption.GotoFolder, "Go to Folder", R.drawable.folder_outlined_open,
        BottomSheetAction.GotoFolder(name = track.folder.trimEnd('/').substringAfterLast('/'), path = track.folder)
    )
}
