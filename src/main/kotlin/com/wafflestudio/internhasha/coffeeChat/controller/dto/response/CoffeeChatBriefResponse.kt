package com.wafflestudio.internhasha.coffeeChat.controller.dto.response

import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatBrief
import java.time.LocalDateTime

data class CoffeeChatBriefResponse(
    val id: String,
    // 공고 ID
    val positionId: String,
    // 공고 포지션
    val positionType: String,
    // 회사 정보
    val company: CoffeeChatUserInfoResponse,
    // 커피챗 생성, 수정 시간
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    // 커피챗 상태
    val coffeeChatStatus: String,
    val changed: Boolean,
    // 지원자 정보
    val applicant: CoffeeChatUserInfoResponse,
) {
    companion object {
        fun fromDto(brief: CoffeeChatBrief) =
            CoffeeChatBriefResponse(
                id = brief.id,
                positionId = brief.positionId,
                positionType = brief.positionType,
                company = CoffeeChatUserInfoResponse.fromDto(brief.company),
                createdAt = brief.createdAt,
                updatedAt = brief.updatedAt,
                coffeeChatStatus = brief.coffeeChatStatus.name,
                changed = brief.changed,
                applicant = CoffeeChatUserInfoResponse.fromDto(brief.applicant),
            )
    }
}
