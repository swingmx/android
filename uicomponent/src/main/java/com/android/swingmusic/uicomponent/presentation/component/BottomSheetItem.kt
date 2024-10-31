package com.android.swingmusic.uicomponent.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.android.swingmusic.core.domain.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetItem(
    label: String,
    scope: CoroutineScope,
    sheetState: SheetState,
    iconPainter: Painter,
    clickedTrack: Track?,
    onClick: (track: Track) -> Unit,
    onHideBottomSheet: (hide: Boolean) -> Unit
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable {
            clickedTrack?.let { track ->
                onClick(track)
            }

            scope
                .launch { sheetState.hide() }
                .invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onHideBottomSheet(true)
                    }
                }
        }
        .padding(all = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = iconPainter,
                contentDescription = "Icon"
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(text = label)
        }
    }
}
