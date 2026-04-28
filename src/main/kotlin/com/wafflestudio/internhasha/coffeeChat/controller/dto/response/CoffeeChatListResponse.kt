package com.wafflestudio.internhasha.coffeeChat.controller.dto.response

import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatBrief

data class CoffeeChatListResponse(
    val coffeeChatList: List<CoffeeChatBriefResponse>,
) {
    companion object {
        fun fromDto(list: List<CoffeeChatBrief>) =
            CoffeeChatListResponse(
                list.map { CoffeeChatBriefResponse.fromDto(it) },
            )
    }
}
