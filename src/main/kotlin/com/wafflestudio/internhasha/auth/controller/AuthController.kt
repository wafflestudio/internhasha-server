package com.wafflestudio.internhasha.auth.controller

import com.wafflestudio.internhasha.auth.controller.dto.request.ChangePasswordRequest
import com.wafflestudio.internhasha.auth.controller.dto.request.CheckSnuMailVerificationRequest
import com.wafflestudio.internhasha.auth.controller.dto.request.EmailRequest
import com.wafflestudio.internhasha.auth.controller.dto.request.SignInRequest
import com.wafflestudio.internhasha.auth.controller.dto.request.SignUpRequest
import com.wafflestudio.internhasha.auth.controller.dto.request.SnuMailRequest
import com.wafflestudio.internhasha.auth.controller.dto.response.SuccessCodeResponse
import com.wafflestudio.internhasha.auth.controller.dto.response.TokenResponse
import com.wafflestudio.internhasha.auth.controller.dto.response.UserInfoResponse
import com.wafflestudio.internhasha.auth.controller.dto.response.UserTokenResponse
import com.wafflestudio.internhasha.auth.controller.dto.response.UserTokenResponseFactory
import com.wafflestudio.internhasha.auth.security.AuthUser
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.auth.service.AccountService
import com.wafflestudio.internhasha.auth.service.AuthService
import com.wafflestudio.internhasha.auth.service.EmailVerificationService
import com.wafflestudio.internhasha.auth.service.SignUpService
import com.wafflestudio.internhasha.auth.service.TokenService
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
    private val signUpService: SignUpService,
    private val accountService: AccountService,
    private val tokenService: TokenService,
    private val emailVerificationService: EmailVerificationService,
    @Value("\${custom.is-secure}") private val isSecure: Boolean,
) {
    // 회원가입
    @PostMapping("/user")
    fun signUp(
        @Valid @RequestBody request: SignUpRequest,
    ): ResponseEntity<UserTokenResponse> {
        val userWithTokens = signUpService.signUp(request.toCommand())
        return UserTokenResponseFactory.buildUserWithTokensResponse(userWithTokens, isSecure)
    }

    // 회원 탈퇴
    @DeleteMapping("/user")
    @PreAuthorize("isAuthenticated()")
    fun deleteUser(
        @Parameter(hidden = true) @AuthUser authUser: AuthUserPrincipal,
    ): ResponseEntity<Void> {
        accountService.withdrawUser(authUser)
        return ResponseEntity.ok().build()
    }

    // 로그인
    @PostMapping("/user/session")
    fun signIn(
        @Valid @RequestBody request: SignInRequest,
    ): ResponseEntity<UserTokenResponse> {
        val userWithTokens = authService.signIn(request.toCommand())
        return UserTokenResponseFactory.buildUserWithTokensResponse(userWithTokens, isSecure)
    }

    // 로그아웃
    @DeleteMapping("/user/session")
    @PreAuthorize("isAuthenticated()")
    fun signOut(
        @Parameter(hidden = true) @AuthUser authUser: AuthUserPrincipal,
        @CookieValue("refresh_token") refreshToken: String?,
    ): ResponseEntity<Void> {
        if (refreshToken.isNullOrBlank()) {
            return ResponseEntity.badRequest().build()
        }
        authService.signOut(authUser.id)
        return UserTokenResponseFactory.buildDeleteTokenResponse(isSecure)
    }

    // refresh token을 이용한 토큰 갱신
    @GetMapping("/token")
    fun refreshAccessToken(
        @CookieValue("refresh_token") refreshToken: String?,
    ): ResponseEntity<TokenResponse> {
        if (refreshToken.isNullOrBlank()) {
            return ResponseEntity.badRequest().build()
        }
        val tokens = tokenService.refreshAccessToken(refreshToken)
        return UserTokenResponseFactory.buildTokensResponse(tokens, isSecure)
    }

    // 메일 중복 확인
    @PostMapping("/mail")
    fun checkDuplicateMail(
        @Valid @RequestBody request: EmailRequest,
    ): ResponseEntity<Void> {
        emailVerificationService.checkDuplicateMail(request.toCheckDuplicateMailCommand())
        return ResponseEntity.ok().build()
    }

    // 스누메일 인증을 위한 코드 발송
    @PostMapping("/mail/verify")
    fun sendSnuMailVerification(
        @Valid @RequestBody request: SnuMailRequest,
    ): ResponseEntity<Void> {
        emailVerificationService.sendSnuMailVerification(request.toCommand())
        return ResponseEntity.ok().build()
    }

    @PostMapping("/mail/validate")
    fun checkSnuMailVerification(
        @Valid @RequestBody request: CheckSnuMailVerificationRequest,
    ): ResponseEntity<SuccessCodeResponse> {
        val successCode = emailVerificationService.checkSnuMailVerification(request.toCommand())
        return ResponseEntity.ok(SuccessCodeResponse(successCode))
    }

    // 비밀 번호 변경
    @PatchMapping("/password")
    @PreAuthorize("isAuthenticated()")
    fun changePassword(
        @Parameter(hidden = true) @AuthUser authUser: AuthUserPrincipal,
        @Valid @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<Void> {
        accountService.changePassword(request.toCommand(authUser.id))
        return ResponseEntity.ok().build()
    }

    // 비밀번호 리셋 - 임시 비밀번호를 스누메일로 전송
    @PostMapping("/password")
    fun resetPassword(
        @Valid @RequestBody request: EmailRequest,
    ): ResponseEntity<Void> {
        accountService.resetPassword(request.toResetPasswordCommand())
        return ResponseEntity.ok().build()
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getUserInfo(
        @Parameter(hidden = true) @AuthUser authUser: AuthUserPrincipal,
    ): ResponseEntity<UserInfoResponse> {
        return ResponseEntity.ok(
            UserInfoResponse(
                id = authUser.id,
                name = authUser.name,
                email = authUser.email,
                userRole = authUser.userRole,
            ),
        )
    }
}
