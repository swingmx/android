package com.android.swingmusic.presentation.navigator

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

private const val AnimDurationLong = 500
private const val AnimDurationShort = 300

// Enter transition when you navigate to a screen
fun scaleInEnterTransition() = scaleIn(
    initialScale = .9f,
    animationSpec = tween(AnimDurationLong)
) + fadeIn(
    animationSpec = tween(AnimDurationShort)
)

// Exit transition when you navigate to a screen
fun scaleOutExitTransition() = scaleOut(
    targetScale = 1.1f,
    animationSpec = tween(AnimDurationShort)
) + fadeOut(
    animationSpec = tween(AnimDurationShort)
)

// Enter transition of a screen when you pop to it
fun scaleInPopEnterTransition() = scaleIn(
    initialScale = 1.1f,
    animationSpec = tween(AnimDurationLong)
) + fadeIn(
    animationSpec = tween(AnimDurationShort)
)

// Exit transition of a screen you are popping from
fun scaleOutPopExitTransition() = scaleOut(
    targetScale = .9f,
    animationSpec = tween(AnimDurationShort)
) + fadeOut(
    animationSpec = tween(AnimDurationShort)
)
