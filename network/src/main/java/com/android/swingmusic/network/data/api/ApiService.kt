package com.android.swingmusic.network.data.api

import com.android.swingmusic.core.data.dto.FoldersAndTracksDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/folder")
    suspend fun getFoldersAndTracks(@Body requestData: FoldersAndTracksRequestDto): FoldersAndTracksDto

}
