package com.wafflestudio.internhasha.coffeeChat.domain

import com.wafflestudio.internhasha.post.domain.Category
import java.time.LocalDateTime

data class CoffeeChat(
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
)
