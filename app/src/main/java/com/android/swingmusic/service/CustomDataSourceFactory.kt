package com.android.swingmusic.service

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource

class CustomDataSourceFactory(
    private val context: Context,
    private val accessToken: String
) : DataSource.Factory {

    @UnstableApi
    override fun createDataSource(): DataSource {
        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory().apply {
            setDefaultRequestProperties(
                mapOf("Authorization" to "Bearer $accessToken")
            )
        }
        return DefaultDataSource.Factory(context, defaultHttpDataSourceFactory).createDataSource()
    }
}
