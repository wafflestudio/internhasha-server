package com.wafflestudio.internhasha.coffeeChat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.internhasha.applicant.dto.ApplicantResponse
import com.wafflestudio.internhasha.auth.UserRole
import com.wafflestudio.internhasha.auth.dto.User
import com.wafflestudio.internhasha.auth.service.AuthService
import com.wafflestudio.internhasha.coffeeChat.CoffeeChatDuplicationException
import com.wafflestudio.internhasha.coffeeChat.CoffeeChatNotFoundException
import com.wafflestudio.internhasha.coffeeChat.CoffeeChatPostExpiredException
import com.wafflestudio.internhasha.coffeeChat.CoffeeChatStatus
import com.wafflestudio.internhasha.coffeeChat.CoffeeChatStatusForbiddenException
import com.wafflestudio.internhasha.coffeeChat.CoffeeChatUserForbiddenException
import com.wafflestudio.internhasha.coffeeChat.dto.CoffeeChatApplicant
import com.wafflestudio.internhasha.coffeeChat.dto.CoffeeChatBrief
import com.wafflestudio.internhasha.coffeeChat.dto.CoffeeChatCompany
import com.wafflestudio.internhasha.coffeeChat.dto.CoffeeChatUserInfo
import com.wafflestudio.internhasha.coffeeChat.service.CoffeeChatService
import com.wafflestudio.internhasha.post.PostNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class CoffeeChatControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var coffeeChatService: CoffeeChatService

    @MockitoBean
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

    private val sampleApplicantDetail =
        CoffeeChatApplicant(
            id = "chat-1",
            postId = "post-1",
            title = "백엔드 개발자",
            company = CoffeeChatUserInfo(name = companyUser.name, imageKey = "company-image-key"),
            createdAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            updatedAt = LocalDateTime.of(2024, 1, 2, 0, 0),
            coffeeChatStatus = CoffeeChatStatus.WAITING,
            changed = false,
            content = "안녕하세요. 잘 부탁드립니다.",
        )
    private val sampleCompanyDetail =
        CoffeeChatCompany(
            id = "chat-1",
            postId = "post-1",
            title = "백엔드 개발자",
            company = CoffeeChatUserInfo(name = companyUser.name, imageKey = "company-image-key"),
            createdAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            updatedAt = LocalDateTime.of(2024, 1, 2, 0, 0),
            coffeeChatStatus = CoffeeChatStatus.ACCEPTED,
            changed = true,
            content = "안녕하세요. 잘 부탁드립니다.",
            applicant =
                ApplicantResponse(
                    id = applicantUser.id,
                    name = applicantUser.name,
                    createdAt = LocalDateTime.of(2024, 1, 3, 0, 0),
                    updatedAt = LocalDateTime.of(2024, 1, 4, 0, 0),
                    userRole = UserRole.ROLE_APPLICANT,
                    email = applicantUser.email,
                    cvKey = "cv-key",
                ),
        )
    private val sampleBrief =
        CoffeeChatBrief(
            id = "chat-1",
            postId = "post-1",
            positionType = "백엔드 개발",
            company = CoffeeChatUserInfo(name = companyUser.name, imageKey = "company-image-key"),
            createdAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            updatedAt = LocalDateTime.of(2024, 1, 2, 0, 0),
            coffeeChatStatus = CoffeeChatStatus.WAITING,
            changed = false,
            applicant = CoffeeChatUserInfo(name = applicantUser.name, imageKey = "applicant-image-key"),
        )

    @BeforeEach
    fun setUp() {
        reset(coffeeChatService, authService)
        authenticateAs(applicantUser)
    }

    @Test
    @DisplayName("인증 헤더가 없으면 AUTH_001 에러를 반환한다")
    fun getCoffeeChatDetailWithoutAuthorizationHeaderFailure() {
        mockMvc.perform(get("/api/coffeeChat/chat-1"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("AUTH_001"))
    }

    @Test
    @DisplayName("인증 헤더 형식이 잘못되면 AUTH_001 에러를 반환한다")
    fun getCoffeeChatDetailMalformedAuthorizationHeaderFailure() {
        mockMvc.perform(
            get("/api/coffeeChat/chat-1")
                .header("Authorization", "InvalidToken"),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("AUTH_001"))
    }

    @Test
    @DisplayName("커피챗 상세 조회 - 지원자 계정이면 지원자용 응답을 반환한다")
    fun getCoffeeChatDetailApplicantSuccess() {
        authenticateAs(applicantUser)
        given(coffeeChatService.getCoffeeChatDetail(applicantUser, "chat-1")).willReturn(sampleApplicantDetail)

        mockMvc.perform(withAuth(get("/api/coffeeChat/chat-1")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("chat-1"))
            .andExpect(jsonPath("$.postId").value("post-1"))
            .andExpect(jsonPath("$.coffeeChatStatus").value("WAITING"))
            .andExpect(jsonPath("$.content").value("안녕하세요. 잘 부탁드립니다."))

        then(coffeeChatService).should(times(1)).getCoffeeChatDetail(applicantUser, "chat-1")
    }

    @Test
    @DisplayName("커피챗 상세 조회 - 회사 계정이면 회사용 응답을 반환한다")
    fun getCoffeeChatDetailCompanySuccess() {
        authenticateAs(companyUser)
        given(coffeeChatService.getCoffeeChatDetail(companyUser, "chat-1")).willReturn(sampleCompanyDetail)

        mockMvc.perform(withAuth(get("/api/coffeeChat/chat-1")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("chat-1"))
            .andExpect(jsonPath("$.applicant.id").value(applicantUser.id))
            .andExpect(jsonPath("$.applicant.email").value(applicantUser.email))
            .andExpect(jsonPath("$.coffeeChatStatus").value("ACCEPTED"))

        then(coffeeChatService).should(times(1)).getCoffeeChatDetail(companyUser, "chat-1")
    }

    @Test
    @DisplayName("커피챗 상세 조회 - 대상이 없으면 not found 에러를 반환한다")
    fun getCoffeeChatDetailNotFoundFailure() {
        given(coffeeChatService.getCoffeeChatDetail(applicantUser, "missing-chat"))
            .willThrow(CoffeeChatNotFoundException(details = mapOf("coffeeChatId" to "missing-chat")))

        mockMvc.perform(withAuth(get("/api/coffeeChat/missing-chat")))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_001"))
            .andExpect(jsonPath("$.details.coffeeChatId").value("missing-chat"))
    }

    @Test
    @DisplayName("신청 여부 확인 - 성공하면 boolean 값을 반환한다")
    fun checkIsSubmittedSuccess() {
        authenticateAs(applicantUser)
        given(coffeeChatService.checkIsSubmitted(applicantUser, "post-1")).willReturn(true)

        mockMvc.perform(withAuth(get("/api/coffeeChat/post-1/status")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isSubmitted").value(true))

        then(coffeeChatService).should(times(1)).checkIsSubmitted(applicantUser, "post-1")
    }

    @Test
    @DisplayName("신청 여부 확인 - 회사 계정이면 권한 없음 에러를 반환한다")
    fun checkIsSubmittedForbiddenFailure() {
        authenticateAs(companyUser)
        given(coffeeChatService.checkIsSubmitted(companyUser, "post-1"))
            .willThrow(
                CoffeeChatUserForbiddenException(
                    details = mapOf("userId" to companyUser.id, "userRole" to companyUser.userRole),
                ),
            )

        mockMvc.perform(withAuth(get("/api/coffeeChat/post-1/status")))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_002"))
            .andExpect(jsonPath("$.details.userId").value(companyUser.id))
    }

    @Test
    @DisplayName("커피챗 신청 - 성공하면 지원자 응답을 반환한다")
    fun postCoffeeChatSuccess() {
        authenticateAs(applicantUser)
        val request = CoffeeChatContent(content = "커피챗 신청합니다.")
        given(coffeeChatService.applyCoffeeChat(applicantUser, "post-1", request)).willReturn(sampleApplicantDetail)

        performCoffeeChatPost(request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("chat-1"))
            .andExpect(jsonPath("$.content").value("안녕하세요. 잘 부탁드립니다."))

        then(coffeeChatService).should(times(1)).applyCoffeeChat(applicantUser, "post-1", request)
    }

    @Test
    @DisplayName("커피챗 신청 - 잘못된 JSON은 파싱 오류를 반환한다")
    fun postCoffeeChatInvalidJsonFailure() {
        val invalidJson = "{"

        mockMvc.perform(
            withAuth(
                post("/api/coffeeChat/post-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_005"))
            .andExpect(jsonPath("$.message").value("Malformed JSON request."))
    }

    @Test
    @DisplayName("커피챗 신청 - 회사 계정이면 권한 없음 에러를 반환한다")
    fun postCoffeeChatForbiddenFailure() {
        authenticateAs(companyUser)
        val request = CoffeeChatContent(content = "커피챗 신청합니다.")
        given(coffeeChatService.applyCoffeeChat(companyUser, "post-1", request))
            .willThrow(
                CoffeeChatUserForbiddenException(
                    details = mapOf("userId" to companyUser.id, "userRole" to companyUser.userRole),
                ),
            )

        performCoffeeChatPost(request)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_002"))
            .andExpect(jsonPath("$.details.userId").value(companyUser.id))
    }

    @Test
    @DisplayName("커피챗 신청 - 중복 신청이면 에러를 반환한다")
    fun postCoffeeChatDuplicateFailure() {
        authenticateAs(applicantUser)
        val request = CoffeeChatContent(content = "커피챗 신청합니다.")
        given(coffeeChatService.applyCoffeeChat(applicantUser, "post-1", request))
            .willThrow(CoffeeChatDuplicationException(details = mapOf("coffeeChatId" to "chat-1")))

        performCoffeeChatPost(request)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_009"))
            .andExpect(jsonPath("$.details.coffeeChatId").value("chat-1"))
    }

    @Test
    @DisplayName("커피챗 신청 - 공고가 마감되었으면 에러를 반환한다")
    fun postCoffeeChatExpiredFailure() {
        authenticateAs(applicantUser)
        val request = CoffeeChatContent(content = "커피챗 신청합니다.")
        given(coffeeChatService.applyCoffeeChat(applicantUser, "post-1", request))
            .willThrow(CoffeeChatPostExpiredException(details = mapOf("postId" to "post-1")))

        performCoffeeChatPost(request)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_007"))
            .andExpect(jsonPath("$.details.postId").value("post-1"))
    }

    @Test
    @DisplayName("커피챗 수정 - 성공하면 지원자 응답을 반환한다")
    fun editCoffeeChatSuccess() {
        authenticateAs(applicantUser)
        val request = CoffeeChatContent(content = "수정된 내용")
        given(coffeeChatService.editCoffeeChat(applicantUser, "chat-1", request)).willReturn(sampleApplicantDetail)

        performCoffeeChatPut(request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("chat-1"))

        then(coffeeChatService).should(times(1)).editCoffeeChat(applicantUser, "chat-1", request)
    }

    @Test
    @DisplayName("커피챗 수정 - 상태가 WAITING이 아니면 에러를 반환한다")
    fun editCoffeeChatStatusForbiddenFailure() {
        authenticateAs(applicantUser)
        val request = CoffeeChatContent(content = "수정된 내용")
        given(coffeeChatService.editCoffeeChat(applicantUser, "chat-1", request))
            .willThrow(
                CoffeeChatStatusForbiddenException(
                    details = mapOf("coffeeChatId" to "chat-1", "status" to "ACCEPTED"),
                ),
            )

        performCoffeeChatPut(request)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_008"))
            .andExpect(jsonPath("$.details.coffeeChatId").value("chat-1"))
    }

    @Test
    @DisplayName("상태 변경 - 지원자는 취소만 가능하다")
    fun changeCoffeeChatStatusApplicantSuccess() {
        authenticateAs(applicantUser)
        val request = CoffeeChatStatusReq(coffeeChatStatus = CoffeeChatStatus.CANCELED, coffeeChatList = listOf("chat-1"))
        val response = CoffeeChatDetailList(succeeded = listOf(sampleApplicantDetail), failed = emptyList())
        given(coffeeChatService.changeCoffeeChatStatus(applicantUser, request)).willReturn(response)

        performCoffeeChatPatch(request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.succeeded[0].id").value("chat-1"))
            .andExpect(jsonPath("$.failed").isEmpty)

        then(coffeeChatService).should(times(1)).changeCoffeeChatStatus(applicantUser, request)
    }

    @Test
    @DisplayName("상태 변경 - 회사는 수락/거절만 가능하다")
    fun changeCoffeeChatStatusCompanySuccess() {
        authenticateAs(companyUser)
        val request = CoffeeChatStatusReq(coffeeChatStatus = CoffeeChatStatus.ACCEPTED, coffeeChatList = listOf("chat-1"))
        val response = CoffeeChatDetailList(succeeded = listOf(sampleCompanyDetail), failed = emptyList())
        given(coffeeChatService.changeCoffeeChatStatus(companyUser, request)).willReturn(response)

        performCoffeeChatPatch(request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.succeeded[0].id").value("chat-1"))
            .andExpect(jsonPath("$.failed").isEmpty)

        then(coffeeChatService).should(times(1)).changeCoffeeChatStatus(companyUser, request)
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

        mockMvc.perform(
            withAuth(
                patch("/api/coffeeChat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("GEN_004"))
            .andExpect(jsonPath("$.details.coffeeChatList").value("List cannot not empty"))
    }

    @Test
    @DisplayName("상태 변경 - 지원자에게 허용되지 않은 상태면 권한 없음 에러를 반환한다")
    fun changeCoffeeChatStatusApplicantForbiddenFailure() {
        authenticateAs(applicantUser)
        val request = CoffeeChatStatusReq(coffeeChatStatus = CoffeeChatStatus.ACCEPTED, coffeeChatList = listOf("chat-1"))
        given(coffeeChatService.changeCoffeeChatStatus(applicantUser, request))
            .willThrow(
                CoffeeChatUserForbiddenException(
                    details = mapOf("userRole" to applicantUser.userRole, "coffeeChatStatus" to CoffeeChatStatus.ACCEPTED),
                ),
            )

        performCoffeeChatPatch(request)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_002"))
            .andExpect(jsonPath("$.details.userRole").value("APPLICANT"))
    }

    @Test
    @DisplayName("커피챗 목록 조회 - 지원자 계정이면 지원자용 목록을 반환한다")
    fun getCoffeeChatListApplicantSuccess() {
        authenticateAs(applicantUser)
        given(coffeeChatService.getCoffeeChatListApplicant(applicantUser)).willReturn(listOf(sampleBrief))

        mockMvc.perform(withAuth(get("/api/coffeeChat")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.coffeeChatList[0].id").value("chat-1"))
            .andExpect(jsonPath("$.coffeeChatList[0].changed").value(false))

        then(coffeeChatService).should(times(1)).getCoffeeChatListApplicant(applicantUser)
    }

    @Test
    @DisplayName("커피챗 목록 조회 - 회사 계정이면 회사용 목록을 반환한다")
    fun getCoffeeChatListCompanySuccess() {
        authenticateAs(companyUser)
        given(coffeeChatService.getCoffeeChatListCompany(companyUser)).willReturn(listOf(sampleBrief))

        mockMvc.perform(withAuth(get("/api/coffeeChat")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.coffeeChatList[0].id").value("chat-1"))
            .andExpect(jsonPath("$.coffeeChatList[0].company.name").value(companyUser.name))

        then(coffeeChatService).should(times(1)).getCoffeeChatListCompany(companyUser)
    }

    @Test
    @DisplayName("커피챗 목록 조회 - 권한이 없으면 에러를 반환한다")
    fun getCoffeeChatListForbiddenFailure() {
        authenticateAs(companyUser)
        given(coffeeChatService.getCoffeeChatListCompany(companyUser))
            .willThrow(
                CoffeeChatUserForbiddenException(
                    details = mapOf("userId" to companyUser.id, "userRole" to companyUser.userRole),
                ),
            )

        mockMvc.perform(withAuth(get("/api/coffeeChat")))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_002"))
            .andExpect(jsonPath("$.details.userId").value(companyUser.id))
    }

    @Test
    @DisplayName("커피챗 배지 개수 조회 - 지원자 계정이면 개수를 반환한다")
    fun countCoffeeChatBadgesApplicantSuccess() {
        authenticateAs(applicantUser)
        given(coffeeChatService.countCoffeeChatBadges(applicantUser)).willReturn(2)

        mockMvc.perform(withAuth(get("/api/coffeeChat/count")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.num").value(2))

        then(coffeeChatService).should(times(1)).countCoffeeChatBadges(applicantUser)
    }

    @Test
    @DisplayName("커피챗 배지 개수 조회 - 회사 계정이면 개수를 반환한다")
    fun countCoffeeChatBadgesCompanySuccess() {
        authenticateAs(companyUser)
        given(coffeeChatService.countCoffeeChatBadges(companyUser)).willReturn(5)

        mockMvc.perform(withAuth(get("/api/coffeeChat/count")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.num").value(5))

        then(coffeeChatService).should(times(1)).countCoffeeChatBadges(companyUser)
    }

    @Test
    @DisplayName("커피챗 신청 - 대상 공고가 없으면 not found 에러를 반환한다")
    fun postCoffeeChatPostNotFoundFailure() {
        authenticateAs(applicantUser)
        val request = CoffeeChatContent(content = "커피챗 신청합니다.")
        given(coffeeChatService.applyCoffeeChat(applicantUser, "post-1", request))
            .willThrow(PostNotFoundException(details = mapOf("postId" to "post-1")))

        performCoffeeChatPost(request)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("POST_001"))
            .andExpect(jsonPath("$.details.postId").value("post-1"))
    }

    @Test
    @DisplayName("커피챗 신청 - 지원자 프로필이 없으면 권한 없음 에러를 반환한다")
    fun postCoffeeChatNoProfileForbiddenFailure() {
        authenticateAs(applicantUser)
        val request = CoffeeChatContent(content = "커피챗 신청합니다.")
        given(coffeeChatService.applyCoffeeChat(applicantUser, "post-1", request))
            .willThrow(
                CoffeeChatUserForbiddenException(
                    details = mapOf("userId" to applicantUser.id, "user.applicant" to "No Profile"),
                ),
            )

        performCoffeeChatPost(request)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_002"))
            .andExpect(jsonPath("$.details['user.applicant']").value("No Profile"))
    }

    @Test
    @DisplayName("커피챗 상세 조회 - 회사 계정이 아니면 권한 없음 에러를 반환한다")
    fun getCoffeeChatDetailForbiddenFailure() {
        authenticateAs(companyUser)
        given(coffeeChatService.getCoffeeChatDetail(companyUser, "chat-1"))
            .willThrow(
                CoffeeChatUserForbiddenException(
                    details = mapOf("userId" to companyUser.id, "userRole" to companyUser.userRole),
                ),
            )

        mockMvc.perform(withAuth(get("/api/coffeeChat/chat-1")))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("COFFEECHAT_002"))
            .andExpect(jsonPath("$.details.userId").value(companyUser.id))
    }

    private fun authenticateAs(user: User) {
        currentToken = "${user.userRole.name.lowercase()}-${user.id}"
        given(authService.authenticate(currentToken)).willReturn(user)
    }

    private fun withAuth(builder: MockHttpServletRequestBuilder): MockHttpServletRequestBuilder =
        builder.header("Authorization", "Bearer $currentToken")

    private fun performCoffeeChatPost(body: Any): ResultActions =
        mockMvc.perform(
            withAuth(
                post("/api/coffeeChat/post-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ),
        )

    private fun performCoffeeChatPut(body: Any): ResultActions =
        mockMvc.perform(
            withAuth(
                put("/api/coffeeChat/chat-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ),
        )

    private fun performCoffeeChatPatch(body: Any): ResultActions =
        mockMvc.perform(
            withAuth(
                patch("/api/coffeeChat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ),
        )
}
