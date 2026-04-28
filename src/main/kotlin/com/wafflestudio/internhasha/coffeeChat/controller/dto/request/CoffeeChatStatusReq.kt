package com.wafflestudio.internhasha.coffeeChat.controller.dto.request

import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatStatus
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CoffeeChatStatusReq(
    @field:NotNull(message = "Status cannot be null.")
    val coffeeChatStatus: CoffeeChatStatus,
    @field:NotEmpty(message = "List cannot not empty")
    val coffeeChatList: List<String>,
)
