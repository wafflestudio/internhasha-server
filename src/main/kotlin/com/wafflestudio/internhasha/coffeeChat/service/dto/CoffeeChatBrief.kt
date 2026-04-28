package com.wafflestudio.internhasha.coffeeChat.service.dto

import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatStatus
import java.time.LocalDateTime

data class CoffeeChatBrief(
    val id: String,
    // 공고 ID
    val positionId: String,
    // 공고 포지션
    val positionType: String,
    // 회사 정보
    val company: CoffeeChatUserInfo,
    // 커피챗 생성, 수정 시간
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    // 커피챗 상태
    val coffeeChatStatus: CoffeeChatStatus,
    val changed: Boolean,
    // 지원자 정보
    val applicant: CoffeeChatUserInfo,
) {
    companion object {
        fun fromDto(
            coffeeChat: CoffeeChatDto,
        ) = CoffeeChatBrief(
            id = coffeeChat.id,
            positionId = coffeeChat.positionId,
            positionType = coffeeChat.positionCategory.displayName(),
            company = coffeeChat.company,
            createdAt = coffeeChat.createdAt,
            updatedAt = coffeeChat.updatedAt,
            coffeeChatStatus = coffeeChat.coffeeChatStatus,
            changed = coffeeChat.changed,
            applicant = coffeeChat.applicant,
        )
    }
}
