package com.wafflestudio.internhasha.post.controller.dto.response

import com.wafflestudio.internhasha.company.domain.Tag
import com.wafflestudio.internhasha.post.service.dto.PostDto
import java.time.LocalDateTime

data class PostBriefResponse(
    val id: String,
    val author: AuthorBriefResponse,
    val companyName: String,
    val profileImageKey: String?,
    val location: String?,
    val employmentEndDate: LocalDateTime?,
    val positionTitle: String,
    val domain: String?,
    val slogan: String?,
    val detailSummary: String,
    val positionType: String,
    val headCount: Long,
    val isBookmarked: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val tags: List<Tag>,
    val coffeeChatCount: Long,
) {
    companion object {
        fun fromPost(postDto: PostDto): PostBriefResponse =
            PostBriefResponse(
                id = postDto.position.id,
                author =
                    AuthorBriefResponse(
                        id = postDto.position.company.id,
                        name = postDto.position.company.companyName,
                        profileImageKey = postDto.position.company.profileImageKey,
                    ),
                companyName = postDto.position.company.companyName,
                profileImageKey = postDto.position.company.profileImageKey,
                location = postDto.position.company.location,
                positionTitle = postDto.position.positionTitle,
                employmentEndDate = postDto.position.employmentEndDate,
                createdAt = postDto.position.createdAt,
                updatedAt = postDto.position.updatedAt,
                positionType = postDto.position.positionType.displayName(),
                headCount = postDto.position.headCount,
                domain =
                    postDto.position.company.domain
                        ?.displayName(),
                slogan = postDto.position.company.slogan,
                detailSummary = postDto.position.detail.take(100),
                isBookmarked = postDto.isBookmarked,
                tags = postDto.position.company.tags,
                coffeeChatCount = postDto.coffeeChatCount,
            )
    }
}
