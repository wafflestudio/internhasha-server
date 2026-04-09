package com.wafflestudio.internhasha.post.web.dto

import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.post.domain.Category
import com.wafflestudio.internhasha.post.domain.Post
import java.time.LocalDateTime

data class PostResponse(
    val id: String,
    // 회사 정보
    val company: Company,
    // 직군 정보
    val position: PositionResponse,
    // 북마크 여부
    val isBookmarked: Boolean = false,
    // 해당 공고에 신청된 커피챗 개수
    val coffeeChatCount: Long,
) {
    companion object {
        fun fromPost(post: Post): PostResponse {
            return PostResponse(
                id = post.position.id,
                company = post.position.company,
                position =
                    PositionResponse(
                        positionTitle = post.position.positionTitle,
                        positionType = post.position.positionType,
                        headCount = post.position.headCount,
                        salary = post.position.salary,
                        detail = post.position.detail,
                        employmentEndDate = post.position.employmentEndDate,
                        createdAt = post.position.createdAt,
                        updatedAt = post.position.updatedAt,
                    ),
                isBookmarked = post.isBookmarked,
                coffeeChatCount = post.coffeeChatCount,
            )
        }
    }
}

data class PositionResponse(
    val positionTitle: String,
    val positionType: Category,
    val headCount: Long,
    val salary: Long? = null,
    val detail: String,
    val employmentEndDate: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
