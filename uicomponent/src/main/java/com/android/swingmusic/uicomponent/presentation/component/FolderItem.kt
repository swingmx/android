package com.android.swingmusic.uicomponent.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme

@Composable
fun FolderItem(
    folder: Folder,
    onClickFolderItem: (Folder) -> Unit,
    onClickMoreVert: (Folder) -> Unit
) {
    Surface(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .clickable {
                onClickFolderItem(folder)
            }
            .padding(horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    start = (11.5).dp, // to align well with Track Item
                    top = 12.dp,
                    bottom = 12.dp,
                    end = 8.dp
                )
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Folder Icon
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                        .requiredSize(48.dp)
                        .clip(RoundedCornerShape(16))
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = .1F)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier,
                        painter = painterResource(id = R.drawable.folder_outlined_open),
                        contentDescription = "Folder Icon",
                    )
                }

                // Folder name, folder count, songs
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.bodyLarge,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (folder.folderCount != 0) {
                            Text(
                                text = folder.folderCount.getFolderHelperText(),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (folder.folderCount > 0 && folder.trackCount > 0) {
                            // Dot Separator
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clip(CircleShape)
                                    .size(3.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .50F)
                                    )
                            )
                        }
                        if (folder.trackCount != 0) {
                            Text(
                                text = folder.trackCount.getTrackCountHelperText(),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (folder.folderCount == 0) {
                            Text(
                                text = "This folder is empty",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Reserve space for menu icon to maintain consistent alignment with TrackItem
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                // TODO: Return this when contextual menu is ready
                /*IconButton(onClick = { onClickMoreVert(folder) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "MoreVert"
                    )
                }*/
            }
        }
    }
}

private fun Int.getTrackCountHelperText(): String {
    return if (this <= 1) "$this Track" else "$this Tracks"
}

private fun Int.getFolderHelperText(): String {
    return if (this <= 1) "$this Folder" else "$this Folders"
}

@Preview(
    showBackground = true,
    device = Devices.PIXEL_6,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Composable
fun FolderItemPreview() {
    // Edge case folders
    val shortFolder = Folder(
        trackCount = 1,
        folderCount = 0,
        isSym = false,
        name = "Music",
        path = "/music"
    )

    val longFolder = Folder(
        trackCount = 9999,
        folderCount = 999,
        isSym = true,
        name = "This Is An Extremely Long Folder Name That Should Definitely Cause Text Overflow Issues In Small Screens",
        path = "/very/long/path/to/folder"
    )

    val emptyFolder = Folder(
        trackCount = 0,
        folderCount = 0,
        isSym = false,
        name = "Empty Folder",
        path = "/empty"
    )

    val onlyFoldersFolder = Folder(
        trackCount = 0,
        folderCount = 15,
        isSym = false,
        name = "Parent Directory With Many Subfolders",
        path = "/parent"
    )

    val onlyTracksFolder = Folder(
        trackCount = 250,
        folderCount = 0,
        isSym = false,
        name = "Album With Many Tracks But No Subfolders",
        path = "/album"
    )

    SwingMusicTheme {
        Surface {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Folder Edge Cases:",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                // Short folder name
                FolderItem(
                    folder = shortFolder,
                    onClickFolderItem = { },
                    onClickMoreVert = { }
                )

                // Very long folder name - should ellipse
                FolderItem(
                    folder = longFolder,
                    onClickFolderItem = { },
                    onClickMoreVert = { }
                )

                // Empty folder
                FolderItem(
                    folder = emptyFolder,
                    onClickFolderItem = { },
                    onClickMoreVert = { }
                )

                // Only folders, no tracks
                FolderItem(
                    folder = onlyFoldersFolder,
                    onClickFolderItem = { },
                    onClickMoreVert = { }
                )

                // Only tracks, no folders
                FolderItem(
                    folder = onlyTracksFolder,
                    onClickFolderItem = { },
                    onClickMoreVert = { }
                )

                // Mixed content with moderate counts
                FolderItem(
                    folder = Folder(
                        trackCount = 42,
                        folderCount = 7,
                        isSym = false,
                        name = "Mixed Content Folder",
                        path = "/mixed"
                    ),
                    onClickFolderItem = { },
                    onClickMoreVert = { }
                )
            }
        }
    }
}
