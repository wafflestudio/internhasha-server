package com.wafflestudio.internhasha.post.controller

import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.applicant.repository.ApplicantRepository
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.repository.AuthRedisRepository
import com.wafflestudio.internhasha.auth.security.TokenProvider
import com.wafflestudio.internhasha.auth.service.dto.UserSimple
import com.wafflestudio.internhasha.common.domain.LinkVo
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.domain.Domain
import com.wafflestudio.internhasha.company.repository.CompanyRepository
import com.wafflestudio.internhasha.post.domain.Category
import com.wafflestudio.internhasha.post.domain.Position
import com.wafflestudio.internhasha.post.repository.BookmarkRepository
import com.wafflestudio.internhasha.post.repository.PositionRepository
import io.mockk.clearMocks
import io.mockk.every
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class BookmarkControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var applicantRepository: ApplicantRepository

    @Autowired
    lateinit var companyRepository: CompanyRepository

    @Autowired
    lateinit var positionRepository: PositionRepository

    @Autowired
    lateinit var bookmarkRepository: BookmarkRepository

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
        SecurityContextHolder.clearContext()
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
    @DisplayName("북마크 추가 - 성공하면 200을 반환한다")
    fun addBookmark() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(post("/api/post/$positionId/bookmark"), applicantAccessToken))
            .andExpect(status().isOk)

        assertEquals(1, bookmarkRepository.findAllByApplicantId(applicantId).size)
    }

    @Test
    @DisplayName("북마크 추가 - 공고가 없으면 not found 에러를 반환한다")
    fun addBookmarkPostNotFoundFailure() {
        mockMvc
            .perform(withAuth(post("/api/post/missing-post/bookmark"), applicantAccessToken))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_001"))
            .andExpect(jsonPath("$.details.postId").value("missing-post"))
    }

    @Test
    @DisplayName("북마크 추가 - 이미 북마크된 공고면 conflict 에러를 반환한다")
    fun addBookmarkAlreadyBookmarkedFailure() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(post("/api/post/$positionId/bookmark"), applicantAccessToken))
            .andExpect(status().isOk)

        mockMvc
            .perform(withAuth(post("/api/post/$positionId/bookmark"), applicantAccessToken))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("POST_002"))
            .andExpect(jsonPath("$.details.userId").value(applicantId))
            .andExpect(jsonPath("$.details.postId").value(positionId))
    }

    @Test
    @DisplayName("북마크 추가 - 기업 계정은 접근할 수 없다")
    fun addBookmarkForbiddenFailure() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(post("/api/post/$positionId/bookmark"), companyAccessToken))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("403"))
    }

    @Test
    @DisplayName("북마크 삭제 - 성공하면 200을 반환한다")
    fun deleteBookmark() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(post("/api/post/$positionId/bookmark"), applicantAccessToken))
            .andExpect(status().isOk)

        mockMvc
            .perform(withAuth(delete("/api/post/$positionId/bookmark"), applicantAccessToken))
            .andExpect(status().isOk)

        assertTrue(bookmarkRepository.findAllByApplicantId(applicantId).isEmpty())
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
    @DisplayName("북마크 삭제 - 기업 계정은 접근할 수 없다")
    fun deleteBookmarkForbiddenFailure() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(delete("/api/post/$positionId/bookmark"), companyAccessToken))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("403"))
    }

    @Test
    @DisplayName("북마크 목록 조회 - 성공하면 PostWithPage를 반환한다")
    fun getBookMarks() {
        val positionId = createPosition()

        mockMvc
            .perform(withAuth(post("/api/post/$positionId/bookmark"), applicantAccessToken))
            .andExpect(status().isOk)

        mockMvc
            .perform(withAuth(get("/api/post/bookmarks"), applicantAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts[0].id").value(positionId))
            .andExpect(jsonPath("$.posts[0].companyName").value("기업회원"))
            .andExpect(jsonPath("$.posts[0].positionTitle").value("백엔드 개발자"))
            .andExpect(jsonPath("$.posts[0].bookmarked").value(true))
            .andExpect(jsonPath("$.paginator.lastPage").exists())
    }

    @Test
    @DisplayName("북마크 목록 조회 - 잘못된 page 값이면 validation error를 반환한다")
    fun getBookmarksValidationFailure() {
        mockMvc
            .perform(withAuth(get("/api/post/bookmarks").param("page", "-1"), applicantAccessToken))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
    }

    @Test
    @DisplayName("북마크 목록 조회 - 기업 계정은 접근할 수 없다")
    fun getBookmarksForbiddenFailure() {
        mockMvc
            .perform(withAuth(get("/api/post/bookmarks"), companyAccessToken))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("403"))
    }

    private fun createPosition(): String {
        val position =
            Position(
                positionTitle = "백엔드 개발자",
                positionType = Category.BACKEND,
                detail = "상세 내용",
                headCount = 3,
                employmentEndDate = LocalDateTime.of(2026, 12, 31, 23, 59),
                salary = 5000,
                company = companyRepository.findById(companyId).orElseThrow(),
            )

        return positionRepository.save(position).requireId()
    }

    private fun withAuth(
        builder: MockHttpServletRequestBuilder,
        token: String,
    ): MockHttpServletRequestBuilder = builder.header("Authorization", "Bearer $token")
}
