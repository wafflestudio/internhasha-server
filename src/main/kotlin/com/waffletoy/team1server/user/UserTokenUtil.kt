package com.waffletoy.team1server.user

import com.waffletoy.team1server.user.persistence.UserEntity
import com.waffletoy.team1server.user.persistence.UserRepository
import io.github.cdimascio.dotenv.Dotenv
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date

object UserTokenUtil {
    data class Tokens(
        val accessToken: String,
        val refreshToken: String,
    )

    // 토큰 생성(access & refresh 쌍)
    fun generateTokens(
        userEntity: UserEntity,
        userRepository: UserRepository,
    ): Tokens {
        val now = Instant.now()
        val accessExpiryDate = Date.from(now.plusSeconds(ACCESS_TOKEN_EXPIRATION_TIME.toLong()))
        val refreshExpiryDate = now.plusSeconds(REFRESH_TOKEN_EXPIRATION_TIME)

        val accessToken =
            Jwts.builder()
                .signWith(SECRET_KEY)
                .setSubject(userEntity.id.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(accessExpiryDate)
                .compact()

        val refreshToken =
            Jwts.builder()
                .signWith(SECRET_KEY)
                .setSubject(userEntity.id.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(refreshExpiryDate))
                .compact()

        // 기존 Refresh Token 갱신
        userEntity.refreshToken = refreshToken
        userEntity.refreshTokenExpiresAt = refreshExpiryDate
        userRepository.save(userEntity)

        return Tokens(accessToken, refreshToken)
    }

    // Access Token 유효성 검증
    fun validateAccessTokenGetUserId(token: String): String? {
        return try {
            val claims =
                Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .body
            if (claims.expiration < Date()) {
                null
            } else {
                claims.subject
            }
        } catch (e: Exception) {
            null
        }
    }

    // Refresh Token 유효성 검증
    fun validateRefreshToken(
        refreshToken: String,
        userRepository: UserRepository,
    ): Boolean {
        val now = Instant.now()

        // Refresh Token이 유효한지 확인
        val userEntity =
            userRepository.findByRefreshToken(refreshToken)
                ?: return false

        return userEntity.refreshTokenExpiresAt?.isAfter(now) == true
    }

    private const val ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 2 // 2 hours
    private const val REFRESH_TOKEN_EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 30 // 30 days
    private val dotenv = Dotenv.load()

    private val TOKEN_PRIVATE_KEY =
        dotenv["TOKEN_PRIVATE_KEY"]
            ?: System.getenv("TOKEN_PRIVATE_KEY")
            ?: throw RuntimeException("TOKEN_PRIVATE_KEY not found")

    private val SECRET_KEY =
        Keys.hmacShaKeyFor(
            TOKEN_PRIVATE_KEY.toByteArray(StandardCharsets.UTF_8),
        )
}
