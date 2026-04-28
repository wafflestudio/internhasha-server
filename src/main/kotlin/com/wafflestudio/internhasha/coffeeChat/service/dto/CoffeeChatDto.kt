package com.wafflestudio.internhasha.coffeeChat.service.dto

import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChat
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatStatus
import com.wafflestudio.internhasha.post.domain.Category
import java.time.LocalDateTime

data class CoffeeChatDto(
    val id: String,
    val positionId: String,
    val positionTitle: String,
    val positionCategory: Category,
    val company: CoffeeChatUserInfo,
    val applicant: CoffeeChatUserInfo,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val coffeeChatStatus: CoffeeChatStatus,
    var changed: Boolean,
    val content: String,
) {
    companion object {
        fun fromEntity(entity: CoffeeChat): CoffeeChatDto {
            return CoffeeChatDto(
                id = requireNotNull(entity.id) { "CoffeeChatEntity id must not be null" },
                positionId = requireNotNull(entity.position.id) { "PositionEntity id must not be null" },
                positionTitle = entity.position.positionTitle,
                positionCategory = entity.position.positionType,
                company = entity.position.company.let { CoffeeChatUserInfo.fromEntity(it) },
                createdAt = requireNotNull(entity.createdAt) { "Timestamp must not be null" },
                updatedAt = requireNotNull(entity.updatedAt) { "Timestamp must not be null" },
                coffeeChatStatus = entity.coffeeChatStatus,
                changed = entity.changed,
                content = entity.content,
                applicant = entity.applicant.let { CoffeeChatUserInfo.fromEntity(it) },
            )
        }
    }
}
