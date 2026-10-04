package com.android.swingmusic.profile.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.profile.presentation.component.ProfileSnackbarHost
import com.android.swingmusic.profile.presentation.component.ProfileTextField
import com.android.swingmusic.profile.presentation.component.ProfileTopBar
import com.android.swingmusic.profile.presentation.event.AccountUiEffect
import com.android.swingmusic.profile.presentation.event.AccountUiEvent
import com.android.swingmusic.profile.presentation.state.AccountUiState
import com.android.swingmusic.profile.presentation.state.canSaveUsername
import com.android.swingmusic.profile.presentation.state.canUpdatePassword
import com.android.swingmusic.profile.presentation.state.passwordsMismatch
import com.android.swingmusic.profile.presentation.viewmodel.AccountViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@Destination
@Composable
internal fun AccountScreen(
    navigator: CommonNavigator,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            AccountUiEffect.NavigateBack -> navigator.navigateBack()
            is AccountUiEffect.ShowSnackBar -> scope.launch {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    AccountScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun AccountScreenContent(
    uiState: AccountUiState,
    onEvent: (AccountUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { ProfileSnackbarHost(snackbarHostState) },
        topBar = { ProfileTopBar(title = "Account", onBack = { onEvent(AccountUiEvent.OnBackClicked) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("Username")
                ProfileTextField(
                    value = uiState.username,
                    onValueChange = { onEvent(AccountUiEvent.OnUsernameChange(it)) },
                    placeholder = "Username",
                    errorText = uiState.usernameError
                )
                SaveButton(
                    text = "Save",
                    enabled = uiState.canSaveUsername,
                    isSaving = uiState.isSavingUsername,
                    onClick = { onEvent(AccountUiEvent.OnSaveUsername) },
                    modifier = Modifier.align(Alignment.End)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("Change password")
                ProfileTextField(
                    value = uiState.newPassword,
                    onValueChange = { onEvent(AccountUiEvent.OnNewPasswordChange(it)) },
                    placeholder = "New password",
                    isPassword = true,
                    imeAction = ImeAction.Next
                )
                ProfileTextField(
                    value = uiState.confirmPassword,
                    onValueChange = { onEvent(AccountUiEvent.OnConfirmPasswordChange(it)) },
                    placeholder = "Confirm password",
                    isPassword = true,
                    errorText = when {
                        uiState.passwordsMismatch -> "Passwords don't match"
                        else -> uiState.passwordError
                    }
                )
                SaveButton(
                    text = "Update password",
                    enabled = uiState.canUpdatePassword,
                    isSaving = uiState.isSavingPassword,
                    onClick = { onEvent(AccountUiEvent.OnUpdatePassword) },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SaveButton(
    text: String,
    enabled: Boolean,
    isSaving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier) {
        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Text(text)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun AccountScreenContentPreview() {
    SwingMusicTheme {
        AccountScreenContent(
            uiState = AccountUiState(
                currentUsername = "eric",
                username = "eric",
                newPassword = "hunter2",
                confirmPassword = "hunter22"
            ),
            onEvent = {}
        )
    }
}
