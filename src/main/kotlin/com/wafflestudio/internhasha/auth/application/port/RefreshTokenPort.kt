package com.wafflestudio.internhasha.auth.application.port

interface RefreshTokenPort {
    fun saveRefreshToken(
        userId: String,
        refreshToken: String,
    )

    fun getRefreshToken(userId: String): String?

    fun deleteRefreshToken(userId: String): Boolean
}
