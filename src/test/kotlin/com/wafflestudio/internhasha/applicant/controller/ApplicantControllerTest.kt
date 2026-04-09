package com.wafflestudio.internhasha.applicant.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.internhasha.applicant.ApplicantNotFoundException
import com.wafflestudio.internhasha.applicant.ApplicantUserForbiddenException
import com.wafflestudio.internhasha.applicant.application.service.ApplicantService
import com.wafflestudio.internhasha.applicant.persistence.ApplicantEntity
import com.wafflestudio.internhasha.applicant.web.dto.ApplicantResponse
import com.wafflestudio.internhasha.applicant.web.dto.PutApplicantRequest
import com.wafflestudio.internhasha.auth.application.service.AuthService
import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.persistence.UserEntity
import com.wafflestudio.internhasha.common.domain.Link
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.verify
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class ApplicantControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var applicantService: ApplicantService

    @MockkBean
    private lateinit var authService: AuthService

    private var currentToken = "applicant-token"

    private val applicantUser =
        User(
            id = "applicant-user-1",
            name = "지원자",
            userRole = UserRole.ROLE_APPLICANT,
            email = "applicant@snu.ac.kr",
        )
    private val companyUser =
        User(
            id = "company-user-1",
            name = "기업회원",
            userRole = UserRole.ROLE_COMPANY,
            email = "company@example.com",
        )

    private val userEntity =
        UserEntity(
            id = applicantUser.id,
            name = applicantUser.name,
            email = applicantUser.email!!,
            passwordHash = "hashed-password",
            createdAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            updatedAt = LocalDateTime.of(2024, 1, 2, 0, 0),
            userRole = UserRole.ROLE_APPLICANT,
        )

    private val applicantEntity =
        ApplicantEntity(
            id = "applicant-entity-1",
            user = userEntity,
            createdAt = LocalDateTime.of(2024, 1, 3, 0, 0),
            updatedAt = LocalDateTime.of(2024, 1, 4, 0, 0),
            enrollYear = 2020,
            dept = "컴퓨터공학",
            positions = listOf("프론트엔드", "백엔드"),
            slogan = "함께 성장합니다",
            explanation = "설명",
            stacks = listOf("Kotlin", "Spring Boot"),
            profileImageKey = "profile-key",
            cvKey = "cv-key",
            portfolioKey = "portfolio-key",
            links = listOf(Link(description = "GitHub", link = "https://github.com/example")),
        )

    private val applicantResponse = ApplicantResponse.fromEntity(applicantEntity)

    @BeforeEach
    fun setUp() {
        clearMocks(applicantService, authService)
        authenticateAs(applicantUser)
    }

    @Test
    @DisplayName("지원자 조회 - 성공하면 ApplicantResponse를 반환한다")
    fun getApplicantSuccess() {
        every { applicantService.getApplicant(applicantUser) } returns applicantResponse

        mockMvc.perform(withAuth(get("/api/applicant/me")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(applicantUser.id))
            .andExpect(jsonPath("$.name").value(applicantUser.name))
            .andExpect(jsonPath("$.userRole").value("APPLICANT"))
            .andExpect(jsonPath("$.email").value(applicantUser.email))
            .andExpect(jsonPath("$.enrollYear").value(2020))
            .andExpect(jsonPath("$.department").value("컴퓨터공학"))
            .andExpect(jsonPath("$.cvKey").value("cv-key"))
            .andExpect(jsonPath("$.links[0].description").value("GitHub"))

        verify(exactly = 1) { applicantService.getApplicant(applicantUser) }
    }

    @Test
    @DisplayName("지원자 조회 - 회사 계정이면 권한 없음 에러를 반환한다")
    fun getApplicantForbiddenFailure() {
        authenticateAs(companyUser)
        every { applicantService.getApplicant(companyUser) } throws ApplicantUserForbiddenException(details = mapOf("userId" to companyUser.id, "userRole" to companyUser.userRole))

        mockMvc.perform(withAuth(get("/api/applicant/me")))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("APPLICANT_001"))
            .andExpect(jsonPath("$.details.userId").value(companyUser.id))
            .andExpect(jsonPath("$.details.userRole").value("COMPANY"))
    }

    @Test
    @DisplayName("지원자 조회 - 지원자 정보가 없으면 not found 에러를 반환한다")
    fun getApplicantNotFoundFailure() {
        every { applicantService.getApplicant(applicantUser) } throws ApplicantNotFoundException()

        mockMvc.perform(withAuth(get("/api/applicant/me")))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("APPLICANT_002"))
    }

    @Test
    @DisplayName("지원자 정보 수정 - 성공하면 ApplicantResponse를 반환한다")
    fun putApplicantSuccess() {
        val request =
            PutApplicantRequest(
                enrollYear = 2021,
                department = "소프트웨어",
                positions = listOf("BE", "FE"),
                slogan = "좋은 팀",
                explanation = "자기소개",
                stacks = listOf("Kotlin", "JPA"),
                imageKey = "image-key",
                cvKey = "cv-key",
                portfolioKey = "portfolio-key",
                links = listOf(Link(description = "Blog", link = "https://blog.example.com")),
            )
        val response =
            ApplicantResponse(
                id = applicantUser.id,
                name = applicantUser.name,
                createdAt = LocalDateTime.of(2024, 1, 3, 0, 0),
                updatedAt = LocalDateTime.of(2024, 1, 4, 0, 0),
                userRole = UserRole.ROLE_APPLICANT,
                email = applicantUser.email,
                enrollYear = request.enrollYear,
                department = request.department,
                positions = request.positions,
                slogan = request.slogan,
                explanation = request.explanation,
                stacks = request.stacks,
                imageKey = request.imageKey,
                cvKey = request.cvKey,
                portfolioKey = request.portfolioKey,
                links = request.links,
            )
        every { applicantService.putApplicant(applicantUser, request) } returns response

        performJsonPut(request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(applicantUser.id))
            .andExpect(jsonPath("$.department").value("소프트웨어"))
            .andExpect(jsonPath("$.cvKey").value("cv-key"))
            .andExpect(jsonPath("$.links[0].description").value("Blog"))

        verify(exactly = 1) { applicantService.putApplicant(applicantUser, request) }
    }

    @Test
    @DisplayName("지원자 정보 수정 - validation 실패 시 validation error를 반환한다")
    fun putApplicantValidationFailure() {
        val invalidJson =
            """
            {
              "enrollYear": 999,
              "department": "",
              "positions": ["BE"],
              "slogan": "",
              "explanation": "",
              "stacks": ["Kotlin"],
              "imageKey": "image-key",
              "cvKey": "",
              "portfolioKey": "portfolio-key",
              "links": []
            }
            """.trimIndent()

        mockMvc.perform(
            withAuth(
                put("/api/applicant/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
            .andExpect(jsonPath("$.details.enrollYear").value("enrollYear must be a valid 4-digit integer"))
            .andExpect(jsonPath("$.details.department").value("department is required"))

        verify(exactly = 0) { applicantService.putApplicant(any(), any()) }
    }

    @Test
    @DisplayName("지원자 정보 수정 - 잘못된 JSON은 파싱 오류를 반환한다")
    fun putApplicantInvalidJsonFailure() {
        val invalidJson = "{"

        mockMvc.perform(
            withAuth(
                put("/api/applicant/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_005"))
            .andExpect(jsonPath("$.message").value("Malformed JSON request."))
    }

    private fun authenticateAs(user: User) {
        currentToken = "${user.userRole.name.lowercase()}-${user.id}"
        every { authService.authenticate(currentToken) } returns user
    }

    private fun withAuth(builder: MockHttpServletRequestBuilder): MockHttpServletRequestBuilder =
        builder.header("Authorization", "Bearer $currentToken")

    private fun performJsonPut(body: Any): ResultActions =
        mockMvc.perform(
            withAuth(
                put("/api/applicant/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ),
        )
}
