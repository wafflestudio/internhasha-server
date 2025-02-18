package com.waffletoy.team1server.coffeeChat.controller

import com.waffletoy.team1server.coffeeChat.service.CoffeeChatService
import com.waffletoy.team1server.user.AuthUser
import com.waffletoy.team1server.user.dtos.User
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/resume")
@Validated
class CoffeeChatController(
    private val coffeeChatService: CoffeeChatService,
) {
    // 커피챗 상세 페이지 불러오기
    @GetMapping("/{resumeId}")
    fun getCoffeeChatDetail(
        @Parameter(hidden = true) @AuthUser user: User,
        @PathVariable resumeId: String,
    ): ResponseEntity<CoffeeChat> {
        return ResponseEntity.ok(
            coffeeChatService.getCoffeeChatDetail(user, resumeId),
        )
    }

    // 커피챗 목록 불러오기
    @GetMapping
    fun getCoffeeChats(
        @Parameter(hidden = true) @AuthUser user: User,
    ): ResponseEntity<CoffeeChats> {
        return ResponseEntity.ok(
            CoffeeChats(
                coffeeChatList = coffeeChatService.getCoffeeChats(user),
            ),
        )
    }

    // 커피챗 신청하기
    @PostMapping("/{postId}")
    fun postCoffeeChat(
        @Parameter(hidden = true) @AuthUser user: User,
        @PathVariable postId: String,
        @RequestBody coffee: Coffee,
    ): ResponseEntity<CoffeeChat> {
        val coffeeChat =
            coffeeChatService.postCoffeeChat(
                user,
                postId,
                coffee,
            )
        return ResponseEntity.ok(coffeeChat)
    }

    // 커피챗 삭제하기
    @DeleteMapping("/{resumeId}")
    fun deleteCoffeeChat(
        @Parameter(hidden = true) @AuthUser user: User,
        @PathVariable resumeId: String,
    ): ResponseEntity<Void> {
        coffeeChatService.deleteCoffeeChat(user, resumeId)
        return ResponseEntity.ok().build()
    }

    // 커피챗 수정하기
    @PatchMapping("/{resumeId}")
    fun patchCoffeeChat(
        @Parameter(hidden = true) @AuthUser user: User,
        @PathVariable resumeId: String,
        @RequestBody coffee: Coffee,
    ): ResponseEntity<CoffeeChat> {
        val updatedCoffeeChat = coffeeChatService.patchCoffeeChat(user, resumeId, coffee)
        return ResponseEntity.ok(updatedCoffeeChat)
    }
}

data class CoffeeChats(
    val coffeeChatList: List<CoffeeChat>,
)

data class Coffee(
    @field:NotBlank(message = "Phone number cannot be blank.")
    @field:Size(max = 20, message = "Phone number cannot exceed 20 characters.")
    val phoneNumber: String,
    @field:NotBlank(message = "Content cannot be blank.")
    val content: String,
)
