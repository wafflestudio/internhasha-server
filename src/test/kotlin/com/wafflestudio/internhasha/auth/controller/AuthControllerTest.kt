package com.wafflestudio.internhasha.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.applicant.repository.ApplicantRepository
import com.wafflestudio.internhasha.auth.controller.dto.request.ChangePasswordRequest
import com.wafflestudio.internhasha.auth.controller.dto.request.CheckSnuMailVerificationRequest
import com.wafflestudio.internhasha.auth.controller.dto.request.EmailRequest
import com.wafflestudio.internhasha.auth.controller.dto.request.SignInRequest
import com.wafflestudio.internhasha.auth.controller.dto.request.SignUpRequest
import com.wafflestudio.internhasha.auth.controller.dto.request.SnuMailRequest
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.repository.AuthRedisRepository
import com.wafflestudio.internhasha.auth.repository.UserRepository
import com.wafflestudio.internhasha.auth.security.TokenProvider
import com.wafflestudio.internhasha.auth.service.dto.UserSimple
import com.wafflestudio.internhasha.common.domain.LinkVo
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.repository.CompanyRepository
import io.mockk.every
import io.mockk.justRun
import jakarta.servlet.http.Cookie
import jakarta.transaction.Transactional
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
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
@Transactional
@AutoConfigureMockMvc
class AuthControllerTest {
    private val objectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var applicantRepository: ApplicantRepository

    @Autowired
    lateinit var companyRepository: CompanyRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @MockkBean
    lateinit var authRedisRepository: AuthRedisRepository

    @Autowired
    lateinit var tokenProvider: TokenProvider

    private val hashedPassword: String = BCrypt.hashpw("password", BCrypt.gensalt())

    lateinit var applicantId: String
    lateinit var applicantAccessToken: String
    lateinit var applicantRefreshToken: String

    lateinit var applicantEntity: Applicant
    lateinit var companyEntity: Company

    @BeforeEach
    fun setUp() {
        objectMapper.registerKotlinModule()

        applicantEntity =
            Applicant(
                name = "지원자",
                email = "applicant@snu.ac.kr",
                enrollYear = 2020,
                dept = "컴퓨터공학",
                slogan = "함께 성장합니다",
                explanation = "설명",
                profileImageKey = "profile-key",
                cvKey = "cv-key",
                portfolioKey = "portfolio-key",
            ).apply {
                this.passwordHash = hashedPassword
                this.links.addAll(listOf(LinkVo(description = "GitHub", link = "https://github.com/example")))
            }

        val applicant = applicantRepository.save(applicantEntity)
        applicantId = applicant.requireId()

        val tokens =
            tokenProvider
                .generateTokens(
                    UserSimple(id = applicantId, userRole = UserRole.ROLE_APPLICANT),
                )
        applicantAccessToken = tokens.accessToken
        applicantRefreshToken = tokens.refreshToken

        companyEntity =
            Company(
                name = "기업회원",
                email = "company@example.com",
            ).apply {
                this.passwordHash = hashedPassword
            }
        companyRepository.save(companyEntity)

        justRun { authRedisRepository.saveRefreshToken(any(), any()) }
        justRun { authRedisRepository.saveEmailCode(any(), any()) }
        justRun { authRedisRepository.saveSuccessCode(any(), any()) }

        every { authRedisRepository.getRefreshToken(any()) } returns applicantRefreshToken
        every { authRedisRepository.deleteRefreshToken(any()) } returns true

        every { authRedisRepository.verifyEmailCode(any(), any()) } returns true
        every { authRedisRepository.deleteEmailCode(any()) } returns true

        every { authRedisRepository.verifySuccessCode(any(), any()) } returns true
        every { authRedisRepository.deleteSuccessCode(any()) } returns true
    }

    @Test
    @DisplayName("회원가입 - 지원자 계정 생성에 성공하면 토큰과 쿠키를 반환한다")
    fun signUpApplicantSuccess() {
        val beforeUserCount = userRepository.count()
        val beforeApplicantCount = applicantRepository.count()
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

        performJsonPost("/api/auth/user", request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.user.id").exists())
            .andExpect(jsonPath("$.user.userRole").value(UserRole.ROLE_APPLICANT.name))
            .andExpect(jsonPath("$.token").exists())
            .andExpect(header().string("Set-Cookie", containsString("refresh_token=")))
            .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))

        assertEquals(beforeUserCount + 1, userRepository.count())
        assertEquals(beforeApplicantCount + 1, applicantRepository.count())
        assertTrue(userRepository.existsByEmailAndDeletedAtIsNull("hong@snu.ac.kr"))
    }

    @Test
    @DisplayName("회원가입 - 기업 계정 생성에 성공하면 토큰과 쿠키를 반환한다")
    fun signUpCompanySuccess() {
        val beforeUserCount = userRepository.count()
        val beforeCompanyCount = companyRepository.count()
        val request =
            SignUpRequest(
                authType = UserRole.ROLE_COMPANY,
                info =
                    SignUpRequest.LocalCompanyInfo(
                        secretPassword = "Hello world",
                        name = "회사",
                        email = "company2@example.com",
                        password = "Password123!",
                        vcName = "벤처캐피탈",
                        vcRecommendation = "추천서",
                    ),
            )

        performJsonPost("/api/auth/user", request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.user.id").exists())
            .andExpect(jsonPath("$.user.userRole").value(UserRole.ROLE_COMPANY.name))
            .andExpect(jsonPath("$.token").exists())
            .andExpect(header().string("Set-Cookie", containsString("refresh_token=")))

        assertEquals(beforeUserCount + 1, userRepository.count())
        assertEquals(beforeCompanyCount + 1, companyRepository.count())
        assertTrue(userRepository.existsByEmailAndDeletedAtIsNull("company2@example.com"))
    }

    @Test
    @DisplayName("회원가입 - 잘못된 요청 바디는 validation error로 응답한다")
    fun signUpValidationFailure() {
        val beforeUserCount = userRepository.count()
        val beforeApplicantCount = applicantRepository.count()
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

        assertEquals(beforeUserCount, userRepository.count())
        assertEquals(beforeApplicantCount, applicantRepository.count())
        assertFalse(userRepository.existsByEmailAndDeletedAtIsNull("invalid-email"))
    }

    @Test
    @DisplayName("회원 탈퇴 - 인증된 사용자를 그대로 서비스에 전달하고 성공 응답을 반환한다")
    fun deleteUserSuccess() {
        mockMvc
            .perform(withAuth(delete("/api/auth/user"), applicantAccessToken))
            .andExpect(status().isOk)
            .andExpect(content().string(""))
    }

    @Test
    @DisplayName("로그인 - 성공하면 access token과 refresh cookie를 반환한다")
    fun signInSuccess() {
        val request = SignInRequest(email = applicantEntity.email, password = "password")

        performJsonPost("/api/auth/user/session", request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.user.id").exists())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(header().string("Set-Cookie", containsString("refresh_token=")))
    }

    @Test
    @DisplayName("로그인 - 잘못된 JSON은 JSON 파싱 오류로 응답한다")
    fun signInInvalidJsonFailure() {
        val invalidJson = """{"email":"applicant@snu.ac.kr"""

        mockMvc
            .perform(
                post("/api/auth/user/session")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_005"))
            .andExpect(jsonPath("$.message").value("Malformed JSON request."))
    }

    @Test
    @DisplayName("로그아웃 - refresh token이 없으면 바로 400을 반환한다")
    fun signOutMissingRefreshTokenFailure() {
        mockMvc
            .perform(withAuth(delete("/api/auth/user/session"), applicantAccessToken))
            .andExpect(status().isBadRequest)
            .andExpect(content().string(""))
    }

    @Test
    @DisplayName("로그아웃 - 성공하면 refresh cookie를 삭제한다")
    fun signOutSuccess() {
        mockMvc
            .perform(
                withAuth(
                    delete("/api/auth/user/session")
                        .cookie(Cookie("refresh_token", "refresh-token")),
                    applicantAccessToken,
                ),
            ).andExpect(status().isOk)
            .andExpect(header().string("Set-Cookie", containsString("refresh_token=")))
            .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
    }

    @Test
    @DisplayName("토큰 재발급 - refresh token이 없으면 바로 400을 반환한다")
    fun refreshAccessTokenMissingRefreshTokenFailure() {
        mockMvc
            .perform(get("/api/auth/token"))
            .andExpect(status().isBadRequest)
            .andExpect(content().string(""))
    }

    @Test
    @DisplayName("토큰 재발급 - 성공하면 새 access token과 refresh cookie를 반환한다")
    fun refreshAccessTokenSuccess() {
        mockMvc
            .perform(
                get("/api/auth/token")
                    .cookie(Cookie("refresh_token", applicantRefreshToken)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(header().string("Set-Cookie", containsString("refresh_token=")))
    }

    @Test
    @DisplayName("이메일 중복 확인 - 성공하면 200을 반환한다")
    fun checkDuplicateMailSuccess() {
        performJsonPost("/api/auth/mail", EmailRequest(email = "new@snu.ac.kr"))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("이메일 중복 확인 - validation error를 반환한다")
    fun checkDuplicateMailValidationFailure() {
        performJsonPost("/api/auth/mail", EmailRequest(email = ""))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
    }

    @Test
    @DisplayName("스누메일 인증 코드 발송 - 성공하면 200을 반환한다")
    fun sendSnuMailVerificationSuccess() {
        performJsonPost("/api/auth/mail/verify", SnuMailRequest(snuMail = "student@snu.ac.kr"))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("스누메일 인증 코드 검증 - 성공하면 successCode를 반환한다")
    fun checkSnuMailVerificationSuccess() {
        performJsonPost(
            "/api/auth/mail/validate",
            CheckSnuMailVerificationRequest(snuMail = "student@snu.ac.kr", code = "123456"),
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.successCode").exists())
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공하면 200을 반환한다")
    fun changePasswordSuccess() {
        val request = ChangePasswordRequest(oldPassword = "password", newPassword = "NewPassword123!")

        performJsonPasswordPatch(request)
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("비밀번호 변경 - validation error를 반환한다")
    fun changePasswordValidationFailure() {
        val request = ChangePasswordRequest(oldPassword = "old", newPassword = "short")

        performJsonPasswordPatch(request)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
    }

    @Test
    @DisplayName("내 정보 조회 - 인증된 사용자를 그대로 반환한다")
    fun getUserInfoSuccess() {
        mockMvc
            .perform(withAuth(get("/api/auth/me"), applicantAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(applicantId))
            .andExpect(jsonPath("$.name").value(applicantEntity.name))
            .andExpect(jsonPath("$.userRole").value(UserRole.ROLE_APPLICANT.name))
            .andExpect(jsonPath("$.email").value(applicantEntity.email))
    }

    @Test
    @DisplayName("잘못된 JSON은 GlobalExceptionHandler의 INVALID_JSON 응답으로 변환된다")
    fun invalidJsonFailure() {
        val invalidJson = """{"""

        mockMvc
            .perform(
                post("/api/auth/mail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ).andExpect(status().isBadRequest)
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
                applicantAccessToken,
            ),
        )

    private fun withAuth(
        builder: MockHttpServletRequestBuilder,
        token: String,
    ): MockHttpServletRequestBuilder = builder.header("Authorization", "Bearer $token")
}
