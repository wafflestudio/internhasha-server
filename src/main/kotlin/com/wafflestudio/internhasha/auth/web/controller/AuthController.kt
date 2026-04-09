package com.wafflestudio.internhasha.auth.web.controller

import com.wafflestudio.internhasha.auth.application.service.AuthService
import com.wafflestudio.internhasha.auth.domain.Token
import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.domain.UserSimple
import com.wafflestudio.internhasha.auth.domain.UserWithAccessToken
import com.wafflestudio.internhasha.auth.web.AuthUser
import com.wafflestudio.internhasha.auth.web.dto.ChangePasswordRequest
import com.wafflestudio.internhasha.auth.web.dto.CheckSnuMailVerificationRequest
import com.wafflestudio.internhasha.auth.web.dto.EmailRequest
import com.wafflestudio.internhasha.auth.web.dto.SignInRequest
import com.wafflestudio.internhasha.auth.web.dto.SignUpRequest
import com.wafflestudio.internhasha.auth.web.dto.SnuMailRequest
import com.wafflestudio.internhasha.auth.web.dto.SuccessCodeResponse
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    @Value("\${custom.is-secure}") private val isSecure: Boolean,
) {
    // 회원가입
    @PostMapping("/user")
    fun signUp(
        @Valid @RequestBody request: SignUpRequest,
    ): ResponseEntity<UserWithAccessToken> {
        val (user, tokens) = authService.signUp(request.toCommand())
        return UserTokenResponseFactory.buildUserWithTokensResponse(UserSimple(user.id, user.userRole), tokens, isSecure)
    }

    // 회원 탈퇴
    @DeleteMapping("/user")
    @PreAuthorize("isAuthenticated()")
    fun deleteUser(
        @Parameter(hidden = true) @AuthUser user: User,
    ): ResponseEntity<Void> {
        authService.withdrawUser(user)
        return ResponseEntity.ok().build()
    }

    // 로그인
    @PostMapping("/user/session")
    fun signIn(
        @Valid @RequestBody request: SignInRequest,
    ): ResponseEntity<UserWithAccessToken> {
        val (userBrief, tokens) = authService.signIn(request.toCommand())
        return UserTokenResponseFactory.buildUserWithTokensResponse(userBrief, tokens, isSecure)
    }

    // 로그아웃
    @DeleteMapping("/user/session")
    @PreAuthorize("isAuthenticated()")
    fun signOut(
        @Parameter(hidden = true) @AuthUser user: User,
        @CookieValue("refresh_token") refreshToken: String?,
    ): ResponseEntity<Void> {
        if (refreshToken.isNullOrBlank()) {
            return ResponseEntity.badRequest().build()
        }
        authService.signOut(user.id)
        return UserTokenResponseFactory.buildDeleteTokenResponse(isSecure)
    }

    // refresh token을 이용한 토큰 갱신
    @GetMapping("/token")
    fun refreshAccessToken(
        @CookieValue("refresh_token") refreshToken: String?,
    ): ResponseEntity<Token> {
        if (refreshToken.isNullOrBlank()) {
            return ResponseEntity.badRequest().build()
        }
        val tokens = authService.refreshAccessToken(refreshToken)
        return UserTokenResponseFactory.buildTokensResponse(tokens, isSecure)
    }

    // 메일 중복 확인
    @PostMapping("/mail")
    fun checkDuplicateMail(
        @Valid @RequestBody request: EmailRequest,
    ): ResponseEntity<Void> {
        authService.checkDuplicateMail(request.toCheckDuplicateMailCommand())
        return ResponseEntity.ok().build()
    }

    // 스누메일 인증을 위한 코드 발송
    @PostMapping("/mail/verify")
    fun sendSnuMailVerification(
        @Valid @RequestBody request: SnuMailRequest,
    ): ResponseEntity<Void> {
        authService.sendSnuMailVerification(request.toCommand())
        return ResponseEntity.ok().build()
    }

    @PostMapping("/mail/validate")
    fun checkSnuMailVerification(
        @Valid @RequestBody request: CheckSnuMailVerificationRequest,
    ): ResponseEntity<SuccessCodeResponse> {
        val successCode = authService.checkSnuMailVerification(request.toCommand())
        return ResponseEntity.ok(SuccessCodeResponse(successCode))
    }

    // 비밀 번호 변경
    @PatchMapping("/password")
    @PreAuthorize("isAuthenticated()")
    fun changePassword(
        @Parameter(hidden = true) @AuthUser user: User,
        @Valid @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<Void> {
        authService.changePassword(request.toCommand(user.id))
        return ResponseEntity.ok().build()
    }

    // 비밀번호 리셋 - 임시 비밀번호를 스누메일로 전송
    @PostMapping("/password")
    fun resetPassword(
        @Valid @RequestBody request: EmailRequest,
    ): ResponseEntity<Void> {
        authService.resetPassword(request.toResetPasswordCommand())
        return ResponseEntity.ok().build()
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getUserInfo(
        @Parameter(hidden = true) @AuthUser user: User,
    ): ResponseEntity<User> {
        return ResponseEntity.ok(user)
    }
}
