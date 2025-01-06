package com.waffletoy.team1server.account.service

import com.waffletoy.team1server.account.AccountTokenUtil
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisTokenService(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    // Refresh Token 저장
    // userId -> token
    // token -> userId
    // pair를 모두 저장
    fun saveRefreshToken(
        userId: String,
        refreshToken: String,
    ) {
        // 기존 동일 userId의 토큰 쌍 삭제
        deleteRefreshTokenByUserId(userId)

        // userId -> refreshToken 저장
        val userKey = "userRefreshToken:$userId"
        redisTemplate.opsForValue().set(
            userKey,
            refreshToken,
            AccountTokenUtil.refreshTokenExpirationTime * 1000,
            TimeUnit.MILLISECONDS,
        )

        // refreshToken -> userId 저장
        val tokenKey = "refreshToken:$refreshToken"
        redisTemplate.opsForValue().set(
            tokenKey,
            userId,
            AccountTokenUtil.refreshTokenExpirationTime * 1000,
            TimeUnit.MILLISECONDS,
        )
    }

    // Refresh Token 조회(유저ID)
    fun getRefreshTokenByUserId(userId: String): String? {
        val key = "refreshToken:$userId"
        return redisTemplate.opsForValue().get(key)
    }

    // Refresh Token 조회(token값)
    fun getUserIdByRefreshToken(refreshToken: String): String? {
        return redisTemplate.opsForValue().get("refreshToken:$refreshToken")
    }

    // Refresh Token 삭제
    // pair 모두 삭제
    fun deleteRefreshTokenByUserId(userId: String): Boolean {
        val userKey = "userRefreshToken:$userId"
        val refreshToken = redisTemplate.opsForValue().get(userKey) // 기존 refreshToken 조회
        val tokenKey = "refreshToken:$refreshToken"

        val userKeyDeleted = redisTemplate.delete(userKey)
        val tokenKeyDeleted = if (refreshToken != null) redisTemplate.delete(tokenKey) else false

        return userKeyDeleted && tokenKeyDeleted
    }

    // Refresh Token 유효성 검증
    fun isTokenValid(
        userId: String,
        token: String,
    ): Boolean {
        val key = "userRefreshToken:$userId"
        val savedToken = redisTemplate.opsForValue().get(key)
        return savedToken == token
    }

    // 이메일 인증 코드(해시) 저장
    fun saveEmailCode(
        snuMail: String,
        emailCode: String,
    ) {
        // 기존 동일 스누메일의 email token 삭제
        deleteEmailCode(snuMail)

        val key = "emailToken:$snuMail"
        redisTemplate.opsForValue().set(
            key,
            emailCode,
            AccountTokenUtil.emailTokenExpirationTime * 1000,
            TimeUnit.MILLISECONDS,
        )
    }

    // 이메일 인증 토큰 조회
    fun getEmailCode(snuMail: String): String? {
        val key = "emailToken:$snuMail"
        return redisTemplate.opsForValue().get(key)
    }

    // 이메일 인증 토큰 삭제
    fun deleteEmailCode(snuMail: String): Boolean {
        val key = "emailToken:$String"
        return redisTemplate.delete(key)
    }

    // db 리셋
    fun deleteAllKeys() {
        val patterns =
            listOf(
                "userRefreshToken:*",
                "refreshToken:*",
                "emailToken:*",
            )

        patterns.forEach { pattern ->
            val keys = redisTemplate.keys(pattern) // 해당 패턴에 맞는 키 검색
            if (keys.isNotEmpty()) {
                redisTemplate.delete(keys) // 검색된 모든 키 삭제
            }
        }
    }
}
