package com.waffletoy.team1server.post.controller

import com.waffletoy.team1server.post.persistence.PostEntity
import java.time.LocalDateTime

data class Post(
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
    val IRDeckLink: String,
    val landingPageLink: String,
    val externalDescriptionLink: List<String>,
    val isActive: Boolean,
    val employeeEndDate: LocalDateTime,
) {
    companion object {
        fun fromEntity(entity: PostEntity): Post =
            Post(
                id = entity.id,
                companyName = entity.companyName,
                email = entity.email ?: "",
                author =
                    AuthorBriefDTO(
                        id = entity.admin.id,
                        name = entity.admin.username,
                        profileImageLink = entity.admin.profileImageLink,
                    ),
                explanation = entity.explanation ?: "",
                tags = entity.tags.map { it.tag },
                roles =
                    entity.roles.map {
                        RoleDTO(
                            id = it.id,
                            category = it.category,
                            detail = it.detail,
                            headcount = it.headcount,
                        )
                    },
                imageLink = entity.imageLink ?: "",
                investAmount = entity.investAmount,
                investCompany = entity.investCompany ?: "",
                isActive = entity.isActive,
                IRDeckLink = entity.irDeckLink ?: "",
                landingPageLink = entity.landingPageLink ?: "",
                externalDescriptionLink = entity.links.map { it.link },
                employeeEndDate = entity.employmentEndDate,
            )
    }
}
