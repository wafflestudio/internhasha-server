package com.waffletoy.team1server.user.controller

import com.waffletoy.team1server.user.*
import com.waffletoy.team1server.user.persistence.UserRepository
import com.waffletoy.team1server.user.service.EmailService
import com.waffletoy.team1server.user.service.UserService
import io.swagger.v3.oas.annotations.Parameter
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
    private val userRepository: UserRepository,
) {
    // 회원가입
    @PostMapping("/signup")
    fun signUp(
        @RequestBody request: SignUpRequest,
        response: HttpServletResponse,
    ): ResponseEntity<SignUpResponse> {
        val (user, tokens) =
            userService.signUp(
                authProvider = request.authProvider,
                snuMail = request.snuMail,
                nickname = request.nickname,
                loginId = request.loginId,
                password = request.password,
                googleAccessToken = request.googleAccessToken,
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
                        snuMail = user.snuMail,
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
                googleAccessToken = request.googleAccessToken,
                loginId = request.loginId,
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
                        snuMail = user.snuMail,
                        nickname = user.nickname,
                        status = user.status,
                        authProvider = request.authProvider,
                    ),
                accessToken = tokens.accessToken,
            ),
        )
    }

    // Access Token 재발급
    @PostMapping("/token/refresh")
    fun refreshAccessToken(
        @CookieValue("refresh_token") refreshToken: String,
        response: HttpServletResponse,
    ): ResponseEntity<TokenRefreshResponse> {
        val tokens = userService.refreshAccessToken(refreshToken)

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
            TokenRefreshResponse(
                accessToken = tokens.accessToken,
            ),
        )
    }

    // 유저 이메일 인증 링크 클릭
    @PostMapping("/verify-email")
    fun verifyEmail(
        @Parameter(hidden = true) @AuthUser user: User?,
        @RequestParam("token") token: String,
    ): ResponseEntity<Void> {
        if (user == null) {
            throw AuthenticateException("Invalid user")
        }
        // 토큰 검증 및 이메일 인증 처리
        val userId = emailService.verifyToken(user.id, token)
        userService.markEmailAsVerified(userId)

        return ResponseEntity.ok().build()
    }

    // 로그아웃 - webconfig에서 api 관리
    @PostMapping("/logout")
    fun logout(
        // 인증된 사용자 객체를 주입받음
        @Parameter(hidden = true) @AuthUser user: User?,
        @CookieValue("refresh_token") refreshToken: String,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        if (user == null) {
            throw AuthenticateException("Invalid user")
        }

        userService.logout(
            user,
            refreshToken,
        )

        // Refresh Token 쿠키 삭제
        val deleteCookie =
            ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .maxAge(0)
                .build()
        response.addHeader("Set-Cookie", deleteCookie.toString())

        return ResponseEntity.ok().build()
    }

    // 사용자 정보 확인
    @GetMapping("/user/info")
    fun getUserInfo(
        @Parameter(hidden = true) @AuthUser user: User?,
//        @RequestParam("token") accessToken: String,
    ): ResponseEntity<UserData> {
//        val user = userService.authenticate(accessToken).user ?: throw AuthenticateException("Unauthorized user")
        if (user == null) {
            throw AuthenticateException("Invalid user")
        }
        return ResponseEntity.ok(
            UserData(
                id = user.id,
                snuMail = user.snuMail,
                nickname = user.nickname,
                status = user.status,
                authProvider = user.authProvider,
            ),
        )
    }

    // 비밀번호 변경
    @PostMapping("/password/change")
    fun changePassword(
        @Parameter(hidden = true) @AuthUser user: User?,
        @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<Void> {
        if (user == null) {
            throw AuthenticateException("Invalid user")
        }

        userService.changePassword(
            user,
            request.oldPassword,
            request.newPassword,
        )
        return ResponseEntity.ok().build()
    }

    @GetMapping("/resetDB")
    fun resetDB(): ResponseEntity<Void> {
        userService.deleteAllUsers()
        return ResponseEntity.ok().build()
    }

    @Value("\${custom.domain-url}")
    private lateinit var domainUrl: String
}

data class UserData(
    val id: String,
    val snuMail: String,
    val nickname: String,
    val status: UserStatus,
    val authProvider: AuthProvider,
)

data class SignUpRequest(
    val authProvider: AuthProvider,
    val snuMail: String,
    // 로컬 로그인
    val nickname: String?,
    val loginId: String?,
    val password: String?,
    // 소셜 로그인
    val googleAccessToken: String?,
)

data class SignUpResponse(
    val userData: UserData,
    val accessToken: String,
)

data class SignInRequest(
    val authProvider: AuthProvider,
    // 소셜 로그인
    val googleAccessToken: String?,
    // 로컬 로그인
    val loginId: String?,
    val password: String?,
)

data class SignInResponse(
    val userData: UserData,
    val accessToken: String,
)

data class TokenRefreshResponse(
    val accessToken: String,
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
)

data class AuthenticatedUser(
    val user: User?,
    val accessToken: String,
)
