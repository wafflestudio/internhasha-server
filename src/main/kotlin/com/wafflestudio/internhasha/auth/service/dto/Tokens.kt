package com.wafflestudio.internhasha.auth.service.dto

data class Tokens(
    val accessToken: String,
    val refreshToken: String,
)
