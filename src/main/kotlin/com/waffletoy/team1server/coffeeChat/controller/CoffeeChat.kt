package com.waffletoy.team1server.coffeeChat.controller

import com.waffletoy.team1server.coffeeChat.persistence.CoffeeChatEntity
import com.waffletoy.team1server.user.dtos.User
import java.time.LocalDateTime

data class CoffeeChat(
    val id: String,
    val positionTitle: String? = null,
    val companyName: String? = null,
    val companyImageLink: String? = null,
    val author: User,
    val content: String,
    val phoneNumber: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(
            coffeeChatEntity: CoffeeChatEntity,
//            includeAuthor: Boolean = true,
        ): CoffeeChat =
            CoffeeChat(
                id = coffeeChatEntity.id,
                positionTitle = coffeeChatEntity.position?.title,
                companyName = coffeeChatEntity.position?.company?.companyName,
                companyImageLink = coffeeChatEntity.position?.company?.imageLink,
//                author = if (includeAuthor) User.fromEntity(coffeeChatEntity.user, includeCoffeeChats = false) else null,
                author = User.fromEntity(coffeeChatEntity.user),
                content = coffeeChatEntity.content ?: "",
                createdAt = coffeeChatEntity.createdAt,
                phoneNumber = coffeeChatEntity.phoneNumber ?: "",
            )
    }
}
