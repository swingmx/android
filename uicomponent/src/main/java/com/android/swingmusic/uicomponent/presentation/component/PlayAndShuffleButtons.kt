package com.android.swingmusic.uicomponent.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme

/** Filled "Play" and outlined "Shuffle" sharing the width, as used on the album and artist screens. */
@Composable
fun PlayAndShuffleButtons(
    onShuffle: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPlay,
            modifier = Modifier.weight(1F),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            ButtonIcon(R.drawable.play_arrow_fill)
            Text(text = "Play")
        }

        OutlinedButton(
            onClick = onShuffle,
            modifier = Modifier.weight(1F),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .3F)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            ButtonIcon(R.drawable.shuffle)
            Text(text = "Shuffle")
        }
    }
}

@Composable
private fun ButtonIcon(icon: Int) {
    Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        modifier = Modifier.size(ButtonDefaults.IconSize)
    )
    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PlayAndShuffleButtonsPreview() {
    SwingMusicTheme {
        PlayAndShuffleButtons(
            onShuffle = {},
            onPlay = {}
        )
    }
}
