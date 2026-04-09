package com.wafflestudio.internhasha.auth.domain

data class Tokens(
    val accessToken: String,
    val refreshToken: String,
)
