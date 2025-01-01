package com.waffletoy.team1server.user.controller

import com.waffletoy.team1server.user.AuthProvider
import com.waffletoy.team1server.user.UserStatus
import com.waffletoy.team1server.user.service.EmailService
import com.waffletoy.team1server.user.service.UserService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService,
    private val emailService: EmailService,
    @Value("\${custom.is-secure}") private val isSecure: Boolean,
) {
    @GetMapping("/debug")
    fun debug(): String {
        return "IS_SECURE: $isSecure"
    }

    // 회원가입
    @PostMapping("/signup")
    fun signUp(
        @RequestBody request: SignUpRequest,
        response: HttpServletResponse,
    ): ResponseEntity<SignUpResponse> {
        val (user, tokens) =
            userService.signUp(
                authProvider = request.authProvider,
                email = request.email,
                nickname = request.nickname,
                loginID = request.loginID,
                password = request.password,
                socialAccessToken = request.socialAccessToken,
            )

        // Refresh Token을 HTTP-only 쿠키에 저장
        val refreshTokenCookie =
            ResponseCookie.from(
                "refresh_token",
                tokens.refreshToken,
            )
                .httpOnly(true)
                .secure(isSecure) // HTTPS 사용 시 활성화
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7일
                .build()

        response.addHeader("Set-Cookie", refreshTokenCookie.toString())

        return ResponseEntity.ok(
            SignUpResponse(
                userData =
                    UserData(
                        id = user.id,
                        email = user.email,
                        nickname = user.nickname,
                        status = user.status,
                        authProvider = request.authProvider,
                    ),
                accessToken = tokens.accessToken,
            ),
        )
    }

    // 로그인
    @PostMapping("/signin")
    fun signIn(
        @RequestBody request: SignInRequest,
        response: HttpServletResponse,
    ): ResponseEntity<SignInResponse> {
        val (user, tokens) =
            userService.signIn(
                authProvider = request.authProvider,
                socialAccessToken = request.socialAccessToken,
                loginID = request.loginId,
                password = request.password,
            )

        // Refresh Token을 HTTP-only 쿠키에 저장
        val refreshTokenCookie =
            ResponseCookie.from(
                "refresh_token",
                tokens.refreshToken,
            )
                .httpOnly(true)
                .secure(isSecure) // HTTPS 사용 시 활성화
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7일
                .build()

        response.addHeader("Set-Cookie", refreshTokenCookie.toString())

        return ResponseEntity.ok(
            SignInResponse(
                userData =
                    UserData(
                        id = user.id,
                        email = user.email,
                        nickname = user.nickname,
                        status = user.status,
                        authProvider = request.authProvider,
                    ),
                accessToken = tokens.accessToken,
            ),
        )
    }

    @GetMapping("/test-email")
    fun sendTestEmail(): ResponseEntity<String> {
        emailService.sendEmail(
            to = "endermaru007@gmail.com",
            subject = "테스트 이메일",
            body = "이것은 테스트 이메일입니다.",
        )
        return ResponseEntity.ok("Email sent successfully")
    }

    // Access Token 재발급
    @PostMapping("/token/refresh")
    fun refreshAccessToken(
        @CookieValue("refresh_token") refreshToken: String,
    ): ResponseEntity<TokenRefreshResponse> {
        val tokens = userService.refreshAccessToken(refreshToken)
        return ResponseEntity.ok(
            TokenRefreshResponse(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
            ),
        )
    }

//    // 유저 이메일 인증 링크 클릭
//    @PostMapping("/verify-email")
//    fun verifyEmail(
//        @RequestParam("token") token: String
//    ): ResponseEntity<Void> {
//        val userId = emailTokenService.verifyToken(token)
//
//        if (userId != null) {
//            userService.markEmailAsVerified(userId)
//            return ResponseEntity.ok().build()
//        }
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
//    }
//
//    // 비밀번호 변경
//    @PostMapping("/password/change")
//    fun changePassword(
//        @RequestBody request: ChangePasswordRequest
//    ): ResponseEntity<Void> {
// //        userService.changePassword(request.oldPassword, request.newPassword)
//        return ResponseEntity.ok().build()
//    }
//
//    // 사용자 정보 확인
//    @GetMapping("/users/me")
//    fun getUserInfo(
//        @RequestHeader("Authorization") authorization: String
//    ): ResponseEntity<UserData> {
//        val accessToken = authorization.removePrefix("Bearer ")
// //        val user = userService.getUserInfo(accessToken)
// //        return ResponseEntity.ok(user)
//    }
//
//    // 사용자 정보 업데이트
//    @PutMapping("/users/me")
//    fun updateUserInfo(
//        @RequestBody request: UpdateUserInfoRequest
//    ): ResponseEntity<UserData> {
//        val updatedUser = userService.updateUserInfo(request)
//        return ResponseEntity.ok(updatedUser)
//    }
//
//    // 로그아웃
//    @PostMapping("/logout")
//    fun logout(
//        @CookieValue("refresh_token") refreshToken: String
//    ): ResponseEntity<Void> {
//        userService.logout(refreshToken)
//        return ResponseEntity.ok().build()
//    }
}

data class UserData(
    val id: String,
    val email: String,
    val nickname: String,
    val status: UserStatus,
    val authProvider: AuthProvider,
)

data class SignUpRequest(
    val authProvider: AuthProvider,
    val email: String,
    // 로컬 로그인
    val nickname: String?,
    val loginID: String?,
    val password: String?,
    // 소셜 로그인
    val socialAccessToken: String?,
)

data class SignUpResponse(
    val userData: UserData,
    val accessToken: String,
)

data class SignInRequest(
    val authProvider: AuthProvider,
    // 소셜 로그인
    val socialAccessToken: String?,
    // 로컬 로그인
    val loginId: String?,
    val password: String?,
)

data class SignInResponse(
    val userData: UserData,
    val accessToken: String,
)

data class TokenRefreshRequest(
    val refreshToken: String,
)

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
)

data class UpdateUserInfoRequest(
    val nickname: String?,
    val email: String?,
)

data class LogoutRequest(val refreshToken: String)
