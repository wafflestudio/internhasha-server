package com.wafflestudio.internhasha.company.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.applicant.repository.ApplicantRepository
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.security.TokenProvider
import com.wafflestudio.internhasha.auth.service.dto.UserSimple
import com.wafflestudio.internhasha.common.domain.LinkVo
import com.wafflestudio.internhasha.common.dto.LinkRequest
import com.wafflestudio.internhasha.company.controller.dto.CreateCompanyRequest
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.domain.Domain
import com.wafflestudio.internhasha.company.domain.Tag
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
class CompanyControllerTest {
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
                    UserSimple(id = "anonymous-id", userRole = UserRole.ROLE_COMPANY),
                ).accessToken
    }

    @Test
    @DisplayName("회사 조회 - 성공하면 Company를 반환한다")
    fun getCompanySuccess() {
        mockMvc
            .perform(withAuth(get("/api/company/me"), companyAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(companyId))
            .andExpect(jsonPath("$.companyName").value(companyUser.name))
    }

    @Test
    @DisplayName("회사 조회 - 지원자 계정이면 권한 없음 에러를 반환한다")
    fun getCompanyForbiddenFailure() {
        mockMvc
            .perform(withAuth(get("/api/company/me"), applicantAccessToken))
            .andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("회사 조회 - 회사 정보가 없으면 not found 에러를 반환한다")
    fun getCompanyNotFoundFailure() {
        mockMvc
            .perform(withAuth(get("/api/company/me"), anonymousAccessToken))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("USER_004"))
    }

    @Test
    @DisplayName("회사 정보 수정 - 성공하면 Company를 반환한다")
    fun putCompanySuccess() {
        val request =
            CreateCompanyRequest(
                companyEstablishedYear = 2018,
                domain = Domain.EDUCATION,
                headcount = 15,
                location = "서울",
                slogan = "같이 성장",
                detail = "회사 상세 설명",
                profileImageKey = "profile-key",
                companyInfoPDFKey = "pdf-key",
                landingPageLink = "https://company.example.com",
                links = listOf(LinkRequest(description = "Notion", link = "https://notion.so/example")),
                tags = listOf(Tag(tag = "교육")),
            )

        mockMvc
            .perform(
                withAuth(put("/api/company/me"), companyAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(companyId))
            .andExpect(jsonPath("$.companyName").value(companyUser.name))
    }

    @Test
    @DisplayName("회사 정보 수정 - validation 실패 시 validation error를 반환한다")
    fun putCompanyValidationFailure() {
        val invalidJson =
            """
            {
              "companyEstablishedYear": 1700,
              "domain": "EDUCATION",
              "headcount": 0,
              "location": "",
              "slogan": "",
              "detail": "",
              "profileImageKey": "",
              "companyInfoPDFKey": null,
              "landingPageLink": "not-a-url",
              "links": [],
              "tags": []
            }
            """.trimIndent()

        mockMvc
            .perform(
                withAuth(put("/api/company/me"), companyAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
            .andExpect(jsonPath("$.details.companyEstablishedYear").value("Established year must be after 1800."))
            .andExpect(jsonPath("$.details.headcount").value("Headcount must be at least 1."))
            .andExpect(jsonPath("$.details.location").value("Location is required."))
            .andExpect(jsonPath("$.details.slogan").value("Slogan is required."))
            .andExpect(jsonPath("$.details.detail").value("Detail description is required."))
            .andExpect(jsonPath("$.details.profileImageKey").value("Profile image key is required."))
            .andExpect(jsonPath("$.details.landingPageLink").value("Landing page link must be a valid URL."))
    }

    @Test
    @DisplayName("회사 정보 수정 - 잘못된 JSON은 파싱 오류를 반환한다")
    fun putCompanyInvalidJsonFailure() {
        val invalidJson = "{"

        mockMvc
            .perform(
                withAuth(put("/api/company/me"), companyAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_005"))
            .andExpect(jsonPath("$.message").value("Malformed JSON request."))
    }

    @Test
    @DisplayName("회사 정보 수정 - 사용자 정보가 없으면 not found 에러를 반환한다")
    fun putCompanyUserNotFoundFailure() {
        val request =
            CreateCompanyRequest(
                companyEstablishedYear = 2018,
                domain = Domain.EDUCATION,
                headcount = 15,
                location = "서울",
                slogan = "같이 성장",
                detail = "회사 상세 설명",
                profileImageKey = "profile-key",
                companyInfoPDFKey = "pdf-key",
                landingPageLink = "https://company.example.com",
                links = emptyList(),
                tags = emptyList(),
            )

        mockMvc
            .perform(
                withAuth(put("/api/company/me"), anonymousAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("USER_004"))
    }

    private fun withAuth(
        builder: MockHttpServletRequestBuilder,
        token: String,
    ): MockHttpServletRequestBuilder = builder.header("Authorization", "Bearer $token")
}
