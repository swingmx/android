package com.android.swingmusic.auth.presentation.screen

import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import com.android.swingmusic.auth.presentation.event.AuthUiEvent
import com.android.swingmusic.auth.presentation.navigation.AuthNavigator
import com.android.swingmusic.auth.presentation.state.AuthState
import com.android.swingmusic.auth.presentation.util.AuthError
import com.android.swingmusic.auth.presentation.viewmodel.AuthViewModel
import com.android.swingmusic.uicomponent.R
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import kotlinx.coroutines.launch
import qrscanner.QrScanner

@Destination
@Composable
fun LoginWithQrCode(
    authViewModel: AuthViewModel,
    authNavigator: AuthNavigator
) {
    val authUiState by authViewModel.authUiState.collectAsState()

    var encodedString by remember { mutableStateOf("") }
    var startScanner by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var statusTextColor: Color = MaterialTheme.colorScheme.onSurface
    val statusText = if (authUiState.isLoading) {
        statusTextColor = MaterialTheme.colorScheme.onSurface
        "LOADING..."
    } else if (authUiState.authState == AuthState.AUTHENTICATED) {
        statusTextColor = MaterialTheme.colorScheme.onSurface
        "AUTHENTICATED"
    } else when (val error = authUiState.authError) {
        is AuthError.LoginError -> {
            statusTextColor = MaterialTheme.colorScheme.error
            error.msg
        }

        else -> ""
    }

    LaunchedEffect(key1 = authUiState.authState, block = {
        if (authUiState.authState == AuthState.AUTHENTICATED) {
           // authNavigator.gotoHomeNavGraph()
            authNavigator.gotoFolderNavGraph()
        }
    })

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clip(CircleShape)
                    .size(80.dp),
                painter = painterResource(id = R.drawable.swing_music_logo_round_outlined),
                contentDescription = "App Logo"
            )

            val text = buildAnnotatedString {
                append("Open Swing Music on the web and go to ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("\nSettings > Pair device ")
                }
            }

            Text(
                modifier = Modifier.padding(horizontal = 14.dp),
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(42.dp))

            Box(
                modifier = Modifier
                    .size(250.dp)
                    .clipToBounds()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = .5F),
                        RoundedCornerShape(size = 12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (startScanner) {
                    QrScanner(
                        modifier = Modifier
                            .clipToBounds()
                            .clip(shape = RoundedCornerShape(size = 14.dp)),
                        flashlightOn = false,
                        launchGallery = false,
                        onCompletion = {
                            encodedString = it
                            startScanner = false

                            authViewModel.onAuthUiEvent(AuthUiEvent.LogInWithQrCode(encoded = it))
                        },
                        onGalleryCallBackHandler = {},
                        onFailure = {
                            coroutineScope.launch {
                                if (it.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "No valid Qr Code detected",
                                        LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(context, it, LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
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

            Spacer(modifier = Modifier.height(42.dp))

            Box(
                modifier = Modifier.width(250.dp),
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
                modifier = Modifier.widthIn(min = 250.dp),
                onClick = {
                    authNavigator.gotoLoginWithUsername()
                }
            ) {
                Text(text = "Login With Username")
            }
        }
    }
}
