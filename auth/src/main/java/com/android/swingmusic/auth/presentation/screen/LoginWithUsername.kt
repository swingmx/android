package com.android.swingmusic.auth.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme

@Composable
fun LoginWithUsername() {
    Scaffold { paddingValues ->
        // TODO: Show Users in a Grid, Tap to select one to log into, Show Inputs
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .wrapContentHeight()
                .fillMaxWidth(.90F)
                .clip(RoundedCornerShape(10))
                .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = .3F))
                .padding(top = 32.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .clip(CircleShape)
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = .3F)),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = R.drawable.artist_fallback),
                contentDescription = "App Logo"
            )
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Composable
fun LoginWithUsernamePreview() {
    SwingMusicTheme {
        LoginWithUsername()
    }
}
