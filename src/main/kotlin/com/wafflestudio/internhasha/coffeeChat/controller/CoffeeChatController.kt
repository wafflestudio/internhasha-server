package com.wafflestudio.internhasha.coffeeChat.controller

import com.wafflestudio.internhasha.auth.domain.RoleValue
import com.wafflestudio.internhasha.auth.security.AuthUser
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.coffeeChat.controller.dto.request.CoffeeChatContent
import com.wafflestudio.internhasha.coffeeChat.controller.dto.request.CoffeeChatStatusReq
import com.wafflestudio.internhasha.coffeeChat.controller.dto.response.CoffeeChatCount
import com.wafflestudio.internhasha.coffeeChat.controller.dto.response.CoffeeChatDetailListResponse
import com.wafflestudio.internhasha.coffeeChat.controller.dto.response.CoffeeChatIsSubmitted
import com.wafflestudio.internhasha.coffeeChat.controller.dto.response.CoffeeChatListResponse
import com.wafflestudio.internhasha.coffeeChat.controller.dto.response.CoffeeChatResponse
import com.wafflestudio.internhasha.coffeeChat.service.CoffeeChatService
import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatCreateCommand
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/coffeeChat")
@Validated
class CoffeeChatController(
    private val coffeeChatService: CoffeeChatService,
) {
    // 커피챗 상세 페이지 불러오기
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{coffeeChatId}")
    fun getCoffeeChatDetail(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @PathVariable coffeeChatId: String,
    ): ResponseEntity<CoffeeChatResponse> {
        return ResponseEntity.ok(
            CoffeeChatResponse.fromDto(coffeeChatService.getCoffeeChatDetail(user, coffeeChatId)),
        )
    }

    // 지원자 - 이미 대기 중 커피챗 있는지 확인
    @PreAuthorize("hasAuthority('${RoleValue.ROLE_APPLICANT}')")
    @GetMapping("/{postId}/status")
    fun checkIsSubmitted(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @PathVariable postId: String,
    ): ResponseEntity<CoffeeChatIsSubmitted> {
        return ResponseEntity.ok(
            CoffeeChatIsSubmitted(
                coffeeChatService.checkIsSubmitted(user, postId),
            ),
        )
    }

    // 지원자 - 커피챗 신청하기
    @PreAuthorize("hasAuthority('${RoleValue.ROLE_APPLICANT}')")
    @PostMapping("/{postId}")
    fun postCoffeeChat(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @PathVariable postId: String,
        @RequestBody coffeeChatContent: CoffeeChatContent,
    ): ResponseEntity<CoffeeChatResponse> {
        return ResponseEntity.ok(
            CoffeeChatResponse.fromDto(
                coffeeChatService.applyCoffeeChat(
                    CoffeeChatCreateCommand(
                        applicantId = user.id,
                        positionId = postId,
                        content = coffeeChatContent.content,
                    ),
                ),
            ),
        )
    }

    // 지원자 - 커피챗 수정하기
    @PreAuthorize("hasAuthority('${RoleValue.ROLE_APPLICANT}')")
    @PutMapping("/{coffeeChatId}")
    fun editCoffeeChat(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @PathVariable coffeeChatId: String,
        @RequestBody coffeeChatContent: CoffeeChatContent,
    ): ResponseEntity<CoffeeChatResponse> {
        return ResponseEntity.ok(
            CoffeeChatResponse.fromDto(
                coffeeChatService.editCoffeeChat(
                    user,
                    coffeeChatId,
                    coffeeChatContent,
                ),
            ),
        )
    }

    // 커피챗 상태 변경
    @PreAuthorize("isAuthenticated()")
    @PatchMapping
    fun changeCoffeeChatStatus(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @Valid @RequestBody coffeeChatStatusReq: CoffeeChatStatusReq,
    ): ResponseEntity<CoffeeChatDetailListResponse> {
        return ResponseEntity.ok(
            CoffeeChatDetailListResponse.fromDto(
                coffeeChatService.changeCoffeeChatStatus(
                    user,
                    coffeeChatStatusReq,
                ),
            ),
        )
    }

    // 커피챗 목록 불러오기
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun getCoffeeChatList(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
    ): ResponseEntity<CoffeeChatListResponse> {
        val coffeeChatList = coffeeChatService.getCoffeeChatList(user)
        return ResponseEntity.ok(CoffeeChatListResponse.fromDto(coffeeChatList))
    }

    // 배지 표시 커피챗 개수
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/count")
    fun countCoffeeChatBadges(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
    ): ResponseEntity<CoffeeChatCount> {
        return ResponseEntity.ok(
            CoffeeChatCount(
                num = coffeeChatService.countCoffeeChatBadges(user),
            ),
        )
    }
}
