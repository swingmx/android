package com.android.swingmusic.auth.data.util

sealed class Resource<T>(val data: T? = null, val message: String? = null){
    class Loading<T>: Resource<T>()
    class Success<T>(data: T): Resource<T>(data)
    class Error<T>(data: T? = null, message: String): Resource<T>(data, message)
}
