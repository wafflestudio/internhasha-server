package com.wafflestudio.internhasha.post.web.dto

import com.wafflestudio.internhasha.company.domain.Domain
import com.wafflestudio.internhasha.company.domain.Tag
import com.wafflestudio.internhasha.post.domain.AuthorBrief
import com.wafflestudio.internhasha.post.domain.Category
import com.wafflestudio.internhasha.post.domain.Post
import java.time.LocalDateTime

data class PostBrief(
    val id: String,
    val author: AuthorBrief,
    val companyName: String,
    val profileImageKey: String?,
    val location: String?,
    val employmentEndDate: LocalDateTime?,
    val positionTitle: String,
    val domain: Domain?,
    val slogan: String?,
    val detailSummary: String,
    val positionType: Category,
    val headCount: Long,
    val isBookmarked: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val tags: List<Tag>,
    val coffeeChatCount: Long,
) {
    companion object {
        fun fromPost(post: Post): PostBrief =
            PostBrief(
                id = post.position.id,
                author =
                    AuthorBrief(
                        id = post.position.company.id,
                        name = post.position.company.companyName,
                        profileImageKey = post.position.company.profileImageKey,
                    ),
                companyName = post.position.company.companyName,
                profileImageKey = post.position.company.profileImageKey,
                location = post.position.company.location,
                positionTitle = post.position.positionTitle,
                employmentEndDate = post.position.employmentEndDate,
                createdAt = post.position.createdAt,
                updatedAt = post.position.updatedAt,
                positionType = post.position.positionType,
                headCount = post.position.headCount,
                domain = post.position.company.domain,
                slogan = post.position.company.slogan,
                detailSummary = post.position.detail.take(100),
                isBookmarked = post.isBookmarked,
                tags = post.position.company.tags,
                coffeeChatCount = post.coffeeChatCount,
            )
    }
}
