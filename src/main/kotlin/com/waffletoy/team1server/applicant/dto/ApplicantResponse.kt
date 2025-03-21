package com.waffletoy.team1server.applicant.dto

import com.waffletoy.team1server.applicant.persistence.ApplicantEntity
import com.waffletoy.team1server.auth.UserRole
import com.waffletoy.team1server.auth.dto.User
import java.time.LocalDateTime

data class ApplicantResponse(
    val name: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val userRole: UserRole,
    val email: String,
    val enrollYear: Int? = null,
    val department: String? = null,
    val positions: List<String>? = null, // JobCategory로 E 바꿀 것
    val slogan: String? = null,
    val explanation: String? = null,
    val stacks: List<String>? = null,
    val imageKey: String? = null,
    val cvKey: String? = null,
    val portfolioKey: String? = null,
    val links: List<Link>? = null,
) {
    companion object {
        fun fromEntity(
            entity: ApplicantEntity,
        ) = ApplicantResponse(
            name = entity.user.name,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            userRole = entity.userRole,
            email = entity.user.email,
            enrollYear = entity.enrollYear,
            department = entity.dept,
            positions = entity.positions,
            slogan = entity.slogan,
            explanation = entity.explanation,
            stacks = entity.stacks,
            imageKey = entity.profileImageKey,
            cvKey = entity.cvKey,
            portfolioKey = entity.portfolioKey,
            links = entity.links
        )
    }
}

