package com.waffletoy.team1server.post.dto

import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.post.Series
import com.waffletoy.team1server.post.persistence.PositionEntity
import java.time.LocalDateTime

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
) {
    companion object {
        fun fromEntity(
            entity: PositionEntity,
            isBookmarked: Boolean = false,
            isLoggedIn: Boolean,
        ): Post =
            Post(
                id = entity.id,
                author =
                AuthorBrief(
                    id = entity.company.company.id,
                    name = entity.company.company.name,
                    profileImageLink = entity.company.company.profileImageLink,
                ),
                company = Company.fromEntity(entity.company),
                position = Position.fromEntity(entity),
                isBookmarked = isBookmarked,
            )
    }
}
