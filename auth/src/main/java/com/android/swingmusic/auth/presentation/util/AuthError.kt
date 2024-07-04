package com.android.swingmusic.auth.presentation.util

interface AuthError {

    data class InputError(val msg: String) : AuthError

    data class LoginError(val msg: String) : AuthError

    object None : AuthError
}
