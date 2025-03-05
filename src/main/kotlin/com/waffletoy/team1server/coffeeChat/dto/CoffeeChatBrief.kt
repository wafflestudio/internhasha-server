package com.waffletoy.team1server.coffeeChat.dto

import com.waffletoy.team1server.coffeeChat.CoffeeChatStatus
import com.waffletoy.team1server.coffeeChat.persistence.CoffeeChatEntity
import com.waffletoy.team1server.user.dtos.UserBrief
import java.time.LocalDateTime

data class CoffeeChatBrief(
    val id: String,

    // 공고 ID
    val postId: String,

    // 공고 제목
    val title: String,

    // 지원자 정보
    val applicant: UserBrief,

    // 회사 정보
    val company: UserBrief,

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
        ) = CoffeeChatBrief(
            id = entity.id,
            postId = entity.position.id,
            title = entity.position.title,
            applicant = entity.applicant.let { UserBrief.fromEntity(it) },
            company = entity.position.company.curator.let {UserBrief.fromEntity(it)},
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            coffeeChatStatus = entity.coffeeChatStatus,
            isChanged = entity.isChanged,
        )
    }
}
