package com.android.swingmusic.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.swingmusic.database.data.entity.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoggedInUser(user: UserEntity)

    @Query("SELECT * FROM logged_in_user LIMIT 1")
    suspend fun getLoggedInUser(): UserEntity?
}
