package com.wafflestudio.internhasha.coffeeChat.domain

import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatUserInfo
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
)
