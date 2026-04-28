package com.wafflestudio.internhasha.auth.controller.dto.response

import com.wafflestudio.internhasha.auth.security.TokenExpirationTime
import com.wafflestudio.internhasha.auth.service.dto.Tokens
import com.wafflestudio.internhasha.auth.service.dto.UserWithTokens
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity

object UserTokenResponseFactory {
    fun buildUserWithTokensResponse(
        userWithTokens: UserWithTokens,
        isSecure: Boolean = true,
    ): ResponseEntity<UserTokenResponse> {
        val refreshTokenCookie = createRefreshTokenCookie(userWithTokens.tokens.refreshToken, isSecure = isSecure)

        return ResponseEntity
            .ok()
            .header("Set-Cookie", refreshTokenCookie.toString())
            .body(
                UserTokenResponse(
                    user = userWithTokens.user.let { UserResponse(it.id, it.userRole) },
                    token = userWithTokens.tokens.accessToken,
                ),
            )
    }

    fun buildTokensResponse(
        tokens: Tokens,
        isSecure: Boolean = true,
    ): ResponseEntity<TokenResponse> {
        val refreshTokenCookie = createRefreshTokenCookie(tokens.refreshToken, isSecure = isSecure)
        return ResponseEntity
            .ok()
            .header("Set-Cookie", refreshTokenCookie.toString())
            .body(
                TokenResponse(
                    accessToken = tokens.accessToken,
                ),
            )
    }

    fun buildDeleteTokenResponse(
        isSecure: Boolean = true,
    ): ResponseEntity<Void> {
        val deleteCookie = createEmptyRefreshTokenCookie(isSecure = isSecure)
        return ResponseEntity
            .ok()
            .header("Set-Cookie", deleteCookie.toString())
            .build()
    }

    fun createRefreshTokenCookie(
        refreshToken: String,
        isSecure: Boolean = true,
    ): ResponseCookie =
        ResponseCookie
            .from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(isSecure) // HTTPS 사용 시 활성화
            .path("/")
            .maxAge(TokenExpirationTime.REFRESH_TOKEN_EXPIRATION_TIME) // 7일
            .sameSite("None")
            .build()

    fun createEmptyRefreshTokenCookie(isSecure: Boolean = true): ResponseCookie =
        ResponseCookie
            .from("refresh_token", "")
            .httpOnly(true)
            .secure(isSecure)
            .path("/")
            .maxAge(0)
            .sameSite("None")
            .build()
}
