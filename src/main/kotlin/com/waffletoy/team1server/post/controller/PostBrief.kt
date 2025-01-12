package com.waffletoy.team1server.post.controller

import java.time.LocalDateTime

data class PostBrief(
    val id: String,
    val companyName: String,
    val email: String,
    val author: AuthorBriefDTO,
    val explanation: String,
    val tags: List<String>,
    val roles: List<RoleDTO>,
    val imageLink: String,
    val investAmount: Int,
    val investCompany: String,
    val isActive: Boolean,
    val employmentEndDate: LocalDateTime,
    val slogan: String,
) {
    companion object {
        fun fromPost(post: Post): PostBrief =
            PostBrief(
                id = post.id,
                companyName = post.companyName,
                email = post.email,
                author = post.author,
                explanation = post.explanation,
                tags = post.tags,
                roles = post.roles,
                imageLink = post.imageLink,
                investAmount = post.investAmount,
                investCompany = post.investCompany,
                isActive = post.isActive,
                employmentEndDate = post.employmentEndDate,
                slogan = post.slogan,
            )
    }
}
