package com.wafflestudio.internhasha.applicant.service.dto

import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.common.dto.Link
import java.time.LocalDateTime

data class ApplicantDto(
    val id: String,
    val name: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val userRole: UserRole,
    val email: String,
    val enrollYear: Int?,
    val department: String?,
    val positions: List<String>,
    val slogan: String?,
    val explanation: String?,
    val stacks: List<String>,
    val imageKey: String?,
    val cvKey: String?,
    val portfolioKey: String,
    val links: List<Link>,
) {
    companion object {
        fun fromEntity(applicant: Applicant) =
            ApplicantDto(
                id = applicant.requireId(),
                name = applicant.name,
                createdAt = requireNotNull(applicant.createdAt),
                updatedAt = requireNotNull(applicant.updatedAt),
                userRole = applicant.getRole(),
                email = applicant.email,
                enrollYear = applicant.enrollYear,
                department = applicant.dept,
                positions = applicant.positions.toList(),
                slogan = applicant.slogan,
                explanation = applicant.explanation,
                stacks = applicant.stacks.toList(),
                imageKey = applicant.profileImageKey,
                cvKey = applicant.cvKey,
                portfolioKey = requireNotNull(applicant.portfolioKey),
                links = applicant.links.map { Link(link = it.link, description = it.description) },
            )
    }
}
