package com.wafflestudio.internhasha.post.controller.dto.response

import com.wafflestudio.internhasha.company.controller.dto.CompanyResponse
import com.wafflestudio.internhasha.post.service.dto.PostDto
import java.time.LocalDateTime

data class PostResponse(
    val id: String,
    // 회사 정보
    val company: CompanyResponse,
    // 직군 정보
    val position: PositionInPost,
    // 북마크 여부
    val bookmarked: Boolean = false,
    // 해당 공고에 신청된 커피챗 개수
    val coffeeChatCount: Long,
) {
    companion object {
        fun fromDto(postDto: PostDto): PostResponse =
            PostResponse(
                id = postDto.position.id,
                company = CompanyResponse.fromDto(postDto.position.company),
                position =
                    PositionInPost(
                        positionTitle = postDto.position.positionTitle,
                        positionType = postDto.position.positionType.displayName(),
                        headCount = postDto.position.headCount,
                        salary = postDto.position.salary,
                        detail = postDto.position.detail,
                        employmentEndDate = postDto.position.employmentEndDate,
                        createdAt = postDto.position.createdAt,
                        updatedAt = postDto.position.updatedAt,
                    ),
                bookmarked = postDto.bookmarked,
                coffeeChatCount = postDto.coffeeChatCount,
            )
    }
}

data class PositionInPost(
    val positionTitle: String,
    val positionType: String,
    val headCount: Long,
    val salary: Long? = null,
    val detail: String,
    val employmentEndDate: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
