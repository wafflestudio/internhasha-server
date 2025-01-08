package com.waffletoy.team1server.account.controller

import com.waffletoy.team1server.account.AuthUser
import com.waffletoy.team1server.account.AuthenticateException
import com.waffletoy.team1server.account.service.EmailService
import com.waffletoy.team1server.account.service.UserService
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val emailService: EmailService,
    @Value("\${custom.is-secure}") private val isSecure: Boolean,
) {
    @PostMapping("/signup/send-code")
    fun sendCode(
        @RequestBody request: SendCodeRequest,
    ): ResponseEntity<Void> {
        // 이메일 코드 전송
        emailService.sendCode(request.snuMail)
        return ResponseEntity.ok().build()
    }

    // 유저 이메일 인증 링크 클릭
    @PostMapping("/verify-email")
    fun verifyEmail(
        @RequestBody request: VerifyCodeRequest,
    ): ResponseEntity<Void> {
        // 코드 검증
        emailService.verifyToken(request.snuMail, request.code)
        return ResponseEntity.ok().build()
    }

    // 로컬 회원가입
    @PostMapping("/signup/local")
    fun signUpLocal(
        @RequestBody request: LocalSignUpRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserWithTokenDTO> {
        val (user, tokens) =
            userService.signUp(
                username = request.username,
                snuMail = request.snuMail,
                localId = request.localId,
                password = request.password,
            )

        // Refresh Token을 HTTP-only 쿠키에 저장
        val refreshTokenCookie = createRefreshTokenCookie(tokens.refreshToken)
        response.addHeader("Set-Cookie", refreshTokenCookie)

        return ResponseEntity.ok(
            UserWithTokenDTO(
                user =
                    UserBriefDTO(
                        id = user.id,
                        username = user.username,
                        isAdmin = user.isAdmin,
                    ),
                accessToken = tokens.accessToken,
            ),
        )
    }

    // 구글 회원가입
    @PostMapping("/signup/google")
    fun signUpGoogle(
        @RequestBody request: GoogleSignUpRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserWithTokenDTO> {
        val (user, tokens) =
            userService.signUp(
                googleAccessToken = request.googleAccessToken,
                snuMail = request.snuMail,
            )

        // Refresh Token을 HTTP-only 쿠키에 저장
        val refreshTokenCookie = createRefreshTokenCookie(tokens.refreshToken)
        response.addHeader("Set-Cookie", refreshTokenCookie)

        return ResponseEntity.ok(
            UserWithTokenDTO(
                user =
                    UserBriefDTO(
                        id = user.id,
                        username = user.username,
                        isAdmin = user.isAdmin,
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
    ): ResponseEntity<TokenDTO> {
        val tokens = userService.refreshAccessToken(refreshToken)

        // Refresh Token을 HTTP-only 쿠키에 저장
        val refreshTokenCookie = createRefreshTokenCookie(tokens.refreshToken)
        response.addHeader("Set-Cookie", refreshTokenCookie)

        return ResponseEntity.ok(
            TokenDTO(
                accessToken = tokens.accessToken,
            ),
        )
    }

    // 로컬 로그인
    @PostMapping("/signin/local")
    fun signInLocal(
        @RequestBody request: LocalSignInRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserWithTokenDTO> {
        val (userOrAdmin, tokens) =
            userService.signIn(
                localId = request.localId,
                password = request.password,
            )

        // Refresh Token을 HTTP-only 쿠키에 저장
        val refreshTokenCookie = createRefreshTokenCookie(tokens.refreshToken)
        response.addHeader("Set-Cookie", refreshTokenCookie)

        return ResponseEntity.ok(
            UserWithTokenDTO(
                user =
                    UserBriefDTO(
                        id = userOrAdmin.id,
                        username = userOrAdmin.username,
                        isAdmin = userOrAdmin.isAdmin,
                    ),
                accessToken = tokens.accessToken,
            ),
        )
    }

    // 구글 로그인
    @PostMapping("/signin/google")
    fun signInGoogle(
        @RequestBody request: GoogleSignInRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserWithTokenDTO> {
        val (user, tokens) =
            userService.signIn(
                googleAccessToken = request.googleAccessToken,
            )

        // Refresh Token을 HTTP-only 쿠키에 저장
        val refreshTokenCookie = createRefreshTokenCookie(tokens.refreshToken)
        response.addHeader("Set-Cookie", refreshTokenCookie)

        return ResponseEntity.ok(
            UserWithTokenDTO(
                user =
                    UserBriefDTO(
                        id = user.id,
                        username = user.username,
                        isAdmin = user.isAdmin,
                    ),
                accessToken = tokens.accessToken,
            ),
        )
    }

    // 비밀번호 변경
    @PostMapping("/password/change")
    fun changePassword(
        @Parameter(hidden = true) @AuthUser user: User?,
        @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<Void> {
        if (user == null) throw AuthenticateException("유효하지 않은 엑세스 토큰입니다.")

        userService.changePassword(
            user,
            request.oldPassword,
            request.newPassword,
        )
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
        if (user == null) throw AuthenticateException("유효하지 않은 엑세스 토큰입니다.")

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
    ): ResponseEntity<User> {
        if (user == null) throw AuthenticateException("유효하지 않은 엑세스 토큰입니다.")
        return ResponseEntity.ok(
            user,
        )
    }

    // 로컬 로그인 추가(구글 계정)
    @PostMapping("/local")
    fun signIn(
        @RequestBody request: LocalSignUpAddRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserWithTokenDTO> {
        val (user, tokens) =
            userService.mergeAccount(
                snuMail = request.snuMail,
                localId = request.localId,
                password = request.password,
            )

        // Refresh Token을 HTTP-only 쿠키에 저장
        val refreshTokenCookie = createRefreshTokenCookie(tokens.refreshToken)
        response.addHeader("Set-Cookie", refreshTokenCookie)

        return ResponseEntity.ok(
            UserWithTokenDTO(
                user =
                    UserBriefDTO(
                        id = user.id,
                        username = user.username,
                        isAdmin = user.isAdmin,
                    ),
                accessToken = tokens.accessToken,
            ),
        )
    }

    // 구글 로그인 추가(로컬 계정)
    @PostMapping("/google")
    fun signUp(
        @RequestBody request: GoogleSignUpRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserWithTokenDTO> {
        val (user, tokens) =
            userService.mergeAccount(
                googleAccessToken = request.googleAccessToken,
                snuMail = request.snuMail,
            )

        // Refresh Token을 HTTP-only 쿠키에 저장
        val refreshTokenCookie = createRefreshTokenCookie(tokens.refreshToken)
        response.addHeader("Set-Cookie", refreshTokenCookie)

        return ResponseEntity.ok(
            UserWithTokenDTO(
                user =
                    UserBriefDTO(
                        id = user.id,
                        username = user.username,
                        isAdmin = user.isAdmin,
                    ),
                accessToken = tokens.accessToken,
            ),
        )
    }

    @GetMapping("/resetDB")
    fun resetDB(): ResponseEntity<Void> {
        userService.deleteAllUsers()
        return ResponseEntity.ok().build()
    }

    @GetMapping("/id-duplicate")
    fun checkDuplicate(
        @RequestBody request:checkId
    ) : ResponseEntity<Void> {
        // 중복이 있으면 409 CONFLICT
        userService.checkDuplicate(request.localId)
        // 중복이 없으면 200 OK
        return ResponseEntity.ok().build()
    }

    // Refresh Token 쿠키 생성 함수
    private fun createRefreshTokenCookie(refreshToken: String): String {
        return ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(isSecure) // HTTPS 사용 시 활성화
            .path("/")
            .maxAge(7 * 24 * 60 * 60) // 7일
            .build()
            .toString()
    }

    @Value("\${custom.domain-url}")
    private lateinit var domainUrl: String
}

data class UserBriefDTO(
    val id: String,
    val username: String,
    val isAdmin: Boolean,
)

data class UserWithTokenDTO(
    val user: UserBriefDTO,
    val accessToken: String,
)

data class TokenDTO(
    val accessToken: String,
)

data class SendCodeRequest(
    val snuMail: String,
)

data class VerifyCodeRequest(
    val snuMail: String,
    val code: String,
)

data class LocalSignUpRequest(
    val username: String,
    val localId: String,
    val password: String,
    val snuMail: String,
)

data class LocalSignUpAddRequest(
    val localId: String,
    val password: String,
    val snuMail: String,
)

data class GoogleSignUpRequest(
    val googleAccessToken: String,
    val snuMail: String,
)

data class LocalSignInRequest(
    val localId: String,
    val password: String,
)

data class GoogleSignInRequest(
    val googleAccessToken: String,
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
)

data class checkId(
    val localId: String?
)