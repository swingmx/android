package com.android.swingmusic.presentation.navigator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination

/**
 * Empty base entry under every tab. With all four tab roots above it, each tab's history can be
 * saved and restored the same way, Home included. The activity opens Home on top of it right away.
 */
@Destination
@Composable
fun TabsRootScreen() {
    Box(modifier = Modifier.fillMaxSize())
}
