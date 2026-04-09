package com.wafflestudio.internhasha.auth.domain

data class UserWithAccessToken(
    val user: UserSimple,
    val token: String,
)
