package com.wafflestudio.internhasha.auth.dto

data class UserWithAccessToken(
    val user: UserBrief,
    val token: String,
)
