package com.wafflestudio.internhasha.coffeeChat.controller.dto.request

import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatStatus
import jakarta.validation.constraints.NotEmpty

data class CoffeeChatStatusReq(
    val coffeeChatStatus: CoffeeChatStatus,
    @field:NotEmpty(message = "List cannot not empty")
    val coffeeChatList: List<String>,
)
