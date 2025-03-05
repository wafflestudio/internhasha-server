package com.waffletoy.team1server.coffeeChat.dto

import com.waffletoy.team1server.coffeeChat.CoffeeChatStatus
import com.waffletoy.team1server.coffeeChat.persistence.CoffeeChatEntity
import com.waffletoy.team1server.post.dto.Company
import com.waffletoy.team1server.user.dtos.UserBrief
import java.time.LocalDateTime

data class CoffeeChatApplicant (
    val id: String,

    // 공고 ID
    val postId: String?,

    // 공고 제목
    val title: String?,

    // 회사명, 회사 이미지 url(imageLink)
    val company: UserBrief,

    // 내용
    val content: String?,

    // 커피챗 생성, 수정 시간
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,

    // 커피챗 상태
    val coffeeChatStatus: CoffeeChatStatus,
    val isChanged: Boolean
) {
    companion object {
        fun fromEntity(
            entity: CoffeeChatEntity,
        ) = CoffeeChatApplicant(
            id = entity.id,
            postId = entity.position.id,
            title = entity.position.title,
            company = entity.position.company.curator.let {UserBrief.fromEntity(it)},
            content = entity.content,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            coffeeChatStatus = entity.coffeeChatStatus,
            isChanged = entity.isChanged,
        )
    }
}