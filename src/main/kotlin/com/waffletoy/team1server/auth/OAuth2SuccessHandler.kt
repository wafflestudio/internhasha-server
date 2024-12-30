package com.waffletoy.team1server.auth

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2SuccessHandler(
    private val objectMapper: ObjectMapper,
) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        // 사용자 정보를 가져와 회원가입/로그인 페이지로 리다이렉션
//        val oAuth2User = authentication.principal as org.springframework.security.oauth2.core.user.OAuth2User
//        val email = oAuth2User.attributes["email"].toString()
//        val name = oAuth2User.attributes["name"].toString()
//        val authorities = oAuth2User.authorities

        // 클라이언트로 리디렉션할 URL 생성
        val targetUrl = "https://survey-josha.site/api/echo/echo"

        // 리디렉션 처리
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

//    private fun determineTargetUrl(
//        request: HttpServletRequest,
//        response: HttpServletResponse,
//        email: String,
//    ): String {
//        // 리다이렉트 URL을 쿠키에서 가져오거나 기본 URL로 설정
//        val redirectUrl = request.getParameter("redirect_uri") ?: "http://localhost:8080/api/echo/hello"
//        return UriComponentsBuilder.fromUriString(redirectUrl)
//            .queryParam("email", email) // 추가 정보 전달
//            .build()
//            .toUriString()
//    }
}
