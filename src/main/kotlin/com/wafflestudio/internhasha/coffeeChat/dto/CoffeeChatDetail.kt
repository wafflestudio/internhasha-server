package com.wafflestudio.internhasha.coffeeChat.dto

import com.wafflestudio.internhasha.applicant.dto.ApplicantResponse
import com.wafflestudio.internhasha.coffeeChat.CoffeeChatStatus
import com.wafflestudio.internhasha.coffeeChat.persistence.CoffeeChatEntity
import java.time.LocalDateTime

sealed class CoffeeChatDetail

data class CoffeeChatApplicant(
    val id: String,
    // 공고 ID
    val postId: String,
    // 공고 제목
    val title: String,
    // 회사명, 회사 이미지 url
    val company: CoffeeChatUserInfo,
    // 커피챗 생성, 수정 시간
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    // 커피챗 상태
    val coffeeChatStatus: CoffeeChatStatus,
    var changed: Boolean,
    // 내용
    val content: String,
) : CoffeeChatDetail() {
    companion object {
        fun fromEntity(
            entity: CoffeeChatEntity,
        ) = CoffeeChatApplicant(
            id = entity.id,
            postId = entity.position.id,
            title = entity.position.positionTitle,
            company = entity.position.company.user.let { CoffeeChatUserInfo.fromEntity(it) },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            coffeeChatStatus = entity.coffeeChatStatus,
            changed = entity.changed,
            content = entity.content,
        )
    }
}

data class CoffeeChatCompany(
    val id: String,
    // 공고 ID
    val postId: String,
    // 공고 제목
    val title: String,
    // 회사명, 회사 이미지 url(imageLink)
    val company: CoffeeChatUserInfo,
    // 커피챗 생성, 수정 시간
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    // 커피챗 상태
    val coffeeChatStatus: CoffeeChatStatus,
    var changed: Boolean,
    // 내용
    val content: String,
    // 지원자 정보
    val applicant: ApplicantResponse,
) : CoffeeChatDetail() {
    companion object {
        fun fromEntity(
            entity: CoffeeChatEntity,
        ): CoffeeChatCompany {
            val applicantResponse = entity.applicant.applicant?.let { ApplicantResponse.fromEntity(it) }
                ?: ApplicantResponse(
                    id = entity.applicant.id,
                    name = entity.applicant.name,
                    createdAt = entity.applicant.createdAt,
                    updatedAt = entity.applicant.updatedAt,
                    userRole = entity.applicant.userRole,
                    email = entity.applicant.email,
                )

            // 성사 조건에 따라 이메일 필터링
            if (entity.coffeeChatStatus != CoffeeChatStatus.ACCEPTED) {
                applicantResponse.email = null
            }

            return CoffeeChatCompany(
                id = entity.id,
                postId = entity.position.id,
                title = entity.position.positionTitle,
                company = entity.position.company.user.let { CoffeeChatUserInfo.fromEntity(it) },
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                coffeeChatStatus = entity.coffeeChatStatus,
                changed = entity.changed,
                content = entity.content,
                applicant = applicantResponse,
            )
        }
    }
}
