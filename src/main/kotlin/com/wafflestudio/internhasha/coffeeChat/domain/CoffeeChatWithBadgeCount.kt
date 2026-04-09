package com.wafflestudio.internhasha.coffeeChat.domain

import java.time.LocalDateTime

class CoffeeChatWithBadgeCount(
    val id: String,
    val postId: String,
    val title: String,
    val company: CoffeeChatUserInfo,
    val applicant: CoffeeChatUserInfo,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val coffeeChatStatus: CoffeeChatStatus,
    var changed: Boolean,
    val content: String,
    val badgeCount: Long,
) {
    companion object {
        fun fromCoffeeChat(
            coffeeChat: CoffeeChat,
            badgeCount: Long,
        ): CoffeeChatWithBadgeCount {
            return CoffeeChatWithBadgeCount(
                id = coffeeChat.id,
                postId = coffeeChat.positionId,
                title = coffeeChat.positionTitle,
                company = coffeeChat.company,
                applicant = coffeeChat.applicant,
                createdAt = coffeeChat.createdAt,
                updatedAt = coffeeChat.updatedAt,
                coffeeChatStatus = coffeeChat.coffeeChatStatus,
                changed = coffeeChat.changed,
                content = coffeeChat.content,
                badgeCount = badgeCount,
            )
        }
    }
}
