package com.waffletoy.team1server.auth.dto

data class UserWithAccessToken(
    val user: UserBrief,
    val token: String,
)
