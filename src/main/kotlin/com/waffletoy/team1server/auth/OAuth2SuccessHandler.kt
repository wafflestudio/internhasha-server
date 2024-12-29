package com.waffletoy.team1server.auth

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2SuccessHandler(
    private val objectMapper: ObjectMapper,
) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val oAuth2User = authentication.principal as org.springframework.security.oauth2.core.user.OAuth2User

        // 사용자 정보 가져오기
        val email = oAuth2User.attributes["email"].toString()
        val name = oAuth2User.attributes["name"].toString()

        val authorities = oAuth2User.authorities;

        // 클라이언트로 리디렉션할 URL 생성
        val targetUrl = "http://localhost:8080/api/echo/echo!"

        // JWT 토큰 생성 (추후 구현)
        // val token = jwtService.createToken(email)

        // URL에 토큰 추가 (예제)
        // val targetUrlWithToken = UriComponentsBuilder.fromUriString(targetUrl)
        //     .queryParam("token", token)
        //     .build()
        //     .toUriString()

        // 리디렉션 처리
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        email: String,
    ): String {
        // 리다이렉트 URL을 쿠키에서 가져오거나 기본 URL로 설정
        val redirectUrl = request.getParameter("redirect_uri") ?: "http://localhost:8080/api/echo/hello"
        return UriComponentsBuilder.fromUriString(redirectUrl)
            .queryParam("email", email) // 추가 정보 전달
            .build()
            .toUriString()
    }
}
