package com.android.swingmusic.auth.domain.model

import com.android.swingmusic.database.domain.model.User

data class AllUsers(
    val profileSettings: ProfileSettings,
    val users: List<User>
)
