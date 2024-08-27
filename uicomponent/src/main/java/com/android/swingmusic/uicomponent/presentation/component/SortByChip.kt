package com.android.swingmusic.uicomponent.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.swingmusic.core.domain.util.SortBy
import com.android.swingmusic.core.domain.util.SortOrder
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme_Preview
import com.android.swingmusic.uicomponent.R as UiComponents

@Composable
fun SortByChip(
    labelPair: Pair<SortBy, String>,
    isSelected: Boolean = false,
    sortOrder: SortOrder = SortOrder.DESCENDING,
    onClick: (Pair<SortBy, String>) -> Unit,
) {
    val icon = if (sortOrder == SortOrder.ASCENDING) {
        UiComponents.drawable.arrow_upward
    } else UiComponents.drawable.arrow_downward

    val borderTint = if (isSelected) Color.Transparent else
        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = .5F)

    val bgTint = if (!isSelected) Color.Transparent else
        MaterialTheme.colorScheme.secondaryContainer

    val textColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else
        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = .75F)

    val formattedLabel by remember {
        derivedStateOf {
            when (labelPair.first) {
                SortBy.DURATION -> "Duration"
                SortBy.CREATED_DATE -> "Date Added"
                SortBy.PLAY_COUNT -> "No. Of Plays"
                SortBy.PLAY_DURATION -> "Play Duration"
                SortBy.LAST_PLAYED -> "Last Played"
                SortBy.NO_OF_TRACKS -> "No. Of Tracks"

                SortBy.TITLE -> "Title"
                SortBy.ALBUM_ARTISTS -> "Artists"
                SortBy.DATE -> "Year Released"

                SortBy.NAME -> "Name"
                SortBy.NO_OF_ALBUMS -> "Albums"
                else -> "Label"
            }
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24))
            .background(bgTint)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = borderTint,
                shape = RoundedCornerShape(24)
            )
            .clickable { onClick(labelPair) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                text = formattedLabel,
                color = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            if (isSelected) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(id = icon),
                    contentDescription = "Sort By Icon"
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SortByChipPreview() {

    SwingMusicTheme_Preview {
        Surface {
            Row(
                modifier = Modifier.padding(8.dp)
            ) {
                SortByChip(
                    labelPair = Pair(SortBy.TITLE, "Name"),
                    isSelected = true,
                    sortOrder = SortOrder.ASCENDING
                ) {

                }
                Spacer(modifier = Modifier.width(12.dp))

                SortByChip(
                    labelPair = Pair(SortBy.NAME, "Name"),
                ) {

                }
            }
        }
    }
}
