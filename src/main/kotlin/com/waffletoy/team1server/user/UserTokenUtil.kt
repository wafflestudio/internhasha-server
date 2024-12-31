package com.waffletoy.team1server.user

import com.waffletoy.team1server.user.persistence.UserEntity
import com.waffletoy.team1server.user.persistence.UserRepository
import io.github.cdimascio.dotenv.Dotenv
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
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

    /**
     * Refresh Token이 만료되었는지 확인
     * @param refreshToken 검증할 Refresh Token
     * @return Boolean - true: 만료됨, false: 유효함
     */
    fun isRefreshTokenExpired(refreshToken: String): Boolean {
        return try {
            // 토큰에서 만료 시간 추출
            val expirationInstant = getExpirationInstantFromToken(refreshToken)

            // 현재 시간과 비교
            expirationInstant.isBefore(Instant.now())
        } catch (e: Exception) {
            // 파싱 실패 시 만료된 것으로 간주
            true
        }
    }

    /**
     * Refresh Token에서 만료 시간을 추출
     * @param token JWT 형식의 Refresh Token
     * @return Instant - 만료 시간
     */
    private fun getExpirationInstantFromToken(token: String): Instant {
        val claims = parseToken(token)
        return claims.expiration.toInstant()
    }

    /**
     * 토큰을 파싱하여 클레임 반환
     * @param token JWT 형식의 토큰
     * @return Claims - JWT Claims 객체
     */
    private fun parseToken(token: String): Claims {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: SignatureException) {
            throw IllegalArgumentException("Invalid token signature")
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse token")
        }
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
