package com.wafflestudio.internhasha.auth.controller

import com.wafflestudio.internhasha.auth.AuthUser
import com.wafflestudio.internhasha.auth.dto.*
import com.wafflestudio.internhasha.auth.service.AuthService
import com.wafflestudio.internhasha.auth.utils.UserTokenResponseUtil
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
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
        response: HttpServletResponse,
    ): ResponseEntity<UserWithAccessToken> {
        val (user, tokens) = authService.signUp(request)
        return UserTokenResponseUtil.buildUserWithTokensResponse(user, tokens, response, isSecure)
    }

    // 회원 탈퇴
    @DeleteMapping("/user")
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
        response: HttpServletResponse,
    ): ResponseEntity<UserWithAccessToken> {
        val (user, tokens) = authService.signIn(request)
        return UserTokenResponseUtil.buildUserWithTokensResponse(user, tokens, response, isSecure)
    }

    // 로그아웃
    @DeleteMapping("/user/session")
    fun signOut(
        @Parameter(hidden = true) @AuthUser user: User,
        @CookieValue("refresh_token") refreshToken: String?,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        if (refreshToken.isNullOrBlank()) {
            return ResponseEntity.badRequest().build()
        }

        authService.signOut(user, refreshToken)
        return UserTokenResponseUtil.buildDeleteTokenResponse(response)
    }

    // refresh token을 이용한 토큰 갱신
    @GetMapping("/token")
    fun refreshAccessToken(
        @CookieValue("refresh_token") refreshToken: String?,
        response: HttpServletResponse,
    ): ResponseEntity<Token> {
        if (refreshToken.isNullOrBlank()) {
            return ResponseEntity.badRequest().build()
        }
        val tokens = authService.refreshAccessToken(refreshToken)
        return UserTokenResponseUtil.buildTokensResponse(tokens, response)
    }

    // 메일 중복 확인
    @PostMapping("/mail")
    fun checkDuplicateMail(
        @Valid @RequestBody request: EmailRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        authService.checkDuplicateMail(request)
        return ResponseEntity.ok().build()
    }

    // 스누메일 인증을 위한 코드 발송
    @PostMapping("/mail/verify")
    fun sendSnuMailVerification(
        @Valid @RequestBody request: SnuMailRequest,
    ): ResponseEntity<Void> {
        authService.sendSnuMailVerification(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/mail/validate")
    fun checkSnuMailVerification(
        @Valid @RequestBody request: CheckSnuMailVerificationRequest,
    ): ResponseEntity<SuccessCode> {
        val successCode = authService.checkSnuMailVerification(request)
        return ResponseEntity.ok(SuccessCode(successCode))
    }

    // 비밀 번호 변경
    @PatchMapping("/password")
    fun changePassword(
        @Parameter(hidden = true) @AuthUser user: User,
        @Valid @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<Void> {
        authService.changePassword(user, request)
        return ResponseEntity.ok().build()
    }

    // 임시 비밀번호를 스누메일로 전송
    @PostMapping("/password")
    fun resetPassword(
        @Valid @RequestBody request: EmailRequest,
    ): ResponseEntity<Void> {
        authService.resetPassword(request)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/me")
    fun getUserInfo(
        @Parameter(hidden = true) @AuthUser user: User,
    ): ResponseEntity<User> {
        return ResponseEntity.ok(user)
    }
}

data class CheckSnuMailVerificationRequest(
    @field:NotBlank(message = "Email is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9._%+-]+@snu\\.ac\\.kr$", message = "Email must end with @snu.ac.kr")
    val snuMail: String,
    @field:NotBlank(message = "Verification code is required")
    val code: String,
)

data class SnuMailRequest(
    @field:NotBlank(message = "Email is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9._%+-]+@snu\\.ac\\.kr$", message = "Email must end with @snu.ac.kr")
    val snuMail: String,
)

data class EmailRequest(
    @field:NotBlank(message = "email is required")
    @field:Email(message = "email is required")
    val email: String,
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "password is required")
    @field:Size(
        min = 8,
        max = 64,
        message = "Password must be between 8 and 64 characters.",
    )
    val oldPassword: String,
    @field:NotBlank(message = "password is required")
    @field:Size(
        min = 8,
        max = 64,
        message = "Password must be between 8 and 64 characters.",
    )
    val newPassword: String,
)

data class SuccessCode(
    val successCode: String,
)
