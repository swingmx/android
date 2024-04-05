package com.android.swingmusic.uicomponent.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme

@Composable
fun PathIndicatorItem(
    folder: Folder,
    isRootPath: Boolean = false,
    isCurrentPath: Boolean,
    onClick: (Folder) -> Unit
) {
    SwingMusicTheme {
        val color = if (isCurrentPath) MaterialTheme.colorScheme.onSurface.copy(alpha = .25F) else
            Color.Unspecified

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(color)
                .clickable {
                    onClick(folder)
                }
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = 2.dp,
                    bottom = 3.dp
                )
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
                        contentDescription = "Outlined Folder Icon"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(text = folder.name)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PathIndicatorItemPreview() {
    val home = Folder(
        trackCount = 10,
        folderCount = 10,
        isSym = false,
        name = "Sample",
        path = "/sample"
    )

    SwingMusicTheme {
        PathIndicatorItem(
            folder = home,
            isRootPath = true,
            isCurrentPath = true,
            onClick = {

            }
        )
    }
}