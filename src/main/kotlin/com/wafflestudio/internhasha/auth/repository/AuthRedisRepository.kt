package com.wafflestudio.internhasha.auth.repository

import com.wafflestudio.internhasha.auth.security.TokenExpirationTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.TimeUnit

@Component
class AuthRedisRepository(
    private val redisTemplate: RedisTemplate<String, String>,
    @param:Value("\${auth.redis.prefix}") private val profilePrefix: String,
) {
    // UserId를 키로 사용 / refreshToken을 value로 사용
    fun saveRefreshToken(
        userId: String,
        refreshToken: String,
    ) {
        val key = generateKey(PREFIX_REFRESH_TOKEN, userId)
        redisTemplate.opsForValue().set(
            key,
            refreshToken,
            TokenExpirationTime.REFRESH_TOKEN_EXPIRATION_TIME,
            TimeUnit.SECONDS,
        )
    }

    fun getRefreshToken(userId: String): String? {
        val key = generateKey(PREFIX_REFRESH_TOKEN, userId)
        return redisTemplate.opsForValue().get(key)
    }

    fun deleteRefreshToken(
        userId: String,
    ): Boolean {
        val key = generateKey(PREFIX_REFRESH_TOKEN, userId)
        val result = redisTemplate.delete(key)
        return result
    }

    fun saveEmailCode(
        email: String,
        emailCode: String,
    ) {
        // 기존 이메일 코드는 덮어쓰기 수행됨
        val key = generateKey(PREFIX_EMAIL_CODE, email)
        redisTemplate.opsForValue().set(
            key,
            hashToken(emailCode),
            TokenExpirationTime.EMAIL_TOKEN_EXPIRATION_TIME,
            TimeUnit.SECONDS,
        )
    }

    fun verifyEmailCode(
        email: String,
        emailCode: String,
    ): Boolean {
        val key = generateKey(PREFIX_EMAIL_CODE, email)
        val hashedCode = redisTemplate.opsForValue().get(key)
        return hashedCode == hashToken(emailCode)
    }

    fun deleteEmailCode(email: String): Boolean {
        val key = generateKey(PREFIX_EMAIL_CODE, email)
        return redisTemplate.delete(key)
    }

    fun saveSuccessCode(
        email: String,
        successCode: String,
    ) {
        val key = generateKey(PREFIX_VERIFICATION_SUCCESS, email)
        redisTemplate.opsForValue().set(
            key,
            hashToken(successCode),
            TokenExpirationTime.ACCESS_TOKEN_EXPIRATION_TIME,
            TimeUnit.SECONDS,
        )
    }

    fun verifySuccessCode(
        email: String,
        successCode: String,
    ): Boolean {
        val key = generateKey(PREFIX_VERIFICATION_SUCCESS, email)
        val hashedCode = redisTemplate.opsForValue().get(key)
        return hashedCode == hashToken(successCode)
    }

    fun deleteSuccessCode(email: String): Boolean {
        val key = generateKey(PREFIX_VERIFICATION_SUCCESS, email)
        return redisTemplate.delete(key)
    }

    companion object {
        const val PREFIX_REFRESH_TOKEN = "auth:refreshToken"
        const val PREFIX_EMAIL_CODE = "signup:emailCode"
        const val PREFIX_VERIFICATION_SUCCESS = "signup:verificationSuccess"
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(token.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private fun generateKey(vararg parts: Any): String = "$profilePrefix:${parts.joinToString(":")}"
}
