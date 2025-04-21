package com.waffletoy.internhasha.post.dto

import com.waffletoy.internhasha.company.dto.Company
import com.waffletoy.internhasha.post.persistence.PositionEntity

data class Post(
    val id: String,
    // 작성자
    val author: AuthorBrief,
    // 회사 정보
    val company: Company,
    // 직군 정보
    val position: Position,
    // 북마크 여부
    val isBookmarked: Boolean = false,
    // 해당 공고에 신청된 커피챗 개수
    val coffeeChatCount: Long,
) {
    companion object {
        fun fromEntity(
            entity: PositionEntity,
            isBookmarked: Boolean = false,
            isLoggedIn: Boolean,
            coffeeChatCount: Long,
        ): Post =
            Post(
                id = entity.id,
                author =
                    AuthorBrief(
                        id = entity.company.user.id,
                        name = entity.company.user.name,
                        profileImageKey = entity.company.profileImageKey,
                    ),
                company = Company.fromEntity(entity.company),
                position = Position.fromEntity(entity),
                isBookmarked = isBookmarked,
                coffeeChatCount = coffeeChatCount,
            )
    }
}
