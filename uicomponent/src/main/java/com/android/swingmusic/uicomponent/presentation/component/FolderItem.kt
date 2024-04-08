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
import androidx.compose.material3.Divider
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
    SwingMusicTheme {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(
                        start = (11.5).dp, // to align well with Track Item
                        top = 12.dp,
                        bottom = 12.dp,
                        end = 16.dp
                    )
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
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
                            // tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .90F),
                            contentDescription = "Folder Icon",
                        )
                    }

                    // Folder name, folder count, songs
                    Column(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            modifier = Modifier.width(240.dp),
                            text = folder.name,
                            style = MaterialTheme.typography.bodyLarge,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (folder.folderCount != 0) {
                                Text(
                                    text = folder.folderCount.getFolderHelperText(),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .80F),
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
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .80F),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else if (folder.folderCount == 0) {
                                Text(
                                    text = "This folder is empty",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .80F),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = { onClickMoreVert(folder) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "MoreVert"
                    )
                }
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
    device = Devices.PIXEL_4,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Composable
fun FolderItemPreview() {
    SwingMusicTheme {
        Surface {
            Column(modifier = Modifier.fillMaxSize()) {
                for (count in 0..6) {
                    val demoFolder =
                        Folder((0..6).random(), (0..6).random(), false, "Swing Music", "/home")
                    FolderItem(
                        folder = demoFolder,
                        onClickFolderItem = {

                        },
                        onClickMoreVert = {

                        }
                    )
                    if (count < 6) {
                        /* Divider(
                             modifier = Modifier.padding(start = 74.dp,end = 12.dp),
                             color = MaterialTheme.colorScheme.onSurface.copy(alpha = .1F)
                         )*/
                    }
                }
            }
        }
    }
}
