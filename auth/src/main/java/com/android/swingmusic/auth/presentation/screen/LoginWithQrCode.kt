package com.android.swingmusic.auth.presentation.screen

import android.content.res.Configuration
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.swingmusic.auth.R
import com.android.swingmusic.auth.presentation.event.AuthUiEvent
import com.android.swingmusic.auth.presentation.state.AuthState
import com.android.swingmusic.auth.presentation.util.AuthError
import com.android.swingmusic.auth.presentation.viewmodel.AuthViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import qrscanner.QrScanner

@Composable
fun LogInWithQrCode(authViewModel: AuthViewModel = viewModel()) {

    val authUiState by remember { authViewModel.authUiState }

    var encodedString by remember { mutableStateOf("") }
    var startScanner by remember { mutableStateOf(true) }
    var flashlightOn by remember { mutableStateOf(false) }
    var launchGallery by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val interactionSrc = remember { MutableInteractionSource() }
    val context = LocalContext.current

    var statusTextColor: Color = MaterialTheme.colorScheme.onSurface
    val statusText =
        if (authUiState.isLoading) {
            statusTextColor = MaterialTheme.colorScheme.onSurface
            "Loading..."
        } else if (authUiState.authState == AuthState.AUTHENTICATED) {
            statusTextColor = MaterialTheme.colorScheme.onSurface
            "Authenticated"
        } else when (val error = authUiState.authError) {
            is AuthError.LoginError -> {
                statusTextColor = MaterialTheme.colorScheme.error
                error.msg
            }

            else -> ""
        }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TODO: Replace this with Swing Music icon
            Image(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .clip(CircleShape)
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = .3F)),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = com.android.swingmusic.uicomponent.R.drawable.artist_fallback),
                contentDescription = "App Logo"
            )

            Spacer(modifier = Modifier.height(16.dp))

            val text = buildAnnotatedString {
                append("Open Swing Music on the web and go to ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Settings > Pair device ")
                }
                append("or select an image from your gallery")
            }

            Text(
                modifier = Modifier.padding(horizontal = 14.dp),
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(.90F)
                    .clip(RoundedCornerShape(10))
                    .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = .3F))
                    .padding(top = 32.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(shape = RoundedCornerShape(size = 14.dp))
                        .clipToBounds()
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = .5F),
                            RoundedCornerShape(size = 14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (startScanner) {
                        QrScanner(
                            modifier = Modifier
                                .clipToBounds()
                                .clip(shape = RoundedCornerShape(size = 14.dp)),
                            flashlightOn = flashlightOn,
                            launchGallery = launchGallery,
                            onCompletion = {
                                flashlightOn = false
                                encodedString = it
                                startScanner = false

                                authViewModel.onAuthUiEvent(AuthUiEvent.LogInWithQrCode(encoded = it))
                            },
                            onGalleryCallBackHandler = {
                                launchGallery = it
                            },
                            onFailure = {
                                coroutineScope.launch {
                                    if (it.isEmpty()) {
                                        Toast.makeText(context, "No valid Qr Code detected", LENGTH_SHORT)
                                            .show()
                                    } else {
                                        Toast.makeText(context, it, LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = .45F))
                                .clipToBounds()
                                .clickable {
                                    authViewModel.onAuthUiEvent(AuthUiEvent.ResetStates)

                                    encodedString = ""
                                    startScanner = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "TAP TO SCAN",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusText,
                        color = statusTextColor.copy(alpha = .84F),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(25.dp)
                        )
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 18.dp)
                            .wrapContentHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Icon(painter = if (flashlightOn)
                            painterResource(id = R.drawable.flashlight_on) else
                            painterResource(id = R.drawable.flashlight_off),
                            contentDescription = "flash",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(indication = null, interactionSource = interactionSrc) {
                                    flashlightOn = !flashlightOn
                                }
                        )

                        VerticalDivider(
                            modifier = Modifier.height(32.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = .75F)
                        )

                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(indication = null, interactionSource = interactionSrc) {
                                    coroutineScope.launch {
                                        authViewModel.onAuthUiEvent(AuthUiEvent.ResetStates)

                                        startScanner = true
                                        delay(1000)
                                        launchGallery = true
                                    }
                                },
                            painter = painterResource(R.drawable.gallery_icon),
                            contentDescription = "gallery",
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(.3f),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider()
                Text(
                    text = "OR",
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier,
                onClick = {
                    // TODO: Navigate to Login with username and password
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(text = "Login With Username")
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Composable
fun AuthWithQrPreview() {
    SwingMusicTheme {
        LogInWithQrCode()
    }
}
