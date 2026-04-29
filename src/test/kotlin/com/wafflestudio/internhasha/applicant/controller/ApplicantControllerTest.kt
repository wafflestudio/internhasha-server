package com.wafflestudio.internhasha.applicant.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.wafflestudio.internhasha.applicant.controller.dto.PutApplicantRequest
import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.applicant.repository.ApplicantRepository
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.security.TokenProvider
import com.wafflestudio.internhasha.auth.service.dto.UserSimple
import com.wafflestudio.internhasha.common.domain.LinkVo
import com.wafflestudio.internhasha.common.dto.LinkRequest
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.repository.CompanyRepository
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ApplicantControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    private val objectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()

    @Autowired
    lateinit var applicantRepository: ApplicantRepository

    @Autowired
    lateinit var companyRepository: CompanyRepository

    @Autowired
    lateinit var tokenProvider: TokenProvider

    val fixedPassword = "applicant-password"
    val hashedPassword: String = BCrypt.hashpw(fixedPassword, BCrypt.gensalt())

    lateinit var applicantId: String
    lateinit var applicantAccessToken: String

    lateinit var companyId: String
    lateinit var companyAccessToken: String

    lateinit var anonymousAccessToken: String

    data class UserDto(
        val id: String,
        val name: String,
        val userRole: UserRole,
        val email: String,
    )

    val applicantUser =
        UserDto(
            id = "applicant-id",
            name = "지원자",
            userRole = UserRole.ROLE_APPLICANT,
            email = "applicant@snu.ac.kr",
        )

    val companyUser =
        UserDto(
            id = "company-id",
            name = "기업회원",
            userRole = UserRole.ROLE_COMPANY,
            email = "company@example.com",
        )

    val applicantEntity =
        Applicant(
            name = applicantUser.name,
            email = applicantUser.email,
            enrollYear = 2020,
            dept = "컴퓨터공학",
            slogan = "함께 성장합니다",
            explanation = "설명",
            profileImageKey = "profile-key",
            cvKey = "cv-key",
            portfolioKey = "portfolio-key",
        ).apply {
            this.passwordHash = hashedPassword
            this.positions.addAll(listOf("프론트엔드", "백엔드"))
            this.stacks.addAll(listOf("Kotlin", "Spring Boot"))
            this.links.addAll(listOf(LinkVo(description = "GitHub", link = "https://github.com/example")))
        }

    val companyEntity =
        Company(
            name = companyUser.name,
            email = companyUser.email,
        ).apply {
            this.passwordHash = hashedPassword
        }

    @BeforeEach
    fun setUp() {
        val applicant = applicantRepository.save(applicantEntity)
        applicantId = applicant.requireId()

        applicantAccessToken =
            tokenProvider
                .generateTokens(
                    UserSimple(id = applicantId, userRole = UserRole.ROLE_APPLICANT),
                ).accessToken

        val company = companyRepository.save(companyEntity)
        companyId = company.requireId()

        companyAccessToken =
            tokenProvider
                .generateTokens(
                    UserSimple(id = companyId, userRole = UserRole.ROLE_COMPANY),
                ).accessToken

        anonymousAccessToken =
            tokenProvider
                .generateTokens(
                    UserSimple(id = "anonymous-id", userRole = UserRole.ROLE_APPLICANT),
                ).accessToken
    }

    @Test
    @DisplayName("지원자 조회 - 성공하면 ApplicantResponse를 반환한다")
    fun getApplicantSuccess() {
        mockMvc
            .perform(withAuth(get("/api/applicant/me"), applicantAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(applicantId))
            .andExpect(jsonPath("$.name").value(applicantUser.name))
            .andExpect(jsonPath("$.userRole").value(UserRole.ROLE_APPLICANT.name))
            .andExpect(jsonPath("$.email").value(applicantUser.email))
            .andExpect(jsonPath("$.enrollYear").value(2020))
            .andExpect(jsonPath("$.department").value("컴퓨터공학"))
            .andExpect(jsonPath("$.cvKey").value("cv-key"))
            .andExpect(jsonPath("$.links[0].description").value("GitHub"))
    }

    @Test
    @DisplayName("지원자 조회 - 회사 계정이면 권한 없음 에러를 반환한다")
    fun getApplicantForbiddenFailure() {
        mockMvc
            .perform(withAuth(get("/api/applicant/me"), companyAccessToken))
            .andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("지원자 조회 - 지원자 정보가 없으면 not found 에러를 반환한다")
    fun getApplicantNotFoundFailure() {
        mockMvc
            .perform(withAuth(get("/api/applicant/me"), anonymousAccessToken))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("USER_004"))
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
                links = listOf(LinkRequest(description = "Blog", link = "https://blog.example.com")),
            )

        mockMvc
            .perform(
                withAuth(put("/api/applicant/me"), applicantAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(applicantId))
            .andExpect(jsonPath("$.department").value("소프트웨어"))
            .andExpect(jsonPath("$.cvKey").value("cv-key"))
            .andExpect(jsonPath("$.links[0].description").value("Blog"))
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

        mockMvc
            .perform(
                withAuth(put("/api/applicant/me"), applicantAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
            .andExpect(jsonPath("$.details.enrollYear").value("enrollYear must be a valid 4-digit integer"))
            .andExpect(jsonPath("$.details.department").value("department is required"))
    }

    @Test
    @DisplayName("지원자 정보 수정 - 잘못된 JSON은 파싱 오류를 반환한다")
    fun putApplicantInvalidJsonFailure() {
        val invalidJson = "{"

        mockMvc
            .perform(
                withAuth(put("/api/applicant/me"), applicantAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_005"))
            .andExpect(jsonPath("$.message").value("Malformed JSON request."))
    }

    private fun withAuth(
        builder: MockHttpServletRequestBuilder,
        token: String,
    ): MockHttpServletRequestBuilder = builder.header("Authorization", "Bearer $token")
}
