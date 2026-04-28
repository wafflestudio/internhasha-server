package com.wafflestudio.internhasha.auth.security

import com.wafflestudio.internhasha.auth.domain.RoleValue
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.service.AuthService
import com.wafflestudio.internhasha.auth.service.TokenService
import com.wafflestudio.internhasha.exceptions.BadAuthorizationHeaderException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver

class JwtAuthenticationFilter(
    private val authService: AuthService,
    private val tokenService: TokenService,
    private val handlerExceptionResolver: HandlerExceptionResolver,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization")

        // 헤더가 없는 경우, 비로그인 유저로 간주
        if (authHeader.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            // 유효한 Authorization 헤더인지 검증
            if (!authHeader.startsWith("Bearer ")) {
                throw BadAuthorizationHeaderException(
                    details = mapOf("Authorization" to "Missing or invalid Authorization header"),
                )
            }

            val token = authHeader.substring(7)
            val user = tokenService.authenticate(token)
            val authentication =
                when (user.userRole) {
                    UserRole.ROLE_APPLICANT -> UsernamePasswordAuthenticationToken(user, null, listOf(SimpleGrantedAuthority(RoleValue.ROLE_APPLICANT)))
                    UserRole.ROLE_COMPANY -> UsernamePasswordAuthenticationToken(user, null, listOf(SimpleGrantedAuthority(RoleValue.ROLE_COMPANY)))
                }
            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            handlerExceptionResolver.resolveException(request, response, null, ex)
            return
        }
    }
}
