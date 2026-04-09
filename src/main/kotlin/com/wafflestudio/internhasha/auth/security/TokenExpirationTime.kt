package com.wafflestudio.internhasha.auth.security

object TokenExpirationTime {
    const val ACCESS_TOKEN_EXPIRATION_TIME = 3600L // 1시간 (초 단위)
    const val REFRESH_TOKEN_EXPIRATION_TIME = 7 * 24 * 3600L // 7일 (초 단위)
    const val EMAIL_TOKEN_EXPIRATION_TIME = 180L // 3분 (초 단위)
}
