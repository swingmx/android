package com.android.swingmusic.uicomponent.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import java.util.Locale

@Composable
fun PathIndicatorItem(
    folder: Folder,
    isRootPath: Boolean = false,
    isCurrentPath: Boolean,
    onClick: (Folder) -> Unit
) {
    val currentPathTint = MaterialTheme.colorScheme.onSurface
    val nonCurrentPathTint = MaterialTheme.colorScheme.onSurface.copy(alpha = .30F)

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onClick(folder)
            }
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRootPath) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    painter = painterResource(id = R.drawable.folder_outlined),
                    tint = if (isCurrentPath) currentPathTint else nonCurrentPathTint,
                    contentDescription = "Outlined Folder Icon"
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = folder.name.uppercase(Locale.ROOT),
                modifier = Modifier.padding(vertical = 2.dp),
                style = MaterialTheme.typography.titleMedium,
                color = if (isCurrentPath) currentPathTint else nonCurrentPathTint
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.NEXUS_5
)
@Composable
fun PathIndicatorItemPreview() {
    val home = Folder(
        trackCount = 1,
        folderCount = 1,
        isSym = false,
        name = "",
        path = "/home"
    )

    SwingMusicTheme {
        Surface {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PathIndicatorItem(
                    folder = home,
                    isRootPath = true,
                    isCurrentPath = false,
                    onClick = {

                    }
                )
                PathIndicatorItem(
                    folder = home.copy(name = "Sample"),
                    isRootPath = false,
                    isCurrentPath = true,
                    onClick = {

                    }
                )
                PathIndicatorItem(
                    folder = home.copy(name = "Sample"),
                    isRootPath = false,
                    isCurrentPath = false,
                    onClick = {

                    }
                )
            }
        }
    }
}