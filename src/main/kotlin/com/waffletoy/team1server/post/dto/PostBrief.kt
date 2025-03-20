package com.waffletoy.team1server.post.dto

import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.post.Series
import java.time.LocalDateTime

data class PostBrief
    val id: String,
    val author: AuthorBrief,
    val companyName: String, //회사 이름
    val imageLink: String, //회사 사진
    val location: String, //근무지 위치
    val employmentEndDate: LocalDateTime?, //공고 마감일
    val positionTitle: String,
    val isActive: Boolean,
    val domain: String,
    val detail100: String, //공고 내용 앞 100자
    val positionType: String, //직군
    val isBookmarked: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromPost(post: Post): PostBrief =
            PostBrief(
                id = post.id,
                author = post.author,
                companyName = post.company.companyName,
                imageLink = post.company.imageLink,
                location = post.company.location,
                positionTitle = post.position.positionTitle,
                employmentEndDate = post.position.employmentEndDate,
                createdAt = post.position.createdAt,
                updatedAt = post.position.updatedAt,
                isActive = post.position.isActive,
                positionType = post.position.positionType,
                domain = post.company.domain,
                detail100 = post.position.detail.take(100), //첫 100자만 가져오는 kotlin 함수
                isBookmarked = post.isBookmarked,
            )
    }
}
