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
            UserTokenUtil.refreshTokenExpirationTime * 1000,
            TimeUnit.MILLISECONDS,
        )

        // refreshToken -> userId 저장
        val tokenKey = "refreshToken:$refreshToken"
        redisTemplate.opsForValue().set(
            tokenKey,
            userId,
            UserTokenUtil.refreshTokenExpirationTime * 1000,
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

    // 이메일 인증 토큰 저장 (email token을 key로, userId를 value로 저장)
    fun saveEmailToken(
        userId: String,
        emailToken: String,
    ) {
        deleteEmailTokenByUserId(userId) // 기존 동일 userId의 email token 삭제

        val key = "emailToken:$emailToken"
        redisTemplate.opsForValue().set(
            key,
            userId,
            UserTokenUtil.emailTokenExpirationTime * 1000,
            TimeUnit.MILLISECONDS,
        )

        val userKey = "emailUserToken:$userId"
        redisTemplate.opsForValue().set(
            userKey,
            emailToken,
            UserTokenUtil.emailTokenExpirationTime * 1000,
            TimeUnit.MILLISECONDS,
        )
    }

    // 이메일 인증 토큰 조회(token값)
    fun getUserIdByEmailToken(token: String): String? {
        val key = "emailToken:$token"
        return redisTemplate.opsForValue().get(key)
    }

    // 이메일 인증 토큰 조회(userId)
    fun getEmailTokenByUserId(userId: String): String? {
        val key = "emailUserToken:$userId"
        return redisTemplate.opsForValue().get(key)
    }

    // 이메일 인증 토큰 삭제
    fun deleteEmailTokenByToken(token: String): Boolean {
        val key = "emailToken:$token"
        return redisTemplate.delete(key)
    }

    // 이메일 인증 토큰 삭제(userId)
    fun deleteEmailTokenByUserId(userId: String): Boolean {
        val userKey = "emailUserToken:$userId"
        val emailToken = redisTemplate.opsForValue().get(userKey) // 기존 emailToken 조회
        val tokenKey = "emailToken:$emailToken"

        val userKeyDeleted = redisTemplate.delete(userKey)
        val tokenKeyDeleted = if (emailToken != null) redisTemplate.delete(tokenKey) else false

        return userKeyDeleted && tokenKeyDeleted
    }
}
