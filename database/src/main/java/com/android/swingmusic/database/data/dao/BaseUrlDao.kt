package com.android.swingmusic.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.swingmusic.database.data.entity.BaseUrlEntity

@Dao
interface BaseUrlDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBaseUrl(baseUrl: BaseUrlEntity)

    @Query("SELECT * FROM base_url LIMIT 1")
    suspend fun getBaseUrl(): BaseUrlEntity?

    @Query("DELETE FROM base_url")
    fun clearBaseUrl()
}
