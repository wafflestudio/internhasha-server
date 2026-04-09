package com.wafflestudio.internhasha.company.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.internhasha.auth.application.service.AuthService
import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.domain.UserNotFoundException
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.company.application.service.CompanyService
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.domain.CompanyNotFoundException
import com.wafflestudio.internhasha.company.domain.Domain
import com.wafflestudio.internhasha.company.domain.Tag
import com.wafflestudio.internhasha.company.dto.Link
import com.wafflestudio.internhasha.company.web.dto.CreateCompanyRequest
import com.wafflestudio.internhasha.exceptions.NotAuthorizedException
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
class CompanyControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var companyService: CompanyService

    @MockkBean
    private lateinit var authService: AuthService

    private var currentToken = "company-token"

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

    private val companyResponse =
        Company(
            id = "company-entity-1",
            companyName = companyUser.name,
            companyEstablishedYear = 2015,
            domain = Domain.B2B,
            headcount = 42,
            location = "서울",
            slogan = "성장하는 팀",
            detail = "회사 소개",
            profileImageKey = "profile-key",
            companyInfoPDFKey = "pdf-key",
            landingPageLink = "https://company.example.com",
            links = listOf(Link(description = "Homepage", link = "https://company.example.com")),
            tags = listOf(Tag(tag = "스타트업")),
            vcName = "VC",
            vcRec = "추천",
            createdAt = LocalDateTime.of(2024, 1, 3, 0, 0),
            updatedAt = LocalDateTime.of(2024, 1, 4, 0, 0),
        )

    @BeforeEach
    fun setUp() {
        clearMocks(companyService, authService)
        authenticateAs(companyUser)
    }

    @Test
    @DisplayName("회사 조회 - 성공하면 Company를 반환한다")
    fun getCompanySuccess() {
        every { companyService.getCompany(companyUser) } returns companyResponse

        mockMvc.perform(withAuth(get("/api/company/me")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(companyResponse.id))
            .andExpect(jsonPath("$.companyName").value(companyUser.name))
            .andExpect(jsonPath("$.domain").value("B2B"))
            .andExpect(jsonPath("$.headcount").value(42))
            .andExpect(jsonPath("$.links[0].description").value("Homepage"))
            .andExpect(jsonPath("$.tags[0].tag").value("스타트업"))

        verify(exactly = 1) { companyService.getCompany(companyUser) }
    }

    @Test
    @DisplayName("회사 조회 - 지원자 계정이면 권한 없음 에러를 반환한다")
    fun getCompanyForbiddenFailure() {
        authenticateAs(applicantUser)
        every { companyService.getCompany(applicantUser) } throws NotAuthorizedException()

        mockMvc.perform(withAuth(get("/api/company/me")))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("AUTH_007"))
    }

    @Test
    @DisplayName("회사 조회 - 회사 정보가 없으면 not found 에러를 반환한다")
    fun getCompanyNotFoundFailure() {
        every { companyService.getCompany(companyUser) } throws CompanyNotFoundException()

        mockMvc.perform(withAuth(get("/api/company/me")))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("COMPANY_001"))
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
                links = listOf(Link(description = "Notion", link = "https://notion.so/example")),
                tags = listOf(Tag(tag = "교육")),
            )
        every { companyService.putCompany(companyUser, request) } returns companyResponse

        performJsonPut(request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(companyResponse.id))
            .andExpect(jsonPath("$.companyName").value(companyUser.name))
            .andExpect(jsonPath("$.domain").value("B2B"))

        verify(exactly = 1) { companyService.putCompany(companyUser, request) }
    }

    @Test
    @DisplayName("회사 정보 수정 - validation 실패 시 validation error를 반환한다")
    fun putCompanyValidationFailure() {
        val request =
            CreateCompanyRequest(
                companyEstablishedYear = 1700,
                domain = Domain.OTHERS,
                headcount = 0,
                location = "",
                slogan = "",
                detail = "",
                profileImageKey = "",
                companyInfoPDFKey = null,
                landingPageLink = "not-a-url",
                links = emptyList(),
                tags = emptyList(),
            )

        performJsonPut(request)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
            .andExpect(jsonPath("$.details.companyEstablishedYear").value("Established year must be after 1800."))
            .andExpect(jsonPath("$.details.headcount").value("Headcount must be at least 1."))
            .andExpect(jsonPath("$.details.location").value("Location is required."))
            .andExpect(jsonPath("$.details.slogan").value("Slogan is required."))
            .andExpect(jsonPath("$.details.detail").value("Detail description is required."))
            .andExpect(jsonPath("$.details.profileImageKey").value("Profile image key is required."))
            .andExpect(jsonPath("$.details.landingPageLink").value("Landing page link must be a valid URL."))

        verify(exactly = 0) { companyService.putCompany(any(), any()) }
    }

    @Test
    @DisplayName("회사 정보 수정 - 잘못된 JSON은 파싱 오류를 반환한다")
    fun putCompanyInvalidJsonFailure() {
        val invalidJson = "{"

        mockMvc.perform(
            withAuth(
                put("/api/company/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_005"))
            .andExpect(jsonPath("$.message").value("Malformed JSON request."))
    }

    @Test
    @DisplayName("회사 정보 수정 - 사용자 정보가 없으면 not found 에러를 반환한다")
    fun putCompanyUserNotFoundFailure() {
        every { companyService.putCompany(any(), any()) } throws UserNotFoundException(mapOf("userId" to companyUser.id))

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

        performJsonPut(request)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("USER_004"))
            .andExpect(jsonPath("$.details.userId").value(companyUser.id))
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
                put("/api/company/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ),
        )
}
