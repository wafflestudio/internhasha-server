package com.waffletoy.team1server.user.dtos

data class UserWithAccessToken(
    val user: User,
    val token: String,
)
