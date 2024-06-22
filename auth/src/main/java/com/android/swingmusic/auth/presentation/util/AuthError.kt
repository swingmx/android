package com.android.swingmusic.auth.presentation.util

interface AuthError {

    data class UrlInputError(val msg: String) : AuthError

    data class LoginError(val msg: String) : AuthError

    object None : AuthError
}
