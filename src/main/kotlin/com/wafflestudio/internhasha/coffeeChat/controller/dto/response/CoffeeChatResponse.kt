package com.wafflestudio.internhasha.coffeeChat.controller.dto.response

import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatDto
import java.time.LocalDateTime

data class CoffeeChatResponse(
    val id: String,
    val positionId: String,
    val positionTitle: String,
    val positionCategory: String,
    val company: CoffeeChatUserInfoResponse,
    val applicant: CoffeeChatUserInfoResponse,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val coffeeChatStatus: String,
    var changed: Boolean,
    val content: String,
) {
    companion object {
        fun fromDto(dto: CoffeeChatDto) =
            CoffeeChatResponse(
                id = dto.id,
                positionId = dto.positionId,
                positionTitle = dto.positionTitle,
                positionCategory = dto.positionCategory.displayName(),
                company = CoffeeChatUserInfoResponse.fromDto(dto.company),
                applicant = CoffeeChatUserInfoResponse.fromDto(dto.applicant),
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt,
                coffeeChatStatus = dto.coffeeChatStatus.name,
                changed = dto.changed,
                content = dto.content,
            )
    }
}
