package com.wafflestudio.internhasha.auth.web.controller

import com.wafflestudio.internhasha.auth.domain.Token
import com.wafflestudio.internhasha.auth.domain.Tokens
import com.wafflestudio.internhasha.auth.domain.UserSimple
import com.wafflestudio.internhasha.auth.domain.UserWithAccessToken
import com.wafflestudio.internhasha.auth.security.TokenExpirationTime
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity

object UserTokenResponseFactory {
    fun buildUserWithTokensResponse(
        userSimple: UserSimple,
        tokens: Tokens,
        isSecure: Boolean = true,
    ): ResponseEntity<UserWithAccessToken> {
        val refreshTokenCookie = createRefreshTokenCookie(tokens.refreshToken, isSecure = isSecure)

        return ResponseEntity.ok()
            .header("Set-Cookie", refreshTokenCookie.toString())
            .body(
                UserWithAccessToken(
                    user = userSimple,
                    token = tokens.accessToken,
                ),
            )
    }

    fun buildTokensResponse(
        tokens: Tokens,
        isSecure: Boolean = true,
    ): ResponseEntity<Token> {
        val refreshTokenCookie = createRefreshTokenCookie(tokens.refreshToken, isSecure = isSecure)
        return ResponseEntity.ok()
            .header("Set-Cookie", refreshTokenCookie.toString())
            .body(
                Token(
                    accessToken = tokens.accessToken,
                ),
            )
    }

    fun buildDeleteTokenResponse(
        isSecure: Boolean = true,
    ): ResponseEntity<Void> {
        val deleteCookie = createEmptyRefreshTokenCookie(isSecure = isSecure)
        return ResponseEntity.ok()
            .header("Set-Cookie", deleteCookie.toString())
            .build()
    }

    fun createRefreshTokenCookie(
        refreshToken: String,
        isSecure: Boolean = true,
    ): ResponseCookie {
        return ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(isSecure) // HTTPS 사용 시 활성화
            .path("/")
            .maxAge(TokenExpirationTime.REFRESH_TOKEN_EXPIRATION_TIME) // 7일
            .sameSite("Lax")
            .build()
    }

    fun createEmptyRefreshTokenCookie(isSecure: Boolean = true): ResponseCookie {
        return ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .secure(isSecure)
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build()
    }
}
