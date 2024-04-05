package com.android.swingmusic.network.data.api

import com.android.swingmusic.core.data.dto.FoldersAndTracksDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.core.data.dto.RootDirsDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/notsettings/get-root-dirs")
    suspend fun getRootDirectories(): RootDirsDto

    @POST("/folder")
    suspend fun getFoldersAndTracks(@Body requestData: FoldersAndTracksRequestDto): FoldersAndTracksDto
}
