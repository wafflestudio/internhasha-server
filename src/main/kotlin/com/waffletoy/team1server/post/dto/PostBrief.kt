package com.waffletoy.team1server.post.dto

import com.waffletoy.team1server.company.Domain
import com.waffletoy.team1server.company.dto.Tag
import com.waffletoy.team1server.post.Category
import java.time.LocalDateTime

data class PostBrief(
    val id: String,
    val author: AuthorBrief,
    val companyName: String,
    val profileImageKey: String?,
    val location: String?,
    val employmentEndDate: LocalDateTime?,
    val positionTitle: String,
    val isActive: Boolean,
    val domain: Domain?,
    val detail100: String,
    val positionType: Category,
    val isBookmarked: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val tags: List<Tag>,
) {
    companion object {
        fun fromPost(post: Post): PostBrief =
            PostBrief(
                id = post.id,
                author = post.author,
                companyName = post.company.companyName,
                profileImageKey = post.company.profileImageKey,
                location = post.company.location,
                positionTitle = post.position.positionTitle,
                employmentEndDate = post.position.employmentEndDate,
                createdAt = post.position.createdAt,
                updatedAt = post.position.updatedAt,
                isActive = post.position.isActive,
                positionType = post.position.positionType,
                domain = post.company.domain,
                detail100 = post.position.detail.take(100),
                isBookmarked = post.isBookmarked,
                tags = post.company.tags,
            )
    }
}
