package com.wafflestudio.internhasha.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.internhasha.auth.application.service.AuthService
import com.wafflestudio.internhasha.auth.domain.Tokens
import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.domain.UserDuplicateSnuMailException
import com.wafflestudio.internhasha.auth.domain.UserEmailVerificationInvalidException
import com.wafflestudio.internhasha.auth.domain.UserNotFoundException
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.domain.UserSimple
import com.wafflestudio.internhasha.auth.web.dto.ChangePasswordRequest
import com.wafflestudio.internhasha.auth.web.dto.CheckSnuMailVerificationRequest
import com.wafflestudio.internhasha.auth.web.dto.EmailRequest
import com.wafflestudio.internhasha.auth.web.dto.SignInRequest
import com.wafflestudio.internhasha.auth.web.dto.SignUpRequest
import com.wafflestudio.internhasha.auth.web.dto.SnuMailRequest
import com.wafflestudio.internhasha.email.EmailSendFailureException
import com.wafflestudio.internhasha.exceptions.InvalidCredentialsException
import com.wafflestudio.internhasha.exceptions.InvalidRefreshTokenException
import com.wafflestudio.internhasha.exceptions.NotAuthorizedException
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val currentToken = "access-token"

    private val applicantUser =
        User(
            id = "user-1",
            name = "지원자",
            userRole = UserRole.ROLE_APPLICANT,
            email = "applicant@snu.ac.kr",
        )
    private val applicantUserSimple =
        UserSimple(
            id = "user-1",
            userRole = UserRole.ROLE_APPLICANT,
        )
    private val companyUser =
        User(
            id = "user-2",
            name = "기업회원",
            userRole = UserRole.ROLE_COMPANY,
            email = "company@example.com",
        )
    private val companyUserSimple =
        UserSimple(
            id = "user-2",
            userRole = UserRole.ROLE_COMPANY,
        )

    @BeforeEach
    fun setUp() {
        clearMocks(authService)
        authenticateAs(applicantUser)
    }

    @Test
    @DisplayName("회원가입 - 지원자 계정 생성에 성공하면 토큰과 쿠키를 반환한다")
    fun signUpApplicantSuccess() {
        val request =
            SignUpRequest(
                authType = UserRole.ROLE_APPLICANT,
                info =
                    SignUpRequest.LocalApplicantInfo(
                        name = "홍길동",
                        email = "hong@snu.ac.kr",
                        password = "Password123!",
                        successCode = "Hello world",
                    ),
            )
        val tokens = Tokens(accessToken = "access-token", refreshToken = "refresh-token")
        every { authService.signUp(any()) } returns (applicantUser to tokens)

        performJsonPost("/api/auth/user", request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.user.id").value(applicantUser.id))
            .andExpect(jsonPath("$.user.userRole").value("APPLICANT"))
            .andExpect(jsonPath("$.token").value("access-token"))
            .andExpect(header().string("Set-Cookie", containsString("refresh_token=refresh-token")))
            .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))

        verify(exactly = 1) { authService.signUp(match { it.authType == UserRole.ROLE_APPLICANT }) }
    }

    @Test
    @DisplayName("회원가입 - 기업 계정 생성에 성공하면 토큰과 쿠키를 반환한다")
    fun signUpCompanySuccess() {
        val request =
            SignUpRequest(
                authType = UserRole.ROLE_COMPANY,
                info =
                    SignUpRequest.LocalCompanyInfo(
                        secretPassword = "Hello world",
                        name = "회사",
                        email = "company@example.com",
                        password = "Password123!",
                        vcName = "벤처캐피탈",
                        vcRecommendation = "추천서",
                    ),
            )
        val tokens = Tokens(accessToken = "access-token", refreshToken = "refresh-token")
        every { authService.signUp(any()) } returns (companyUser to tokens)

        performJsonPost("/api/auth/user", request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.user.id").value(companyUser.id))
            .andExpect(jsonPath("$.user.userRole").value("COMPANY"))
            .andExpect(jsonPath("$.token").value("access-token"))
            .andExpect(header().string("Set-Cookie", containsString("refresh_token=refresh-token")))

        verify(exactly = 1) { authService.signUp(match { it.authType == UserRole.ROLE_COMPANY }) }
    }

    @Test
    @DisplayName("회원가입 - 잘못된 요청 바디는 validation error로 응답한다")
    fun signUpValidationFailure() {
        val request =
            SignUpRequest(
                authType = UserRole.ROLE_APPLICANT,
                info =
                    SignUpRequest.LocalApplicantInfo(
                        name = "",
                        email = "invalid-email",
                        password = "short",
                        successCode = "Hello world",
                    ),
            )

        performJsonPost("/api/auth/user", request)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
            .andExpect(jsonPath("$.message").value("Validation failed for the request."))
            .andExpect(jsonPath("$.details['info.name']").value("name is required"))
            .andExpect(jsonPath("$.details['info.email']").value("snuMail must be a valid SNU email address."))
            .andExpect(jsonPath("$.details['info.password']").value("Password must be between 8 and 64 characters."))

        verify(exactly = 0) { authService.signUp(any()) }
    }

    @Test
    @DisplayName("회원가입 - 서비스가 중복 이메일 예외를 던지면 에러 응답으로 변환된다")
    fun signUpDuplicateMailFailure() {
        val request =
            SignUpRequest(
                authType = UserRole.ROLE_APPLICANT,
                info =
                    SignUpRequest.LocalApplicantInfo(
                        name = "홍길동",
                        email = "hong@snu.ac.kr",
                        password = "Password123!",
                        successCode = "Hello world",
                    ),
            )
        every { authService.signUp(any()) } throws UserDuplicateSnuMailException(details = mapOf("email" to "hong@snu.ac.kr"))

        performJsonPost("/api/auth/user", request)
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("USER_001"))
            .andExpect(jsonPath("$.details.email").value("hong@snu.ac.kr"))
    }

    @Test
    @DisplayName("회원 탈퇴 - 인증된 사용자를 그대로 서비스에 전달하고 성공 응답을 반환한다")
    fun deleteUserSuccess() {
        authenticateAs(applicantUser)
        every { authService.withdrawUser(applicantUser) } just Runs

        mockMvc.perform(withAuth(delete("/api/auth/user")))
            .andExpect(status().isOk)
            .andExpect(content().string(""))

        verify(exactly = 1) { authService.withdrawUser(applicantUser) }
    }

    @Test
    @DisplayName("회원 탈퇴 - 서비스가 권한 없음 예외를 던지면 에러 응답으로 변환된다")
    fun deleteUserNotAuthorizedFailure() {
        authenticateAs(companyUser)
        every { authService.withdrawUser(companyUser) } throws NotAuthorizedException(details = mapOf("userId" to companyUser.id))

        mockMvc.perform(withAuth(delete("/api/auth/user")))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("AUTH_007"))
            .andExpect(jsonPath("$.details.userId").value(companyUser.id))
    }

    @Test
    @DisplayName("로그인 - 성공하면 access token과 refresh cookie를 반환한다")
    fun signInSuccess() {
        val request = SignInRequest(email = "applicant@snu.ac.kr", password = "Password123!")
        val tokens = Tokens(accessToken = "access-token", refreshToken = "refresh-token")
        every { authService.signIn(match { it.email == request.email && it.password == request.password }) } returns (applicantUserSimple to tokens)

        performJsonPost("/api/auth/user/session", request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.user.id").value(applicantUser.id))
            .andExpect(jsonPath("$.token").value("access-token"))
            .andExpect(header().string("Set-Cookie", containsString("refresh_token=refresh-token")))

        verify(exactly = 1) { authService.signIn(match { it.email == request.email && it.password == request.password }) }
    }

    @Test
    @DisplayName("로그인 - 잘못된 JSON은 JSON 파싱 오류로 응답한다")
    fun signInInvalidJsonFailure() {
        val invalidJson = """{"email":"applicant@snu.ac.kr"""

        mockMvc.perform(
            post("/api/auth/user/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_005"))
            .andExpect(jsonPath("$.message").value("Malformed JSON request."))
    }

    @Test
    @DisplayName("로그인 - 서비스가 인증 실패 예외를 던지면 에러 응답으로 변환된다")
    fun signInInvalidCredentialsFailure() {
        val request = SignInRequest(email = "applicant@snu.ac.kr", password = "wrong-password")
        every { authService.signIn(any()) } throws InvalidCredentialsException(details = mapOf("email" to request.email))

        performJsonPost("/api/auth/user/session", request)
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("AUTH_002"))
            .andExpect(jsonPath("$.details.email").value(request.email))
    }

    @Test
    @DisplayName("로그아웃 - refresh token이 없으면 바로 400을 반환한다")
    fun signOutMissingRefreshTokenFailure() {
        mockMvc.perform(withAuth(delete("/api/auth/user/session")))
            .andExpect(status().isBadRequest)
            .andExpect(content().string(""))

        verify(exactly = 0) { authService.signOut(any(), any()) }
    }

    @Test
    @DisplayName("로그아웃 - 성공하면 refresh cookie를 삭제한다")
    fun signOutSuccess() {
        every { authService.signOut(applicantUser, "refresh-token") } just Runs

        mockMvc.perform(
            withAuth(
                delete("/api/auth/user/session")
                    .cookie(Cookie("refresh_token", "refresh-token")),
            ),
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Set-Cookie", containsString("refresh_token=")))
            .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))

        verify(exactly = 1) { authService.signOut(applicantUser, "refresh-token") }
    }

    @Test
    @DisplayName("로그아웃 - 서비스가 refresh token 불일치 예외를 던지면 에러 응답으로 변환된다")
    fun signOutTokenMismatchFailure() {
        every { authService.signOut(any(), "refresh-token") } throws InvalidRefreshTokenException(details = mapOf("refreshToken" to "refresh-token"))

        mockMvc.perform(
            withAuth(
                delete("/api/auth/user/session")
                    .cookie(Cookie("refresh_token", "refresh-token")),
            ),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("AUTH_003"))
            .andExpect(jsonPath("$.details.refreshToken").value("refresh-token"))
    }

    @Test
    @DisplayName("토큰 재발급 - refresh token이 없으면 바로 400을 반환한다")
    fun refreshAccessTokenMissingRefreshTokenFailure() {
        mockMvc.perform(get("/api/auth/token"))
            .andExpect(status().isBadRequest)
            .andExpect(content().string(""))

        verify(exactly = 0) { authService.refreshAccessToken(any()) }
    }

    @Test
    @DisplayName("토큰 재발급 - 성공하면 새 access token과 refresh cookie를 반환한다")
    fun refreshAccessTokenSuccess() {
        val tokens = Tokens(accessToken = "new-access-token", refreshToken = "new-refresh-token")
        every { authService.refreshAccessToken("refresh-token") } returns tokens

        mockMvc.perform(
            get("/api/auth/token")
                .cookie(Cookie("refresh_token", "refresh-token")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("new-access-token"))
            .andExpect(header().string("Set-Cookie", containsString("refresh_token=new-refresh-token")))

        verify(exactly = 1) { authService.refreshAccessToken("refresh-token") }
    }

    @Test
    @DisplayName("토큰 재발급 - 서비스가 refresh token 예외를 던지면 에러 응답으로 변환된다")
    fun refreshAccessTokenInvalidTokenFailure() {
        every { authService.refreshAccessToken("refresh-token") } throws InvalidRefreshTokenException(details = mapOf("refreshToken" to "refresh-token"))

        mockMvc.perform(
            get("/api/auth/token")
                .cookie(Cookie("refresh_token", "refresh-token")),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("AUTH_003"))
            .andExpect(jsonPath("$.details.refreshToken").value("refresh-token"))
    }

    @Test
    @DisplayName("이메일 중복 확인 - 성공하면 200을 반환한다")
    fun checkDuplicateMailSuccess() {
        every { authService.checkDuplicateMail(any()) } just Runs

        performJsonPost("/api/auth/mail", EmailRequest(email = "new@example.com"))
            .andExpect(status().isOk)

        verify(exactly = 1) { authService.checkDuplicateMail(match { it.email == "new@example.com" }) }
    }

    @Test
    @DisplayName("이메일 중복 확인 - validation error를 반환한다")
    fun checkDuplicateMailValidationFailure() {
        performJsonPost("/api/auth/mail", EmailRequest(email = ""))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
            .andExpect(jsonPath("$.details.email").exists())

        verify(exactly = 0) { authService.checkDuplicateMail(any()) }
    }

    @Test
    @DisplayName("이메일 중복 확인 - 서비스가 중복 예외를 던지면 에러 응답으로 변환된다")
    fun checkDuplicateMailDuplicateFailure() {
        every { authService.checkDuplicateMail(any()) } throws UserDuplicateSnuMailException(details = mapOf("email" to "new@example.com"))

        performJsonPost("/api/auth/mail", EmailRequest(email = "new@example.com"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("USER_001"))
            .andExpect(jsonPath("$.details.email").value("new@example.com"))
    }

    @Test
    @DisplayName("스누메일 인증 코드 발송 - 성공하면 200을 반환한다")
    fun sendSnuMailVerificationSuccess() {
        every { authService.sendSnuMailVerification(any()) } just Runs

        performJsonPost("/api/auth/mail/verify", SnuMailRequest(snuMail = "student@snu.ac.kr"))
            .andExpect(status().isOk)

        verify(exactly = 1) { authService.sendSnuMailVerification(match { it.snuMail == "student@snu.ac.kr" }) }
    }

    @Test
    @DisplayName("스누메일 인증 코드 발송 - 서비스가 메일 발송 실패 예외를 던지면 에러 응답으로 변환된다")
    fun sendSnuMailVerificationFailure() {
        every { authService.sendSnuMailVerification(any()) } throws EmailSendFailureException(details = mapOf("snuMail" to "student@snu.ac.kr"))

        performJsonPost("/api/auth/mail/verify", SnuMailRequest(snuMail = "student@snu.ac.kr"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.code").value("EMAIL_001"))
            .andExpect(jsonPath("$.details.snuMail").value("student@snu.ac.kr"))
    }

    @Test
    @DisplayName("스누메일 인증 코드 검증 - 성공하면 successCode를 반환한다")
    fun checkSnuMailVerificationSuccess() {
        every { authService.checkSnuMailVerification(any()) } returns "success-code"

        performJsonPost(
            "/api/auth/mail/validate",
            CheckSnuMailVerificationRequest(snuMail = "student@snu.ac.kr", code = "123456"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.successCode").value("success-code"))

        verify(exactly = 1) {
            authService.checkSnuMailVerification(match { it.snuMail == "student@snu.ac.kr" && it.code == "123456" })
        }
    }

    @Test
    @DisplayName("스누메일 인증 코드 검증 - 서비스가 실패 예외를 던지면 에러 응답으로 변환된다")
    fun checkSnuMailVerificationFailure() {
        every { authService.checkSnuMailVerification(any()) } throws UserEmailVerificationInvalidException(details = mapOf("snuMail" to "student@snu.ac.kr"))

        performJsonPost(
            "/api/auth/mail/validate",
            CheckSnuMailVerificationRequest(snuMail = "student@snu.ac.kr", code = "123456"),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("USER_006"))
            .andExpect(jsonPath("$.details.snuMail").value("student@snu.ac.kr"))
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공하면 200을 반환한다")
    fun changePasswordSuccess() {
        val request = ChangePasswordRequest(oldPassword = "OldPassword123!", newPassword = "NewPassword123!")
        every { authService.changePassword(applicantUser, request) } just Runs

        performJsonPasswordPatch(request)
            .andExpect(status().isOk)

        verify(exactly = 1) { authService.changePassword(applicantUser, request) }
    }

    @Test
    @DisplayName("비밀번호 변경 - validation error를 반환한다")
    fun changePasswordValidationFailure() {
        val request = ChangePasswordRequest(oldPassword = "old", newPassword = "short")

        performJsonPasswordPatch(request)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
            .andExpect(jsonPath("$.details.oldPassword").value("Password must be between 8 and 64 characters."))
            .andExpect(jsonPath("$.details.newPassword").value("Password must be between 8 and 64 characters."))

        verify(exactly = 0) { authService.changePassword(any(), any()) }
    }

    @Test
    @DisplayName("비밀번호 변경 - 서비스가 인증 실패 예외를 던지면 에러 응답으로 변환된다")
    fun changePasswordInvalidCredentialsFailure() {
        val request = ChangePasswordRequest(oldPassword = "wrong-password", newPassword = "NewPassword123!")
        every { authService.changePassword(any(), request) } throws InvalidCredentialsException(details = mapOf("oldPassword" to request.oldPassword))

        performJsonPasswordPatch(request)
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("AUTH_002"))
            .andExpect(jsonPath("$.details.oldPassword").value("wrong-password"))
    }

    @Test
    @DisplayName("임시 비밀번호 발급 - 성공하면 200을 반환한다")
    fun resetPasswordSuccess() {
        every { authService.resetPassword(any()) } just Runs

        performJsonPost("/api/auth/password", EmailRequest(email = "applicant@snu.ac.kr"))
            .andExpect(status().isOk)

        verify(exactly = 1) { authService.resetPassword(match { it.email == "applicant@snu.ac.kr" }) }
    }

    @Test
    @DisplayName("임시 비밀번호 발급 - 서비스가 사용자 없음 예외를 던지면 에러 응답으로 변환된다")
    fun resetPasswordUserNotFoundFailure() {
        every { authService.resetPassword(any()) } throws UserNotFoundException(details = mapOf("email" to "missing@example.com"))

        performJsonPost("/api/auth/password", EmailRequest(email = "missing@example.com"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("USER_004"))
            .andExpect(jsonPath("$.details.email").value("missing@example.com"))
    }

    @Test
    @DisplayName("내 정보 조회 - 인증된 사용자를 그대로 반환한다")
    fun getUserInfoSuccess() {
        authenticateAs(companyUser)

        mockMvc.perform(withAuth(get("/api/auth/me")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(companyUser.id))
            .andExpect(jsonPath("$.name").value(companyUser.name))
            .andExpect(jsonPath("$.userRole").value("COMPANY"))
            .andExpect(jsonPath("$.email").value(companyUser.email))
    }

    @Test
    @DisplayName("잘못된 JSON은 GlobalExceptionHandler의 INVALID_JSON 응답으로 변환된다")
    fun invalidJsonFailure() {
        val invalidJson = """{"""

        mockMvc.perform(
            post("/api/auth/mail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_005"))
            .andExpect(jsonPath("$.message").value("Malformed JSON request."))
    }

    private fun performJsonPost(
        path: String,
        body: Any,
    ): ResultActions =
        mockMvc.perform(
            post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)),
        )

    private fun performJsonPasswordPatch(body: Any): ResultActions =
        mockMvc.perform(
            withAuth(
                patch("/api/auth/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ),
        )

    private fun authenticateAs(user: User) {
        every { authService.authenticate(currentToken) } returns user
    }

    private fun withAuth(builder: MockHttpServletRequestBuilder): MockHttpServletRequestBuilder =
        builder.header("Authorization", "Bearer $currentToken")
}
