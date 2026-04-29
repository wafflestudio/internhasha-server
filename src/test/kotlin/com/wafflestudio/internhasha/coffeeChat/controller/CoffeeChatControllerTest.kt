package com.wafflestudio.internhasha.coffeeChat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.applicant.repository.ApplicantRepository
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.auth.security.TokenProvider
import com.wafflestudio.internhasha.auth.service.dto.UserSimple
import com.wafflestudio.internhasha.coffeeChat.controller.dto.request.CoffeeChatContent
import com.wafflestudio.internhasha.coffeeChat.controller.dto.request.CoffeeChatStatusReq
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatStatus
import com.wafflestudio.internhasha.coffeeChat.exception.CoffeeChatDuplicationException
import com.wafflestudio.internhasha.coffeeChat.exception.CoffeeChatNotFoundException
import com.wafflestudio.internhasha.coffeeChat.exception.CoffeeChatPostExpiredException
import com.wafflestudio.internhasha.coffeeChat.exception.CoffeeChatStatusForbiddenException
import com.wafflestudio.internhasha.coffeeChat.exception.CoffeeChatUserForbiddenException
import com.wafflestudio.internhasha.coffeeChat.service.CoffeeChatService
import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatBrief
import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatCreateCommand
import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatDetailList
import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatDto
import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatUserInfo
import com.wafflestudio.internhasha.common.domain.LinkVo
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.repository.CompanyRepository
import com.wafflestudio.internhasha.post.domain.Category
import com.wafflestudio.internhasha.post.exception.PostNotFoundException
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.verify
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class CoffeeChatControllerTest {
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
    lateinit var coffeeChatService: CoffeeChatService

    lateinit var applicantId: String
    lateinit var companyId: String
    lateinit var applicantAccessToken: String
    lateinit var companyAccessToken: String
    lateinit var applicantPrincipal: AuthUserPrincipal
    lateinit var companyPrincipal: AuthUserPrincipal

    private lateinit var sampleApplicantDetail: CoffeeChatDto
    private lateinit var sampleCompanyDetail: CoffeeChatDto
    private lateinit var sampleBrief: CoffeeChatBrief

    @BeforeEach
    fun setUp() {
        clearMocks(coffeeChatService)

        val password = "password"
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

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
        applicantPrincipal =
            AuthUserPrincipal(
                id = applicantId,
                name = savedApplicant.name,
                userRole = UserRole.ROLE_APPLICANT,
                email = savedApplicant.email,
                passwordHash = savedApplicant.passwordHash,
            )
        applicantAccessToken =
            tokenProvider.generateTokens(UserSimple(id = applicantId, userRole = UserRole.ROLE_APPLICANT)).accessToken

        val companyEntity =
            Company(
                name = "기업회원",
                email = "company@example.com",
            ).apply {
                this.passwordHash = hashedPassword
            }

        val savedCompany = companyRepository.save(companyEntity)
        companyId = savedCompany.requireId()
        companyPrincipal =
            AuthUserPrincipal(
                id = companyId,
                name = savedCompany.name,
                userRole = UserRole.ROLE_COMPANY,
                email = savedCompany.email,
                passwordHash = savedCompany.passwordHash,
            )
        companyAccessToken =
            tokenProvider.generateTokens(UserSimple(id = companyId, userRole = UserRole.ROLE_COMPANY)).accessToken

        sampleApplicantDetail =
            CoffeeChatDto(
                id = "chat-1",
                positionId = "post-1",
                positionTitle = "백엔드 개발자",
                positionCategory = Category.BACKEND,
                company =
                    CoffeeChatUserInfo(
                        id = companyId,
                        name = savedCompany.name,
                        email = savedCompany.email,
                        profileImageKey = "company-image-key",
                    ),
                applicant =
                    CoffeeChatUserInfo(
                        id = applicantId,
                        name = savedApplicant.name,
                        email = savedApplicant.email,
                        profileImageKey = "applicant-image-key",
                    ),
                createdAt = LocalDateTime.of(2024, 1, 1, 0, 0),
                updatedAt = LocalDateTime.of(2024, 1, 2, 0, 0),
                coffeeChatStatus = CoffeeChatStatus.WAITING,
                changed = false,
                content = "안녕하세요. 잘 부탁드립니다.",
            )

        sampleCompanyDetail =
            sampleApplicantDetail.copy(
                coffeeChatStatus = CoffeeChatStatus.ACCEPTED,
                changed = true,
            )

        sampleBrief =
            CoffeeChatBrief(
                id = "chat-1",
                positionId = "post-1",
                positionType = "백엔드 개발",
                company = sampleApplicantDetail.company,
                createdAt = LocalDateTime.of(2024, 1, 1, 0, 0),
                updatedAt = LocalDateTime.of(2024, 1, 2, 0, 0),
                coffeeChatStatus = CoffeeChatStatus.WAITING,
                changed = false,
                applicant = sampleApplicantDetail.applicant,
            )
    }

    @Test
    @DisplayName("인증 헤더가 없으면 AUTH_001 에러를 반환한다")
    fun getCoffeeChatDetailWithoutAuthorizationHeaderFailure() {
        mockMvc
            .perform(get("/api/coffeeChat/chat-1"))
            .andExpect(status().isForbidden)
            .andExpect(content().string(""))
    }

    @Test
    @DisplayName("인증 헤더 형식이 잘못되면 AUTH_001 에러를 반환한다")
    fun getCoffeeChatDetailMalformedAuthorizationHeaderFailure() {
        mockMvc
            .perform(
                get("/api/coffeeChat/chat-1")
                    .header("Authorization", "InvalidToken"),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("AUTH_001"))
    }

    @Test
    @DisplayName("커피챗 상세 조회 - 지원자 계정이면 지원자용 응답을 반환한다")
    fun getCoffeeChatDetailApplicantSuccess() {
        every { coffeeChatService.getCoffeeChatDetail(applicantPrincipal, "chat-1") } returns sampleApplicantDetail

        mockMvc
            .perform(withAuth(get("/api/coffeeChat/chat-1"), applicantAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("chat-1"))
            .andExpect(jsonPath("$.positionId").value("post-1"))
            .andExpect(jsonPath("$.positionTitle").value("백엔드 개발자"))
            .andExpect(jsonPath("$.positionCategory").value("백엔드 개발"))
            .andExpect(jsonPath("$.coffeeChatStatus").value("WAITING"))
            .andExpect(jsonPath("$.changed").value(false))
            .andExpect(jsonPath("$.content").value("안녕하세요. 잘 부탁드립니다."))

        verify(exactly = 1) { coffeeChatService.getCoffeeChatDetail(applicantPrincipal, "chat-1") }
    }

    @Test
    @DisplayName("커피챗 상세 조회 - 회사 계정이면 회사용 응답을 반환한다")
    fun getCoffeeChatDetailCompanySuccess() {
        every { coffeeChatService.getCoffeeChatDetail(companyPrincipal, "chat-1") } returns sampleCompanyDetail

        mockMvc
            .perform(withAuth(get("/api/coffeeChat/chat-1"), companyAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("chat-1"))
            .andExpect(jsonPath("$.applicant.id").value(applicantId))
            .andExpect(jsonPath("$.applicant.email").value("applicant@snu.ac.kr"))
            .andExpect(jsonPath("$.coffeeChatStatus").value("ACCEPTED"))
            .andExpect(jsonPath("$.changed").value(true))

        verify(exactly = 1) { coffeeChatService.getCoffeeChatDetail(companyPrincipal, "chat-1") }
    }

    @Test
    @DisplayName("커피챗 상세 조회 - 대상이 없으면 not found 에러를 반환한다")
    fun getCoffeeChatDetailNotFoundFailure() {
        every {
            coffeeChatService.getCoffeeChatDetail(applicantPrincipal, "missing-chat")
        } throws CoffeeChatNotFoundException(details = mapOf("coffeeChatId" to "missing-chat"))

        mockMvc
            .perform(withAuth(get("/api/coffeeChat/missing-chat"), applicantAccessToken))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_001"))
            .andExpect(jsonPath("$.details.coffeeChatId").value("missing-chat"))
    }

    @Test
    @DisplayName("신청 여부 확인 - 성공하면 boolean 값을 반환한다")
    fun checkIsSubmittedSuccess() {
        every { coffeeChatService.checkIsSubmitted(applicantPrincipal, "post-1") } returns true

        mockMvc
            .perform(withAuth(get("/api/coffeeChat/post-1/status"), applicantAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.submitted").value(true))

        verify(exactly = 1) { coffeeChatService.checkIsSubmitted(applicantPrincipal, "post-1") }
    }

    @Test
    @DisplayName("신청 여부 확인 - 회사 계정이면 권한 없음 에러를 반환한다")
    fun checkIsSubmittedForbiddenFailure() {
        mockMvc
            .perform(withAuth(get("/api/coffeeChat/post-1/status"), companyAccessToken))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("403"))
    }

    @Test
    @DisplayName("커피챗 신청 - 성공하면 지원자 응답을 반환한다")
    fun postCoffeeChatSuccess() {
        val request = CoffeeChatContent(content = "커피챗 신청합니다.")
        val command = CoffeeChatCreateCommand(applicantId = applicantId, positionId = "post-1", content = request.content)
        every { coffeeChatService.applyCoffeeChat(command) } returns sampleApplicantDetail

        performCoffeeChatPost(request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("chat-1"))
            .andExpect(jsonPath("$.content").value("안녕하세요. 잘 부탁드립니다."))

        verify(exactly = 1) { coffeeChatService.applyCoffeeChat(command) }
    }

    @Test
    @DisplayName("커피챗 신청 - 잘못된 JSON은 파싱 오류를 반환한다")
    fun postCoffeeChatInvalidJsonFailure() {
        val invalidJson = "{"

        mockMvc
            .perform(
                withAuth(
                    post("/api/coffeeChat/post-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson),
                    applicantAccessToken,
                ),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_005"))
            .andExpect(jsonPath("$.message").value("Malformed JSON request."))
    }

    @Test
    @DisplayName("커피챗 신청 - 회사 계정이면 권한 없음 에러를 반환한다")
    fun postCoffeeChatForbiddenFailure() {
        val request = CoffeeChatContent(content = "커피챗 신청합니다.")

        mockMvc
            .perform(
                withAuth(
                    post("/api/coffeeChat/post-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                    companyAccessToken,
                ),
            ).andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("403"))
    }

    @Test
    @DisplayName("커피챗 신청 - 중복 신청이면 에러를 반환한다")
    fun postCoffeeChatDuplicateFailure() {
        val request = CoffeeChatContent(content = "커피챗 신청합니다.")
        val command = CoffeeChatCreateCommand(applicantId = applicantId, positionId = "post-1", content = request.content)
        every {
            coffeeChatService.applyCoffeeChat(command)
        } throws CoffeeChatDuplicationException(details = mapOf("coffeeChatId" to "chat-1"))

        performCoffeeChatPost(request)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_009"))
            .andExpect(jsonPath("$.details.coffeeChatId").value("chat-1"))
    }

    @Test
    @DisplayName("커피챗 신청 - 공고가 마감되었으면 에러를 반환한다")
    fun postCoffeeChatExpiredFailure() {
        val request = CoffeeChatContent(content = "커피챗 신청합니다.")
        val command = CoffeeChatCreateCommand(applicantId = applicantId, positionId = "post-1", content = request.content)
        every {
            coffeeChatService.applyCoffeeChat(command)
        } throws CoffeeChatPostExpiredException(details = mapOf("postId" to "post-1"))

        performCoffeeChatPost(request)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_007"))
            .andExpect(jsonPath("$.details.postId").value("post-1"))
    }

    @Test
    @DisplayName("커피챗 수정 - 성공하면 지원자 응답을 반환한다")
    fun editCoffeeChatSuccess() {
        val request = CoffeeChatContent(content = "수정된 내용")
        every { coffeeChatService.editCoffeeChat(applicantPrincipal, "chat-1", request) } returns sampleApplicantDetail

        performCoffeeChatPut(request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("chat-1"))

        verify(exactly = 1) { coffeeChatService.editCoffeeChat(applicantPrincipal, "chat-1", request) }
    }

    @Test
    @DisplayName("커피챗 수정 - 상태가 WAITING이 아니면 에러를 반환한다")
    fun editCoffeeChatStatusForbiddenFailure() {
        val request = CoffeeChatContent(content = "수정된 내용")
        every {
            coffeeChatService.editCoffeeChat(applicantPrincipal, "chat-1", request)
        } throws
            CoffeeChatStatusForbiddenException(
                details = mapOf("coffeeChatId" to "chat-1", "status" to "ACCEPTED"),
            )

        performCoffeeChatPut(request)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_008"))
            .andExpect(jsonPath("$.details.coffeeChatId").value("chat-1"))
    }

    @Test
    @DisplayName("상태 변경 - 지원자는 취소만 가능하다")
    fun changeCoffeeChatStatusApplicantSuccess() {
        val request = CoffeeChatStatusReq(coffeeChatStatus = CoffeeChatStatus.CANCELED, coffeeChatList = listOf("chat-1"))
        val response =
            CoffeeChatDetailList(
                succeeded = listOf(sampleApplicantDetail.copy(coffeeChatStatus = CoffeeChatStatus.CANCELED)),
                failed = emptyList(),
            )
        every { coffeeChatService.changeCoffeeChatStatus(applicantPrincipal, request) } returns response

        performCoffeeChatPatch(request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.succeeded[0].id").value("chat-1"))
            .andExpect(jsonPath("$.succeeded[0].coffeeChatStatus").value("CANCELED"))
            .andExpect(jsonPath("$.failed").isEmpty)

        verify(exactly = 1) { coffeeChatService.changeCoffeeChatStatus(applicantPrincipal, request) }
    }

    @Test
    @DisplayName("상태 변경 - 회사는 수락/거절만 가능하다")
    fun changeCoffeeChatStatusCompanySuccess() {
        val request = CoffeeChatStatusReq(coffeeChatStatus = CoffeeChatStatus.ACCEPTED, coffeeChatList = listOf("chat-1"))
        val response = CoffeeChatDetailList(succeeded = listOf(sampleCompanyDetail), failed = emptyList())
        every { coffeeChatService.changeCoffeeChatStatus(companyPrincipal, request) } returns response

        performCoffeeChatPatch(request, companyAccessToken)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.succeeded[0].id").value("chat-1"))
            .andExpect(jsonPath("$.succeeded[0].coffeeChatStatus").value("ACCEPTED"))
            .andExpect(jsonPath("$.failed").isEmpty)

        verify(exactly = 1) { coffeeChatService.changeCoffeeChatStatus(companyPrincipal, request) }
    }

    @Test
    @DisplayName("상태 변경 - validation 실패 시 validation error를 반환한다")
    fun changeCoffeeChatStatusValidationFailure() {
        val invalidJson =
            """
            {
              "coffeeChatStatus": "ACCEPTED",
              "coffeeChatList": []
            }
            """.trimIndent()

        mockMvc
            .perform(
                withAuth(
                    patch("/api/coffeeChat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson),
                    applicantAccessToken,
                ),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
            .andExpect(jsonPath("$.details.coffeeChatList").value("List cannot not empty"))
    }

    @Test
    @DisplayName("상태 변경 - 지원자에게 허용되지 않은 상태면 권한 없음 에러를 반환한다")
    fun changeCoffeeChatStatusApplicantForbiddenFailure() {
        val request =
            CoffeeChatStatusReq(coffeeChatStatus = CoffeeChatStatus.ACCEPTED, coffeeChatList = listOf("chat-1"))
        every {
            coffeeChatService.changeCoffeeChatStatus(applicantPrincipal, request)
        } throws
            CoffeeChatUserForbiddenException(
                details = mapOf("userRole" to applicantPrincipal.userRole, "coffeeChatStatus" to CoffeeChatStatus.ACCEPTED),
            )

        performCoffeeChatPatch(request)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_002"))
            .andExpect(jsonPath("$.details.userRole").value("ROLE_APPLICANT"))
    }

    @Test
    @DisplayName("커피챗 목록 조회 - 지원자 계정이면 지원자용 목록을 반환한다")
    fun getCoffeeChatListApplicantSuccess() {
        every { coffeeChatService.getCoffeeChatList(applicantPrincipal) } returns listOf(sampleBrief)

        mockMvc
            .perform(withAuth(get("/api/coffeeChat"), applicantAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.coffeeChatList[0].id").value("chat-1"))
            .andExpect(jsonPath("$.coffeeChatList[0].positionId").value("post-1"))
            .andExpect(jsonPath("$.coffeeChatList[0].changed").value(false))

        verify(exactly = 1) { coffeeChatService.getCoffeeChatList(applicantPrincipal) }
    }

    @Test
    @DisplayName("커피챗 목록 조회 - 회사 계정이면 회사용 목록을 반환한다")
    fun getCoffeeChatListCompanySuccess() {
        every { coffeeChatService.getCoffeeChatList(companyPrincipal) } returns listOf(sampleBrief)

        mockMvc
            .perform(withAuth(get("/api/coffeeChat"), companyAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.coffeeChatList[0].id").value("chat-1"))
            .andExpect(jsonPath("$.coffeeChatList[0].company.name").value("기업회원"))

        verify(exactly = 1) { coffeeChatService.getCoffeeChatList(companyPrincipal) }
    }

    @Test
    @DisplayName("커피챗 목록 조회 - 권한이 없으면 에러를 반환한다")
    fun getCoffeeChatListForbiddenFailure() {
        every {
            coffeeChatService.getCoffeeChatList(companyPrincipal)
        } throws
            CoffeeChatUserForbiddenException(
                details = mapOf("userId" to companyPrincipal.id, "userRole" to companyPrincipal.userRole),
            )

        mockMvc
            .perform(withAuth(get("/api/coffeeChat"), companyAccessToken))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_002"))
            .andExpect(jsonPath("$.details.userId").value(companyPrincipal.id))
    }

    @Test
    @DisplayName("커피챗 배지 개수 조회 - 지원자 계정이면 개수를 반환한다")
    fun countCoffeeChatBadgesApplicantSuccess() {
        every { coffeeChatService.countCoffeeChatBadges(applicantPrincipal) } returns 2

        mockMvc
            .perform(withAuth(get("/api/coffeeChat/count"), applicantAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.num").value(2))

        verify(exactly = 1) { coffeeChatService.countCoffeeChatBadges(applicantPrincipal) }
    }

    @Test
    @DisplayName("커피챗 배지 개수 조회 - 회사 계정이면 개수를 반환한다")
    fun countCoffeeChatBadgesCompanySuccess() {
        every { coffeeChatService.countCoffeeChatBadges(companyPrincipal) } returns 5

        mockMvc
            .perform(withAuth(get("/api/coffeeChat/count"), companyAccessToken))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.num").value(5))

        verify(exactly = 1) { coffeeChatService.countCoffeeChatBadges(companyPrincipal) }
    }

    @Test
    @DisplayName("커피챗 신청 - 대상 공고가 없으면 not found 에러를 반환한다")
    fun postCoffeeChatPostNotFoundFailure() {
        val request = CoffeeChatContent(content = "커피챗 신청합니다.")
        val command = CoffeeChatCreateCommand(applicantId = applicantId, positionId = "post-1", content = request.content)
        every {
            coffeeChatService.applyCoffeeChat(command)
        } throws PostNotFoundException(details = mapOf("postId" to "post-1"))

        performCoffeeChatPost(request)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_001"))
            .andExpect(jsonPath("$.details.postId").value("post-1"))
    }

    @Test
    @DisplayName("커피챗 신청 - 지원자 프로필이 없으면 권한 없음 에러를 반환한다")
    fun postCoffeeChatNoProfileForbiddenFailure() {
        val request = CoffeeChatContent(content = "커피챗 신청합니다.")
        val command = CoffeeChatCreateCommand(applicantId = applicantId, positionId = "post-1", content = request.content)
        every {
            coffeeChatService.applyCoffeeChat(command)
        } throws
            CoffeeChatUserForbiddenException(
                details = mapOf("userId" to applicantPrincipal.id, "user.applicant" to "No Profile"),
            )

        performCoffeeChatPost(request)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_002"))
            .andExpect(jsonPath("$.details['user.applicant']").value("No Profile"))
    }

    @Test
    @DisplayName("커피챗 상세 조회 - 회사 계정이 아니면 권한 없음 에러를 반환한다")
    fun getCoffeeChatDetailForbiddenFailure() {
        every {
            coffeeChatService.getCoffeeChatDetail(companyPrincipal, "chat-1")
        } throws
            CoffeeChatUserForbiddenException(
                details = mapOf("userId" to companyPrincipal.id, "userRole" to companyPrincipal.userRole),
            )

        mockMvc
            .perform(withAuth(get("/api/coffeeChat/chat-1"), companyAccessToken))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_002"))
            .andExpect(jsonPath("$.details.userId").value(companyPrincipal.id))
    }

    private fun withAuth(
        builder: MockHttpServletRequestBuilder,
        token: String,
    ): MockHttpServletRequestBuilder = builder.header("Authorization", "Bearer $token")

    private fun performCoffeeChatPost(body: Any): ResultActions =
        mockMvc.perform(
            withAuth(
                post("/api/coffeeChat/post-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
                applicantAccessToken,
            ),
        )

    private fun performCoffeeChatPut(body: Any): ResultActions =
        mockMvc.perform(
            withAuth(
                put("/api/coffeeChat/chat-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
                applicantAccessToken,
            ),
        )

    private fun performCoffeeChatPatch(
        body: Any,
        token: String = applicantAccessToken,
    ): ResultActions =
        mockMvc.perform(
            withAuth(
                patch("/api/coffeeChat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
                token,
            ),
        )
}
