package com.wafflestudio.internhasha.coffeeChat.controller.dto.response

import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatUserInfo

data class CoffeeChatUserInfoResponse(
    val id: String,
    val name: String,
    val email: String?,
    val profileImageKey: String?,
) {
    companion object {
        fun fromDto(info: CoffeeChatUserInfo) =
            CoffeeChatUserInfoResponse(
                id = info.id,
                name = info.name,
                email = info.email,
                profileImageKey = info.profileImageKey,
            )
    }
}
