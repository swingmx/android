package com.android.swingmusic.auth.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.android.swingmusic.auth.presentation.event.AuthUiEvent
import com.android.swingmusic.auth.presentation.navigation.AuthNavigator
import com.android.swingmusic.auth.presentation.state.AuthState
import com.android.swingmusic.auth.presentation.util.AuthError
import com.android.swingmusic.auth.presentation.viewmodel.AuthViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme_Preview
import com.ramcosta.composedestinations.annotation.Destination

@Composable
private fun LoginWithUsername(
    baseUrl: String,
    username: String,
    password: String,
    statusText: String,
    statusTextColor: Color,
    onBaseUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClickBack: () -> Unit,
    onClickLogIn: () -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onClickBack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Arrow Back"
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    style = MaterialTheme.typography.headlineSmall,
                    text = "Login"
                )
            }

        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxHeight(.9F)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .clip(CircleShape)
                        .size(80.dp),
                    painter = painterResource(id = R.drawable.swing_music_logo_round_outlined),
                    contentDescription = "App Logo"
                )

                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(.84F)
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(10))
                        .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = .3F))
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        modifier = Modifier.fillMaxWidth(.85F),
                        value = baseUrl,
                        onValueChange = {
                            onBaseUrlChange(it)
                        },
                        shape = RoundedCornerShape(16),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Type or paste the server url",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5F)
                            )
                        },
                        visualTransformation = VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = colorScheme.onPrimaryContainer.copy(alpha = .1F),
                            unfocusedContainerColor = colorScheme.onPrimaryContainer.copy(alpha = .1F),
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        modifier = Modifier.fillMaxWidth(.85F),
                        value = username,
                        onValueChange = {
                            onUsernameChange(it)
                        },
                        shape = RoundedCornerShape(16),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Username",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5F)
                            )
                        },
                        visualTransformation = VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = colorScheme.onPrimaryContainer.copy(alpha = .1F),
                            unfocusedContainerColor = colorScheme.onPrimaryContainer.copy(alpha = .1F),
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        modifier = Modifier.fillMaxWidth(.85F),
                        value = password,
                        onValueChange = {
                            onPasswordChange(it)
                        },
                        shape = RoundedCornerShape(16),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Password",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5F)
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = colorScheme.onPrimaryContainer.copy(alpha = .1F),
                            unfocusedContainerColor = colorScheme.onPrimaryContainer.copy(alpha = .1F),
                        )
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth(.85F)
                            .heightIn(min = 46.dp),
                        onClick = { onClickLogIn() }) {
                        Text(text = "Login")
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
            }
        }
    }
}

@Destination
@Composable
fun LoginWithUsernameScreen(
    authViewModel: AuthViewModel,
    authNavigator: AuthNavigator
) {
    val authUiState by authViewModel.authUiState.collectAsState()
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

        is AuthError.InputError -> {
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

    LoginWithUsername(
        baseUrl = authUiState.baseUrl ?: "",
        username = authUiState.username ?: "",
        password = authUiState.password ?: "",
        statusText = statusText,
        statusTextColor = statusTextColor,
        onBaseUrlChange = {
            authViewModel.onAuthUiEvent(AuthUiEvent.OnBaseUrlChange(it))
        },
        onUsernameChange = {
            authViewModel.onAuthUiEvent(AuthUiEvent.OnUsernameChange(it))
        },
        onPasswordChange = {
            authViewModel.onAuthUiEvent(AuthUiEvent.OnPasswordChange(it))
        },
        onClickBack = { authNavigator.gotoLoginWithQrCode() },
        onClickLogIn = { authViewModel.onAuthUiEvent(AuthUiEvent.LogInWithUsernameAndPassword) }
    )
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Composable
fun LoginWithUsernamePreview() {
    SwingMusicTheme_Preview {
        var baseUrl = remember { "" }
        var username = remember { "" }
        var password = remember { "" }

        LoginWithUsername(
            baseUrl = baseUrl,
            username = username,
            password = password,
            statusText = "",
            statusTextColor = Color.Unspecified,
            onBaseUrlChange = { baseUrl = it },
            onUsernameChange = { username = it },
            onPasswordChange = { password = it },
            onClickBack = {},
            onClickLogIn = {}
        )
    }
}
