package com.wafflestudio.internhasha.auth.security

import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.service.dto.Tokens
import com.wafflestudio.internhasha.auth.service.dto.UserSimple
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*

@Component
class TokenProvider(
    @Value("\${custom.TOKEN_PRIVATE_KEY}") private val tokenPrivateKey: String,
) {
    private val secretKey =
        Keys.hmacShaKeyFor(
            tokenPrivateKey.toByteArray(StandardCharsets.UTF_8),
        )

    // 토큰 생성(access & refresh)
    fun generateTokens(user: UserSimple): Tokens {
        return Tokens(
            generateAccessToken(user),
            generateRefreshToken(user),
        )
    }

    private fun generateAccessToken(user: UserSimple): String {
        val now = Instant.now()
        return Jwts.builder()
            .setSubject(user.id)
            .claim("role", user.userRole)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(TokenExpirationTime.ACCESS_TOKEN_EXPIRATION_TIME)))
            .signWith(secretKey)
            .compact()
    }

    private fun generateRefreshToken(user: UserSimple): String {
        val now = Instant.now()
        return Jwts.builder()
            .setSubject(user.id)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(TokenExpirationTime.REFRESH_TOKEN_EXPIRATION_TIME)))
            .signWith(secretKey)
            .compact()
    }

    fun getUserIdFromToken(token: String): String? {
        return getClaim(token)?.subject
    }

    fun getUserRoleFromToken(token: String): UserRole? {
        val roleString = getClaim(token)?.get("role", String::class.java) ?: return null
        return UserRole.entries.find { it.value == roleString }
    }

    // JWT 토큰 파싱으로 유효성 검증, 유저 식별
    private fun getClaim(token: String): Claims? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: Exception) {
            null // 유효하지 않거나 만료된 경우
        }
    }
}
