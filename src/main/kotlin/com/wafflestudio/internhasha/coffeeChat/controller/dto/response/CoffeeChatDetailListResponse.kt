package com.wafflestudio.internhasha.coffeeChat.controller.dto.response

import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatDetailList

data class CoffeeChatDetailListResponse(
    val succeeded: List<CoffeeChatResponse>,
    val failed: List<CoffeeChatResponse>,
) {
    companion object {
        fun fromDto(dto: CoffeeChatDetailList): CoffeeChatDetailListResponse =
            CoffeeChatDetailListResponse(
                succeeded = dto.succeeded.map { CoffeeChatResponse.fromDto(it) },
                failed = dto.failed.map { CoffeeChatResponse.fromDto(it) },
            )
    }
}
