package com.wafflestudio.internhasha.post.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.applicant.repository.ApplicantRepository
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.repository.AuthRedisRepository
import com.wafflestudio.internhasha.auth.security.TokenProvider
import com.wafflestudio.internhasha.auth.service.dto.UserSimple
import com.wafflestudio.internhasha.common.domain.LinkVo
import com.wafflestudio.internhasha.common.domain.TagVo
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.domain.Domain
import com.wafflestudio.internhasha.company.repository.CompanyRepository
import com.wafflestudio.internhasha.post.controller.dto.request.CreatePositionRequest
import com.wafflestudio.internhasha.post.domain.Category
import io.mockk.clearMocks
import io.mockk.every
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
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class PostControllerTest {
    private val objectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var applicantRepository: ApplicantRepository

    @Autowired
    lateinit var companyRepository: CompanyRepository

    @Autowired
    lateinit var tokenProvider: TokenProvider

    @MockkBean
    lateinit var authRedisRepository: AuthRedisRepository

    lateinit var applicantId: String
    lateinit var companyId: String
    lateinit var applicantAccessToken: String
    lateinit var companyAccessToken: String

    @BeforeEach
    fun setUp() {
        clearMocks(authRedisRepository)

        val hashedPassword = BCrypt.hashpw("password", BCrypt.gensalt())

        val applicantEntity =
            Applicant(
                name = "지원자",
                email = "applicant@snu.ac.kr",
                enrollYear = 2020,
                dept = "컴퓨터공학",
                slogan = "함께 성장합니다",
                explanation = "설명",
                profileImageKey = "applicant-image-key",
                cvKey = "cv-key",
                portfolioKey = "portfolio-key",
            ).apply {
                this.passwordHash = hashedPassword
                this.links.addAll(listOf(LinkVo(description = "GitHub", link = "https://github.com/example")))
            }

        val savedApplicant = applicantRepository.save(applicantEntity)
        applicantId = savedApplicant.requireId()
        applicantAccessToken =
            tokenProvider.generateTokens(UserSimple(id = applicantId, userRole = UserRole.ROLE_APPLICANT)).accessToken

        val companyEntity =
            Company(
                name = "기업회원",
                email = "company@example.com",
            ).apply {
                this.passwordHash = hashedPassword
                this.companyEstablishedYear = 2018
                this.domain = Domain.B2B
                this.headcount = 42
                this.location = "서울"
                this.slogan = "성장하는 팀"
                this.detail = "회사 소개"
                this.profileImageKey = "company-image-key"
                this.companyInfoPDFKey = "company-pdf-key"
                this.landingPageLink = "https://company.example.com"
                this.links.addAll(listOf(LinkVo(description = "Homepage", link = "https://company.example.com")))
                this.tags.addAll(listOf(TagVo(tag = "스타트업")))
                this.vcName = "VC"
                this.vcRec = "추천"
            }

        val savedCompany = companyRepository.save(companyEntity)
        companyId = savedCompany.requireId()
        companyAccessToken =
            tokenProvider.generateTokens(UserSimple(id = companyId, userRole = UserRole.ROLE_COMPANY)).accessToken

        every { authRedisRepository.getRefreshToken(any()) } returns "refresh-token"
        every { authRedisRepository.deleteRefreshToken(any()) } returns true
        every { authRedisRepository.verifyEmailCode(any(), any()) } returns true
        every { authRedisRepository.deleteEmailCode(any()) } returns true
        every { authRedisRepository.verifySuccessCode(any(), any()) } returns true
        every { authRedisRepository.deleteSuccessCode(any()) } returns true
    }

    @Test
    @DisplayName("공고 상세 조회 - 비로그인 상태에서도 성공 응답을 반환한다")
    fun getPageDetailLoggedOutSuccess() {
        val positionId = createPosition()

        mockMvc
            .perform(get("/api/post/$positionId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(positionId))
            .andExpect(jsonPath("$.company.companyName").value("기업회원"))
            .andExpect(jsonPath("$.position.positionTitle").value("백엔드 개발자"))
            .andExpect(jsonPath("$.position.positionType").value("백엔드 개발"))
    }

    @Test
    @DisplayName("공고 상세 조회 - 로그인 상태에서도 성공 응답을 반환한다")
    fun getPageDetailLoggedInSuccess() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(get("/api/post/$positionId"), applicantAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(positionId))
            .andExpect(jsonPath("$.company.id").value(companyId))
    }

    @Test
    @DisplayName("공고 상세 조회 - 공고가 없으면 not found 에러를 반환한다")
    fun getPageDetailNotFoundFailure() {
        mockMvc
            .perform(get("/api/post/missing-post"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_001"))
    }

    @Test
    @DisplayName("공고 목록 조회 - 성공하면 PostWithPage를 반환한다")
    fun getPostsSuccess() {
        createPosition()

        mockMvc
            .perform(
                get("/api/post")
                    .param("roles", "FRONT", "BACKEND")
                    .param("domains", "B2B")
                    .param("isActive", "true")
                    .param("page", "0")
                    .param("order", "1"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.posts[0].id").exists())
            .andExpect(jsonPath("$.posts[0].companyName").value("기업회원"))
            .andExpect(jsonPath("$.posts[0].positionTitle").value("백엔드 개발자"))
            .andExpect(jsonPath("$.paginator.lastPage").exists())
    }

    @Test
    @DisplayName("북마크 추가 - 성공하면 200을 반환한다")
    fun addBookmarkSuccess() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(post("/api/post/$positionId/bookmark"), applicantAccessToken))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("북마크 추가 - 공고가 없으면 not found 에러를 반환한다")
    fun addBookmarkPostNotFoundFailure() {
        mockMvc
            .perform(withAuth(post("/api/post/missing-post/bookmark"), applicantAccessToken))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_001"))
    }

    @Test
    @DisplayName("북마크 삭제 - 성공하면 200을 반환한다")
    fun deleteBookmarkSuccess() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(post("/api/post/$positionId/bookmark"), applicantAccessToken))
            .andExpect(status().isOk)

        mockMvc
            .perform(withAuth(delete("/api/post/$positionId/bookmark"), applicantAccessToken))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("북마크 삭제 - 북마크가 없으면 에러를 반환한다")
    fun deleteBookmarkNotFoundFailure() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(delete("/api/post/$positionId/bookmark"), applicantAccessToken))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_003"))
            .andExpect(jsonPath("$.details.userId").value(applicantId))
            .andExpect(jsonPath("$.details.postId").value(positionId))
    }

    @Test
    @DisplayName("북마크 목록 조회 - 성공하면 PostWithPage를 반환한다")
    fun getBookmarksSuccess() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(post("/api/post/$positionId/bookmark"), applicantAccessToken))
            .andExpect(status().isOk)

        mockMvc
            .perform(withAuth(get("/api/post/bookmarks"), applicantAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts[0].id").value(positionId))
            .andExpect(jsonPath("$.posts[0].bookmarked").value(true))
            .andExpect(jsonPath("$.paginator.lastPage").exists())
    }

    @Test
    @DisplayName("공고 생성 - 성공하면 Position을 반환한다")
    fun createPositionSuccess() {
        val request = createPositionRequest()

        performJsonPositionPost(request, companyAccessToken)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.positionTitle").value("백엔드 개발자"))
            .andExpect(jsonPath("$.positionType").value("백엔드 개발"))
            .andExpect(jsonPath("$.headCount").value(3))
    }

    @Test
    @DisplayName("공고 생성 - validation 실패 시 validation error를 반환한다")
    fun createPositionValidationFailure() {
        val invalidJson =
            """
            {
              "positionTitle": "",
              "positionType": "BACKEND",
              "detail": "",
              "headCount": -1
            }
            """.trimIndent()

        mockMvc
            .perform(
                withAuth(
                    post("/api/post/position")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson),
                    companyAccessToken,
                ),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
            .andExpect(jsonPath("$.details.positionTitle").value("Position title is required."))
            .andExpect(jsonPath("$.details.detail").value("Position detail is required."))
            .andExpect(jsonPath("$.details.headCount").value("Headcount must be at least 0."))
    }

    @Test
    @DisplayName("공고 생성 - 잘못된 JSON은 파싱 오류를 반환한다")
    fun createPositionInvalidJsonFailure() {
        val invalidJson = "{"

        mockMvc
            .perform(
                withAuth(
                    post("/api/post/position")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson),
                    companyAccessToken,
                ),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_005"))
            .andExpect(jsonPath("$.message").value("Malformed JSON request."))
    }

    @Test
    @DisplayName("공고 생성 - 권한이 없으면 에러를 반환한다")
    fun createPositionForbiddenFailure() {
        performJsonPositionPost(createPositionRequest(), applicantAccessToken)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("403"))
    }

    @Test
    @DisplayName("회사 공고 목록 조회 - 성공하면 PostWithPage를 반환한다")
    fun getPostByCompanySuccess() {
        createPosition()

        mockMvc
            .perform(
                withAuth(
                    get("/api/post/position/me")
                        .param("roles", "BACKEND")
                        .param("domains", "B2B"),
                    companyAccessToken,
                ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.posts[0].id").exists())
            .andExpect(jsonPath("$.paginator.lastPage").exists())
    }

    @Test
    @DisplayName("회사 공고 목록 조회 - 지원자 계정이면 권한 없음 에러를 반환한다")
    fun getPostByCompanyForbiddenFailure() {
        mockMvc
            .perform(withAuth(get("/api/post/position/me"), applicantAccessToken))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("403"))
    }

    @Test
    @DisplayName("공고 수정 - 성공하면 Position을 반환한다")
    fun updatePositionSuccess() {
        val positionId = createPosition()
        val request =
            CreatePositionRequest(
                positionTitle = "백엔드 시니어",
                positionType = Category.BACKEND,
                detail = "수정 상세",
                headCount = 5,
                salary = 7000,
                employmentEndDate = LocalDateTime.of(2026, 11, 30, 23, 59),
            )

        performJsonPut("/api/post/position/$positionId", request, companyAccessToken)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(positionId))
            .andExpect(jsonPath("$.positionTitle").value("백엔드 시니어"))
            .andExpect(jsonPath("$.positionType").value("백엔드 개발"))
    }

    @Test
    @DisplayName("공고 수정 - 공고가 없으면 not found 에러를 반환한다")
    fun updatePositionNotFoundFailure() {
        val request =
            CreatePositionRequest(
                positionTitle = "백엔드 시니어",
                positionType = Category.BACKEND,
                detail = "수정 상세",
                headCount = 5,
                salary = 7000,
                employmentEndDate = LocalDateTime.of(2026, 11, 30, 23, 59),
            )

        performJsonPut("/api/post/position/missing-position", request, companyAccessToken)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_001"))
    }

    @Test
    @DisplayName("공고 즉시 마감 - 성공하면 200을 반환한다")
    fun closePositionSuccess() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(patch("/api/post/position/$positionId/close"), companyAccessToken))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("공고 즉시 마감 - 권한이 없으면 에러를 반환한다")
    fun closePositionForbiddenFailure() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(patch("/api/post/position/$positionId/close"), applicantAccessToken))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("403"))
    }

    @Test
    @DisplayName("공고 즉시 마감 - 공고가 없으면 not found 에러를 반환한다")
    fun closePositionNotFoundFailure() {
        mockMvc
            .perform(withAuth(patch("/api/post/position/missing-position/close"), companyAccessToken))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_001"))
    }

    @Test
    @DisplayName("내 북마크 공고 조회 - 성공하면 PostWithPage를 반환한다")
    fun getBookmarksPageSuccess() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(post("/api/post/$positionId/bookmark"), applicantAccessToken))
            .andExpect(status().isOk)

        mockMvc
            .perform(withAuth(get("/api/post/bookmarks"), applicantAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts[0].id").value(positionId))
            .andExpect(jsonPath("$.paginator.lastPage").exists())
    }

    private fun createPosition(): String {
        val result =
            performJsonPositionPost(createPositionRequest(), companyAccessToken)
                .andExpect(status().isOk)
                .andReturn()

        return objectMapper
            .readTree(result.response.contentAsString)
            .get("id")
            .asText()
    }

    private fun createPositionRequest(): CreatePositionRequest =
        CreatePositionRequest(
            positionTitle = "백엔드 개발자",
            positionType = Category.BACKEND,
            detail = "상세 내용",
            headCount = 3,
            salary = 5000,
            employmentEndDate = LocalDateTime.of(2026, 12, 31, 23, 59),
        )

    private fun performJsonPositionPost(
        body: Any,
        token: String,
    ): ResultActions =
        mockMvc.perform(
            withAuth(
                post("/api/post/position")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
                token,
            ),
        )

    private fun performJsonPut(
        path: String,
        body: Any,
        token: String,
    ): ResultActions =
        mockMvc.perform(
            withAuth(
                put(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
                token,
            ),
        )

    private fun withAuth(
        builder: MockHttpServletRequestBuilder,
        token: String,
    ): MockHttpServletRequestBuilder = builder.header("Authorization", "Bearer $token")
}
