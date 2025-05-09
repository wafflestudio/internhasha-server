package com.wafflestudio.internhasha.applicant.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.internhasha.applicant.persistence.ApplicantEntity
import com.wafflestudio.internhasha.auth.UserRole
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApplicantResponse(
    val id: String,
    val name: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val userRole: UserRole,
    var email: String? = null,
    val enrollYear: Int? = null,
    val department: String? = null,
    val positions: List<String>? = null,
    val slogan: String? = null,
    val explanation: String? = null,
    val stacks: List<String>? = null,
    val imageKey: String? = null,
    val cvKey: String,
    val portfolioKey: String? = null,
    val links: List<Link>? = null,
) {
    companion object {
        fun fromEntity(
            entity: ApplicantEntity,
        ) = ApplicantResponse(
            id = entity.user.id,
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
            links = entity.links,
        )
    }
}
