package com.waffletoy.team1server.user.controller

import com.waffletoy.team1server.user.AuthUser
import com.waffletoy.team1server.user.dtos.*
import com.waffletoy.team1server.user.service.UserService
import com.waffletoy.team1server.user.utils.UserTokenResponseUtil
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    @Value("\${custom.is-secure}") private val isSecure: Boolean,
) {
    // Endpoints for signups
    // 로컬 아이디 중복 확인
    @PostMapping("/signup/check-id")
    fun checkDuplicateId(
        @Valid @RequestBody request: CheckDuplicateIdRequest,
    ): ResponseEntity<Void> {
        userService.checkDuplicateId(request)
        return ResponseEntity.ok().build()
    }

    // 스누메일 중복 확인
    @PostMapping("/signup/check-snu-mail")
    fun checkDuplicateSnuMail(
        @Valid @RequestBody request: CheckDuplicateSnuMailRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        userService.checkDuplicateSnuMail(request)
        return ResponseEntity.ok().build()
    }

    // 회원가입(local_normal, local_curator)
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: SignUpRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserWithAccessToken> {
        val (user, tokens) = userService.signUp(request)
        return UserTokenResponseUtil.buildUserWithTokensResponse(user, tokens, response, isSecure)
    }

    // Endpoints for sign in / sign out

    @PostMapping("/signin")
    fun signIn(
        @Valid @RequestBody request: SignInRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserWithAccessToken> {
        val (user, tokens) = userService.signIn(request)
        return UserTokenResponseUtil.buildUserWithTokensResponse(user, tokens, response, isSecure)
    }

    @PostMapping("/signout")
    fun signOut(
        @Parameter(hidden = true) @AuthUser user: User,
        @CookieValue("refresh_token") refreshToken: String?,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        if (refreshToken.isNullOrBlank()) {
            return ResponseEntity.badRequest().build()
        }

        userService.signOut(user, refreshToken)
        return UserTokenResponseUtil.buildDeleteTokenResponse(response)
    }

    // refresh token을 이용한 토큰 갱신
    @PostMapping("/refresh-token")
    fun refreshAccessToken(
        @CookieValue("refresh_token") refreshToken: String?,
        response: HttpServletResponse,
    ): ResponseEntity<AccessToken> {
        if (refreshToken.isNullOrBlank()) {
            return ResponseEntity.badRequest().build()
        }
        val tokens = userService.refreshAccessToken(refreshToken)
        return UserTokenResponseUtil.buildTokensResponse(tokens, response)
    }

    // Endpoints for snu mail
    // 구글 access token 으로 구글 이메일 가져오기
    @PostMapping("/snu-mail-verification/google-email")
    fun fetchGoogleAccountEmail(
        @RequestBody request: FetchGoogleAccountEmailRequest,
    ): ResponseEntity<GoogleEmail> {
        val email = userService.fetchGoogleAccountEmail(request)
        return ResponseEntity.ok(
            GoogleEmail(
                googleEmail = email,
            ),
        )
    }

    @PostMapping("/snu-mail-verification/request")
    fun sendSnuMailVerification(
        @Valid @RequestBody request: SendSnuMailVerificationRequest,
    ): ResponseEntity<Void> {
        userService.sendSnuMailVerification(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/snu-mail-verification/verify")
    fun checkSnuMailVerification(
        @Valid @RequestBody request: CheckSnuMailVerificationRequest,
    ): ResponseEntity<Void> {
        userService.checkSnuMailVerification(request) // TODO: 뭐라도 Return해야 하지는 않을지
        return ResponseEntity.ok().build()
    }

    // TODO
    @GetMapping("/me")
    fun getUserInfo(
        @Parameter(hidden = true) @AuthUser user: User,
    ): ResponseEntity<User> {
        return ResponseEntity.ok(user)
    }

    // User 부가 기능

    // 회원 탈퇴
    @PostMapping("/withdraw")
    fun deleteUser(
        @Parameter(hidden = true) @AuthUser user: User,
    ): ResponseEntity<Void> {
        userService.withdrawUser(user)
        return ResponseEntity.ok().build()
    }

    // 비밀 번호 변경
    @PostMapping("/change-password")
    fun changePassword(
        @Parameter(hidden = true) @AuthUser user: User,
        @Valid @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<Void> {
        userService.changePassword(user, request)
        return ResponseEntity.ok().build()
    }

    // 아이디(또는 소셜 로그인 정보)를 찾아 스누메일로 전송
    @PostMapping("/help/find-Id")
    fun findId(
        @Valid @RequestBody request: FindIdRequest,
    ): ResponseEntity<Void> {
        userService.findIdAndFetchInfo(request)
        return ResponseEntity.ok().build()
    }

    // 임시 비밀번호를 스누메일로 전송
    @PostMapping("/help/reset-password")
    fun resetPassword(
        @Valid @RequestBody request: ResetPasswordRequest,
    ): ResponseEntity<Void> {
        userService.resetPassword(request)
        return ResponseEntity.ok().build()
    }

    // Endpoint for resetting DB for testing
    // reset DB는 비활성화
//    @PostMapping("/resetDB")
//    fun resetDatabase(
//        @RequestHeader("X-Secret") secret: String,
//    ): ResponseEntity<String> {
//        userService.resetDatabase(secret)
//        return ResponseEntity.ok("Database has been reset.")
//    }
}

data class CheckDuplicateIdRequest(
    @field:NotBlank(message = "ID is required")
    val id: String,
)

data class CheckDuplicateSnuMailRequest(
    @field:NotBlank(message = "Email is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9._%+-]+@snu\\.ac\\.kr$", message = "Email must end with @snu.ac.kr")
    val snuMail: String,
)

data class FetchGoogleAccountEmailRequest(
    @field:NotBlank(message = "Access token is required")
    val accessToken: String,
)

data class SendSnuMailVerificationRequest(
    @field:NotBlank(message = "Email is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9._%+-]+@snu\\.ac\\.kr$", message = "Email must end with @snu.ac.kr")
    val snuMail: String,
)

data class CheckSnuMailVerificationRequest(
    @field:NotBlank(message = "Email is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9._%+-]+@snu\\.ac\\.kr$", message = "Email must end with @snu.ac.kr")
    val snuMail: String,
    @field:NotBlank(message = "Verification code is required")
    val code: String,
)

data class ChangePasswordRequest(
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!*]).{8,20}$",
        message = "Password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character.",
    )
    val oldPassword: String,
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!*]).{8,20}$",
        message = "Password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character.",
    )
    val newPassword: String,
)

data class FindIdRequest(
    @field:NotBlank(message = "Email is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9._%+-]+@snu\\.ac\\.kr$", message = "Email must end with @snu.ac.kr")
    val snuMail: String,
)

data class ResetPasswordRequest(
    @field:NotBlank(message = "Email is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9._%+-]+@snu\\.ac\\.kr$", message = "Email must end with @snu.ac.kr")
    val snuMail: String,
)
