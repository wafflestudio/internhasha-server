package com.waffletoy.team1server.user.utils

import com.waffletoy.team1server.user.dtos.AccessToken
import com.waffletoy.team1server.user.dtos.User
import com.waffletoy.team1server.user.dtos.UserWithAccessToken
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity

object UserTokenResponseUtil {
    fun buildUserWithTokensResponse(
        user: User,
        tokens: UserTokenUtil.Tokens,
        response: HttpServletResponse,
        isSecure: Boolean = true,
    ): ResponseEntity<UserWithAccessToken> {
        val refreshTokenCookie = UserTokenUtil.createRefreshTokenCookie(tokens, isSecure = isSecure)
        response.addHeader("Set-Cookie", refreshTokenCookie.toString())

        return ResponseEntity.ok(
            UserWithAccessToken(
                user = user,
                token = tokens.accessToken,
            ),
        )
    }

    fun buildTokensResponse(
        tokens: UserTokenUtil.Tokens,
        response: HttpServletResponse,
        isSecure: Boolean = true,
    ): ResponseEntity<AccessToken> {
        val refreshTokenCookie = UserTokenUtil.createRefreshTokenCookie(tokens, isSecure = isSecure)
        response.addHeader("Set-Cookie", refreshTokenCookie.toString())

        return ResponseEntity.ok(
            AccessToken(
                accessToken = tokens.accessToken,
            ),
        )
    }

    fun buildDeleteTokenResponse(
        response: HttpServletResponse,
        isSecure: Boolean = true,
    ): ResponseEntity<Void> {
        val deleteCookie = UserTokenUtil.createEmptyRefreshTokenCookie(isSecure = isSecure)
        response.addHeader("Set-Cookie", deleteCookie.toString())
        return ResponseEntity.ok().build()
    }
}
