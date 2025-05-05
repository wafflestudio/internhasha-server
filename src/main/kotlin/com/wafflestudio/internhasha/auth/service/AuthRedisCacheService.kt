package com.wafflestudio.internhasha.auth.service

import com.wafflestudio.internhasha.auth.utils.UserTokenUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class AuthRedisCacheService(
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${auth.redis.prefix}") private val profilePrefix: String,
) {
    companion object {
        const val PREFIX_REFRESH_TOKEN_BY_USER_ID = "auth:refreshToken:byUserId"
        const val PREFIX_USER_ID_BY_REFRESH_TOKEN = "auth:userId:byRefreshToken"
        const val PREFIX_EMAIL_CODE_BY_SNUMAIL = "signup:emailCode:bySnuMail"
        const val PREFIX_EMAIL_CODE_SUCCESS = "signup:emailCode:success"
    }

    private fun fullKey(prefix: String): String {
        return "$profilePrefix:$prefix"
    }

    private val logger: Logger = LoggerFactory.getLogger(AuthRedisCacheService::class.java)
    //
    // AUTHENTICATION
    //

    // (userId: refreshToken), (refreshToken: userId) 둘 다 저장
    fun saveRefreshToken(
        userId: String,
        refreshToken: String,
    ) {
        // 기존 동일 userId의 토큰 쌍 삭제
        deleteRefreshTokenByUserId(userId)

        // userId -> refreshToken 저장
        val userIdKey = fullKey("$PREFIX_REFRESH_TOKEN_BY_USER_ID:$userId")
        redisTemplate.opsForValue().set(
            userIdKey,
            refreshToken,
            UserTokenUtil.refreshTokenExpirationTime * 1000,
            TimeUnit.MILLISECONDS,
        )

        // refreshToken -> userId 저장
        val tokenKey = fullKey("$PREFIX_USER_ID_BY_REFRESH_TOKEN:$refreshToken")
        redisTemplate.opsForValue().set(
            tokenKey,
            userId,
            UserTokenUtil.refreshTokenExpirationTime * 1000,
            TimeUnit.MILLISECONDS,
        )
    }

    // Refresh Token 조회(계정ID)
    fun getRefreshTokenByUserId(userId: String): String? {
        val userIdKey = fullKey("$PREFIX_REFRESH_TOKEN_BY_USER_ID:$userId")
        return redisTemplate.opsForValue().get(userIdKey)
    }

    // Refresh Token 조회(token값)
    fun getUserIdByRefreshToken(refreshToken: String): String? {
        return redisTemplate.opsForValue().get(fullKey("$PREFIX_USER_ID_BY_REFRESH_TOKEN:$refreshToken"))
    }

    // Refresh Token 삭제
    // pair 모두 삭제
    fun deleteRefreshTokenByUserId(userId: String): Boolean {
        val userIdKey = fullKey("$PREFIX_REFRESH_TOKEN_BY_USER_ID:$userId")
        val refreshToken = redisTemplate.opsForValue().get(userIdKey) // 기존 refreshToken 조회
        val tokenKey = fullKey("$PREFIX_USER_ID_BY_REFRESH_TOKEN:$refreshToken")

        val userKeyDeleted = redisTemplate.delete(userIdKey)
        val tokenKeyDeleted = if (refreshToken != null) redisTemplate.delete(tokenKey) else false

        return userKeyDeleted && tokenKeyDeleted
    }

    // Refresh Token 유효성 검증
    fun isTokenValid(
        userId: String,
        token: String,
    ): Boolean {
        val userIdKey = fullKey("$PREFIX_REFRESH_TOKEN_BY_USER_ID:$userId")
        val savedToken = redisTemplate.opsForValue().get(userIdKey)
        return savedToken == token
    }

    //
    // SIGNUP
    //

    // 이메일 인증 코드(해시) 저장
    fun saveEmailCode(
        snuMail: String,
        emailCode: String,
    ) {
        // 기존 동일 스누메일의 email token 삭제
        deleteEmailCode(snuMail)

        val key = fullKey("$PREFIX_EMAIL_CODE_BY_SNUMAIL:$snuMail")
        redisTemplate.opsForValue().set(
            key,
            emailCode,
            UserTokenUtil.emailTokenExpirationTime * 1000,
            TimeUnit.MILLISECONDS,
        )
    }

    // 이메일 인증 토큰 조회
    fun getEmailCode(snuMail: String): String? {
        val key = fullKey("$PREFIX_EMAIL_CODE_BY_SNUMAIL:$snuMail")
        return redisTemplate.opsForValue().get(key)
    }

    // 이메일 인증 토큰 삭제
    fun deleteEmailCode(snuMail: String): Boolean {
        val key = fullKey("$PREFIX_EMAIL_CODE_BY_SNUMAIL:$snuMail")
        return redisTemplate.delete(key)
    }

    // 이메일 인증 성공 코드 저장
    fun saveSuccessCode(
        successCode: String,
    ) {
        val key = fullKey("$PREFIX_EMAIL_CODE_SUCCESS:$successCode")
        redisTemplate.opsForValue().set(
            key,
            true.toString(),
            UserTokenUtil.emailTokenExpirationTime * 1000,
            TimeUnit.MILLISECONDS,
        )
    }

    // 이메일 인증 성공 토큰 조회
    fun getSuccessCode(successCode: String): Boolean {
        val key = fullKey("$PREFIX_EMAIL_CODE_SUCCESS:$successCode")
        return redisTemplate.hasKey(key)
    }

    // 이메일 인증 성공 토큰 삭제
    fun deleteSuccessCode(successCode: String): Boolean {
        val key = fullKey("$PREFIX_EMAIL_CODE_SUCCESS:$successCode")
        return redisTemplate.delete(key)
    }
}
