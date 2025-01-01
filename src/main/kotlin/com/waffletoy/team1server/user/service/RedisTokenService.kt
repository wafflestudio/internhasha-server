package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.user.UserTokenUtil
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisTokenService(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    // Refresh Token 저장
    fun saveRefreshToken(
        userId: String,
        refreshToken: String,
    ) {
        val key = "refreshToken:$userId"
        redisTemplate.opsForValue().set(
            key,
            refreshToken,
            UserTokenUtil.refreshTokenExpirationTime,
            TimeUnit.MILLISECONDS,
        )
    }

    // Refresh Token 조회(유저ID)
    fun getRefreshToken(userId: String): String? {
        val key = "refreshToken:$userId"
        return redisTemplate.opsForValue().get(key)
    }

    // Refresh Token 조회(token값)
    fun getUserIdByRefreshToken(refreshToken: String): String? {
        return redisTemplate.opsForValue().get("refreshToken:$refreshToken")
    }

    // Refresh Token 삭제
    fun deleteRefreshToken(userId: String): Boolean {
        val key = "refreshToken:$userId"
        return redisTemplate.delete(key)
    }

    // Refresh Token 유효성 검증
    fun isTokenValid(
        userId: String,
        token: String,
    ): Boolean {
        val key = "refreshToken:$userId"
        val savedToken = redisTemplate.opsForValue().get(key)
        return savedToken == token
    }

    // 이메일 인증 토큰 저장
    fun saveEmailToken(
        userId: String,
        emailToken: String,
    ) {
        val key = "emailToken:$userId"
        redisTemplate.opsForValue().set(
            key,
            emailToken,
            UserTokenUtil.emailTokenExpirationTime,
            TimeUnit.MILLISECONDS,
        )
    }

    // 이메일 인증 토큰 조회
    fun getEmailToken(userId: String): String? {
        val key = "emailToken:$userId"
        return redisTemplate.opsForValue().get(key)
    }

    // 이메일 인증 토큰 삭제
    fun deleteEmailToken(userId: String): Boolean {
        val key = "emailToken:$userId"
        return redisTemplate.delete(key)
    }
}
