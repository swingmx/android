package com.android.swingmusic.profile.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.profile.presentation.component.ProfileTopBar
import com.android.swingmusic.profile.presentation.event.PairDeviceUiEffect
import com.android.swingmusic.profile.presentation.event.PairDeviceUiEvent
import com.android.swingmusic.profile.presentation.state.PairDeviceUiState
import com.android.swingmusic.profile.presentation.state.displayName
import com.android.swingmusic.profile.presentation.state.qrPayload
import com.android.swingmusic.profile.presentation.state.serverHost
import com.android.swingmusic.profile.presentation.viewmodel.PairDeviceViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination
import qrgenerator.qrkitpainter.QrKitBrush
import qrgenerator.qrkitpainter.rememberQrKitPainter
import qrgenerator.qrkitpainter.solidBrush

@Destination
@Composable
internal fun PairDeviceScreen(
    navigator: CommonNavigator,
    viewModel: PairDeviceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            PairDeviceUiEffect.NavigateBack -> navigator.navigateBack()
        }
    }

    PairDeviceScreenContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun PairDeviceScreenContent(
    uiState: PairDeviceUiState,
    onEvent: (PairDeviceUiEvent) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ProfileTopBar(
                title = "Pair Another Device",
                onBack = { onEvent(PairDeviceUiEvent.OnBackClicked) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    append("On the other device, open Swing Music and tap ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("TAP TO SCAN") }
                    if (uiState.username.isNotEmpty()) {
                        append(". It signs in as ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(displayName(uiState.username)) }
                    }
                    append(".")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F),
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .size(248.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (uiState.qrPayload != null) Color.White else MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                val payload = uiState.qrPayload
                when {
                    uiState.isLoadingCode -> CircularProgressIndicator()

                    payload != null -> Image(
                        painter = rememberQrKitPainter(data = payload) {
                            colors = colors.copy(darkBrush = QrKitBrush.solidBrush(Color.Black))
                        },
                        contentDescription = "Pairing QR code",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    )

                    else -> Text(
                        text = uiState.errorLoadingCode ?: "",
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            uiState.code?.let { code ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "CODE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = .4.sp
                    )
                    Text(
                        text = code,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 6.sp
                    )
                    Text(
                        text = "Works once · ${serverHost(uiState.baseUrl)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = { onEvent(PairDeviceUiEvent.OnNewCodeClicked) },
                enabled = !uiState.isLoadingCode,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .3F))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(if (uiState.code == null && !uiState.isLoadingCode) "Try again" else "New code")
            }

            Spacer(modifier = Modifier.height(140.dp).fillMaxWidth())
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, heightDp = 800)
@Composable
private fun PairDeviceScreenContentPreview() {
    SwingMusicTheme {
        PairDeviceScreenContent(
            uiState = PairDeviceUiState(
                baseUrl = "https://neon.example.com/",
                username = "eric",
                isLoadingCode = false,
                code = "A7K2QX"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, heightDp = 800)
@Composable
private fun PairDeviceScreenErrorPreview() {
    SwingMusicTheme {
        PairDeviceScreenContent(
            uiState = PairDeviceUiState(
                baseUrl = "https://neon.example.com/",
                isLoadingCode = false,
                errorLoadingCode = "Check your connection to the server."
            ),
            onEvent = {}
        )
    }
}
