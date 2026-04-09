package com.wafflestudio.internhasha.post.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.internhasha.auth.application.service.AuthService
import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.domain.UserNotFoundException
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.domain.Domain
import com.wafflestudio.internhasha.company.domain.Tag
import com.wafflestudio.internhasha.company.dto.Link
import com.wafflestudio.internhasha.exceptions.NotAuthorizedException
import com.wafflestudio.internhasha.post.application.service.PostService
import com.wafflestudio.internhasha.post.domain.AuthorBrief
import com.wafflestudio.internhasha.post.domain.Category
import com.wafflestudio.internhasha.post.domain.Position
import com.wafflestudio.internhasha.post.domain.Post
import com.wafflestudio.internhasha.post.domain.PostBookmarkNotFoundException
import com.wafflestudio.internhasha.post.domain.PostCompanyNotFoundException
import com.wafflestudio.internhasha.post.domain.PostNotFoundException
import com.wafflestudio.internhasha.post.domain.PostPositionNotFoundException
import com.wafflestudio.internhasha.post.web.dto.CreatePositionRequest
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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
@AutoConfigureMockMvc
class PostControllerTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var postService: PostService

    @MockkBean
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val currentToken = "access-token"

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

    private val samplePost =
        Post(
            id = "post-1",
            author = AuthorBrief(id = companyUser.id, name = companyUser.name, profileImageKey = "author-image-key"),
            company = sampleCompany(),
            position = samplePosition(),
            isBookmarked = true,
            coffeeChatCount = 3,
        )

    private val anotherPost =
        Post(
            id = "post-2",
            author = AuthorBrief(id = companyUser.id, name = companyUser.name, profileImageKey = "author-image-key"),
            company = sampleCompany(companyName = "다른 회사", companyId = "company-2"),
            position = samplePosition(positionId = "position-2", positionTitle = "앱 개발자", positionType = Category.APP),
            isBookmarked = false,
            coffeeChatCount = 0,
        )

    @BeforeEach
    fun setUp() {
        clearMocks(postService, authService)
        authenticateAs(applicantUser)
    }

    @Test
    @DisplayName("공고 상세 조회 - 비로그인 상태에서도 성공 응답을 반환한다")
    fun getPageDetailLoggedOutSuccess() {
        every { postService.getPageDetail(null, "post-1") } returns samplePost

        mockMvc.perform(get("/api/post/post-1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("post-1"))
            .andExpect(jsonPath("$.author.name").value(companyUser.name))
            .andExpect(jsonPath("$.company.companyName").value(samplePost.company.companyName))
            .andExpect(jsonPath("$.position.positionTitle").value(samplePost.position.positionTitle))
            .andExpect(jsonPath("$.isBookmarked").value(true))
            .andExpect(jsonPath("$.coffeeChatCount").value(3))

        verify(exactly = 1) { postService.getPageDetail(null, "post-1") }
    }

    @Test
    @DisplayName("공고 상세 조회 - 로그인 상태에서는 AuthUserOrNull이 사용자로 주입된다")
    fun getPageDetailLoggedInSuccess() {
        authenticateAs(applicantUser)
        every { postService.getPageDetail(applicantUser, "post-1") } returns samplePost

        mockMvc.perform(
            get("/api/post/post-1")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("post-1"))
            .andExpect(jsonPath("$.author.id").value(companyUser.id))

        verify(exactly = 1) { postService.getPageDetail(applicantUser, "post-1") }
    }

    @Test
    @DisplayName("공고 상세 조회 - 공고가 없으면 not found 에러를 반환한다")
    fun getPageDetailNotFoundFailure() {
        every { postService.getPageDetail(any(), "missing-post") } throws PostNotFoundException(details = mapOf("postId" to "missing-post"))

        mockMvc.perform(get("/api/post/missing-post"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_001"))
            .andExpect(jsonPath("$.details.postId").value("missing-post"))
    }

    @Test
    @DisplayName("공고 목록 조회 - 성공하면 PostWithPage를 반환한다")
    fun getPostsSuccess() {
        every {
            postService.getPosts(
                user = null,
                positions = listOf("FRONT", "BACKEND"),
                page = 0,
                order = 1,
                isActive = true,
                domains = listOf("B2B"),
            )
        } returns PageImpl(listOf(samplePost, anotherPost), PageRequest.of(0, 12), 2)

        mockMvc.perform(
            get("/api/post")
                .param("roles", "FRONT", "BACKEND")
                .param("domains", "B2B")
                .param("isActive", "true")
                .param("page", "0")
                .param("order", "1"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts[0].id").value("post-1"))
            .andExpect(jsonPath("$.posts[0].companyName").value(samplePost.company.companyName))
            .andExpect(jsonPath("$.posts[0].positionTitle").value(samplePost.position.positionTitle))
            .andExpect(jsonPath("$.posts[0].isBookmarked").value(true))
            .andExpect(jsonPath("$.paginator.lastPage").value(1))

        verify(exactly = 1) {
            postService.getPosts(
                user = null,
                positions = listOf("FRONT", "BACKEND"),
                page = 0,
                order = 1,
                isActive = true,
                domains = listOf("B2B"),
            )
        }
    }

    @Test
    @DisplayName("북마크 추가 - 성공하면 200을 반환한다")
    fun addBookmarkSuccess() {
        authenticateAs(applicantUser)
        every { postService.addBookmark(applicantUser.id, "post-1") } just Runs

        mockMvc.perform(
            post("/api/post/post-1/bookmark")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)

        verify(exactly = 1) { postService.addBookmark(applicantUser.id, "post-1") }
    }

    @Test
    @DisplayName("북마크 추가 - 공고가 없으면 not found 에러를 반환한다")
    fun addBookmarkPostNotFoundFailure() {
        every { postService.addBookmark(applicantUser.id, "missing-post") } throws PostNotFoundException(details = mapOf("postId" to "missing-post"))

        mockMvc.perform(
            post("/api/post/missing-post/bookmark")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_001"))
            .andExpect(jsonPath("$.details.postId").value("missing-post"))
    }

    @Test
    @DisplayName("북마크 삭제 - 성공하면 200을 반환한다")
    fun deleteBookmarkSuccess() {
        authenticateAs(applicantUser)
        every { postService.deleteBookmark(applicantUser.id, "post-1") } just Runs

        mockMvc.perform(
            delete("/api/post/post-1/bookmark")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)

        verify(exactly = 1) { postService.deleteBookmark(applicantUser.id, "post-1") }
    }

    @Test
    @DisplayName("북마크 삭제 - 북마크가 없으면 에러를 반환한다")
    fun deleteBookmarkNotFoundFailure() {
        every { postService.deleteBookmark(applicantUser.id, "post-1") } throws PostBookmarkNotFoundException(details = mapOf("userId" to applicantUser.id, "postId" to "post-1"))

        mockMvc.perform(
            delete("/api/post/post-1/bookmark")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_003"))
            .andExpect(jsonPath("$.details.userId").value(applicantUser.id))
            .andExpect(jsonPath("$.details.postId").value("post-1"))
    }

    @Test
    @DisplayName("북마크 목록 조회 - 성공하면 PostWithPage를 반환한다")
    fun getBookmarksSuccess() {
        authenticateAs(applicantUser)
        every { postService.getBookmarks(applicantUser.id, 0) } returns PageImpl(listOf(samplePost), PageRequest.of(0, 12), 1)

        mockMvc.perform(
            get("/api/post/bookmarks")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts[0].id").value("post-1"))
            .andExpect(jsonPath("$.posts[0].isBookmarked").value(true))
            .andExpect(jsonPath("$.paginator.lastPage").value(1))

        verify(exactly = 1) { postService.getBookmarks(applicantUser.id, 0) }
    }

    @Test
    @DisplayName("공고 생성 - 성공하면 Position을 반환한다")
    fun createPositionSuccess() {
        authenticateAs(companyUser)
        val request =
            CreatePositionRequest(
                positionTitle = "백엔드 개발자",
                positionType = Category.BACKEND,
                detail = "상세 내용",
                headCount = 3,
                salary = 5000,
                employmentEndDate = LocalDateTime.of(2026, 12, 31, 23, 59),
                companyId = "company-1",
            )
        val response = samplePosition(positionId = "position-created", positionTitle = request.positionTitle, positionType = request.positionType, detail = request.detail, headCount = request.headCount, salary = request.salary, employmentEndDate = request.employmentEndDate)
        every { postService.createPosition(companyUser, request) } returns response

        performJsonPositionPost(request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("position-created"))
            .andExpect(jsonPath("$.positionTitle").value("백엔드 개발자"))
            .andExpect(jsonPath("$.positionType").value("BACKEND"))
            .andExpect(jsonPath("$.headCount").value(3))

        verify(exactly = 1) { postService.createPosition(companyUser, request) }
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
              "headCount": -1,
              "companyId": ""
            }
            """.trimIndent()

        mockMvc.perform(
            withAuth(
                post("/api/post/position")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
            .andExpect(jsonPath("$.details.positionTitle").value("Position title is required."))
            .andExpect(jsonPath("$.details.detail").value("Position detail is required."))
            .andExpect(jsonPath("$.details.headCount").value("Headcount must be at least 0."))
            .andExpect(jsonPath("$.details.companyId").value("Company ID is required."))
    }

    @Test
    @DisplayName("공고 생성 - 잘못된 JSON은 파싱 오류를 반환한다")
    fun createPositionInvalidJsonFailure() {
        val invalidJson = "{"

        mockMvc.perform(
            withAuth(
                post("/api/post/position")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_005"))
            .andExpect(jsonPath("$.message").value("Malformed JSON request."))
    }

    @Test
    @DisplayName("공고 생성 - 회사 정보가 없으면 에러를 반환한다")
    fun createPositionCompanyNotFoundFailure() {
        authenticateAs(companyUser)
        val request =
            CreatePositionRequest(
                positionTitle = "백엔드 개발자",
                positionType = Category.BACKEND,
                detail = "상세 내용",
                headCount = 3,
                salary = 5000,
                employmentEndDate = LocalDateTime.of(2026, 12, 31, 23, 59),
                companyId = "missing-company",
            )
        every { postService.createPosition(companyUser, request) } throws PostCompanyNotFoundException(details = mapOf("companyId" to "missing-company"))

        performJsonPositionPost(request)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_008"))
            .andExpect(jsonPath("$.details.companyId").value("missing-company"))
    }

    @Test
    @DisplayName("공고 생성 - 권한이 없으면 에러를 반환한다")
    fun createPositionForbiddenFailure() {
        authenticateAs(applicantUser)
        val request =
            CreatePositionRequest(
                positionTitle = "백엔드 개발자",
                positionType = Category.BACKEND,
                detail = "상세 내용",
                headCount = 3,
                salary = 5000,
                employmentEndDate = LocalDateTime.of(2026, 12, 31, 23, 59),
                companyId = "company-1",
            )
        every { postService.createPosition(any(), any()) } throws NotAuthorizedException()

        performJsonPositionPost(request)
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("AUTH_007"))
    }

    @Test
    @DisplayName("회사 공고 목록 조회 - 성공하면 PostWithPage를 반환한다")
    fun getPostByCompanySuccess() {
        authenticateAs(companyUser)
        every {
            postService.getPostByCompany(
                user = companyUser,
                positions = listOf("BACKEND"),
                page = 0,
                order = 0,
                isActive = false,
                domains = listOf("B2B"),
            )
        } returns PageImpl(listOf(samplePost), PageRequest.of(0, 12), 1)

        mockMvc.perform(
            get("/api/post/position/me")
                .header("Authorization", "Bearer access-token")
                .param("roles", "BACKEND")
                .param("domains", "B2B"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts[0].id").value("post-1"))
            .andExpect(jsonPath("$.paginator.lastPage").value(1))

        verify(exactly = 1) {
            postService.getPostByCompany(
                user = companyUser,
                positions = listOf("BACKEND"),
                page = 0,
                order = 0,
                isActive = false,
                domains = listOf("B2B"),
            )
        }
    }

    @Test
    @DisplayName("회사 공고 목록 조회 - 지원자 계정이면 권한 없음 에러를 반환한다")
    fun getPostByCompanyForbiddenFailure() {
        authenticateAs(applicantUser)
        every { postService.getPostByCompany(any(), any(), any(), any(), any(), any()) } throws NotAuthorizedException()

        mockMvc.perform(
            get("/api/post/position/me")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("AUTH_007"))
    }

    @Test
    @DisplayName("공고 수정 - 성공하면 Position을 반환한다")
    fun updatePositionSuccess() {
        authenticateAs(companyUser)
        val request =
            CreatePositionRequest(
                positionTitle = "백엔드 시니어",
                positionType = Category.BACKEND,
                detail = "수정 상세",
                headCount = 5,
                salary = 7000,
                employmentEndDate = LocalDateTime.of(2026, 11, 30, 23, 59),
                companyId = "company-1",
            )
        val response = samplePosition(positionId = "position-1", positionTitle = request.positionTitle, positionType = request.positionType, detail = request.detail, headCount = request.headCount, salary = request.salary, employmentEndDate = request.employmentEndDate)
        every { postService.updatePosition(companyUser, "position-1", request) } returns response

        performJsonPut("/api/post/position/position-1", request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("position-1"))
            .andExpect(jsonPath("$.positionTitle").value("백엔드 시니어"))

        verify(exactly = 1) { postService.updatePosition(companyUser, "position-1", request) }
    }

    @Test
    @DisplayName("공고 수정 - 공고가 없으면 not found 에러를 반환한다")
    fun updatePositionNotFoundFailure() {
        authenticateAs(companyUser)
        val request =
            CreatePositionRequest(
                positionTitle = "백엔드 시니어",
                positionType = Category.BACKEND,
                detail = "수정 상세",
                headCount = 5,
                salary = 7000,
                employmentEndDate = LocalDateTime.of(2026, 11, 30, 23, 59),
                companyId = "company-1",
            )
        every { postService.updatePosition(any(), "missing-position", any()) } throws PostPositionNotFoundException(details = mapOf("positionId" to "missing-position"))

        performJsonPut("/api/post/position/missing-position", request)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_009"))
            .andExpect(jsonPath("$.details.positionId").value("missing-position"))
    }

    @Test
    @DisplayName("공고 즉시 마감 - 성공하면 200을 반환한다")
    fun closePositionSuccess() {
        authenticateAs(companyUser)
        every { postService.closePosition(companyUser, "position-1") } just Runs

        mockMvc.perform(
            patch("/api/post/position/position-1/close")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)

        verify(exactly = 1) { postService.closePosition(companyUser, "position-1") }
    }

    @Test
    @DisplayName("공고 즉시 마감 - 권한이 없으면 에러를 반환한다")
    fun closePositionForbiddenFailure() {
        authenticateAs(applicantUser)
        every { postService.closePosition(any(), any()) } throws NotAuthorizedException()

        mockMvc.perform(
            patch("/api/post/position/position-1/close")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("AUTH_007"))
    }

    @Test
    @DisplayName("공고 즉시 마감 - 공고가 없으면 not found 에러를 반환한다")
    fun closePositionNotFoundFailure() {
        authenticateAs(companyUser)
        every { postService.closePosition(any(), "missing-position") } throws PostPositionNotFoundException(details = mapOf("positionId" to "missing-position"))

        mockMvc.perform(
            patch("/api/post/position/missing-position/close")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_009"))
            .andExpect(jsonPath("$.details.positionId").value("missing-position"))
    }

    @Test
    @DisplayName("내 북마크 공고 조회 - 성공하면 PostWithPage를 반환한다")
    fun getBookmarksPageSuccess() {
        authenticateAs(applicantUser)
        every { postService.getBookmarks(applicantUser.id, 0) } returns PageImpl(listOf(samplePost), PageRequest.of(0, 12), 1)

        mockMvc.perform(
            get("/api/post/bookmarks")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts[0].id").value("post-1"))
            .andExpect(jsonPath("$.paginator.lastPage").value(1))

        verify(exactly = 1) { postService.getBookmarks(applicantUser.id, 0) }
    }

    @Test
    @DisplayName("내 북마크 공고 조회 - 사용자 정보가 없으면 not found 에러를 반환한다")
    fun getBookmarksUserNotFoundFailure() {
        authenticateAs(applicantUser)
        every { postService.getBookmarks(any(), any()) } throws UserNotFoundException(mapOf("userId" to applicantUser.id))

        mockMvc.perform(
            get("/api/post/bookmarks")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("USER_004"))
            .andExpect(jsonPath("$.details.userId").value(applicantUser.id))
    }

    private fun performJsonPositionPost(body: Any): ResultActions =
        mockMvc.perform(
            withAuth(
                post("/api/post/position")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ),
        )

    private fun performJsonPut(
        path: String,
        body: Any,
    ): ResultActions =
        mockMvc.perform(
            withAuth(
                put(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ),
        )

    private fun authenticateAs(user: User) {
        every { authService.authenticate(currentToken) } returns user
    }

    private fun withAuth(builder: MockHttpServletRequestBuilder): MockHttpServletRequestBuilder =
        builder.header("Authorization", "Bearer $currentToken")

    private fun sampleCompany(
        companyName: String = companyUser.name,
        companyId: String = "company-1",
    ): Company =
        Company(
            id = companyId,
            companyName = companyName,
            companyEstablishedYear = 2018,
            domain = Domain.B2B,
            headcount = 42,
            location = "서울",
            slogan = "성장하는 팀",
            detail = "회사 소개",
            profileImageKey = "company-image-key",
            companyInfoPDFKey = "company-pdf-key",
            landingPageLink = "https://company.example.com",
            links = listOf(Link(description = "Homepage", link = "https://company.example.com")),
            tags = listOf(Tag(tag = "스타트업")),
            vcName = "VC",
            vcRec = "추천",
            createdAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            updatedAt = LocalDateTime.of(2024, 1, 2, 0, 0),
        )

    private fun samplePosition(
        positionId: String = "position-1",
        positionTitle: String = "백엔드 개발자",
        positionType: Category = Category.BACKEND,
        detail: String = "공고 상세 설명",
        headCount: Int = 3,
        salary: Int? = 5000,
        employmentEndDate: LocalDateTime? = LocalDateTime.of(2026, 12, 31, 23, 59),
    ): Position =
        Position(
            id = positionId,
            positionTitle = positionTitle,
            positionType = positionType,
            headCount = headCount,
            salary = salary,
            detail = detail,
            employmentEndDate = employmentEndDate,
            createdAt = LocalDateTime.of(2024, 1, 3, 0, 0),
            updatedAt = LocalDateTime.of(2024, 1, 4, 0, 0),
        )
}
