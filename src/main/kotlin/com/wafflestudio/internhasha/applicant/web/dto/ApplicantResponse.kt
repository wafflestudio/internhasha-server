package com.wafflestudio.internhasha.applicant.web.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.common.domain.Link
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApplicantResponse(
    val id: String,
    val name: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val userRole: UserRole,
    val email: String? = null,
    val enrollYear: Int? = null,
    val department: String? = null,
    val positions: List<String>? = null,
    val slogan: String? = null,
    val explanation: String? = null,
    val stacks: List<String>? = null,
    val imageKey: String? = null,
    val cvKey: String? = null,
    val portfolioKey: String? = null,
    val links: List<Link>? = null,
) {
    companion object {
        fun fromApplicant(applicant: Applicant) =
            ApplicantResponse(
                id = applicant.id,
                name = applicant.name,
                createdAt = applicant.createdAt,
                updatedAt = applicant.updatedAt,
                userRole = applicant.userRole,
                email = applicant.email,
                enrollYear = applicant.enrollYear,
                department = applicant.department,
                positions = applicant.positions,
                slogan = applicant.slogan,
                explanation = applicant.explanation,
                stacks = applicant.stacks,
                imageKey = applicant.profileImageKey,
                cvKey = applicant.cvKey,
                portfolioKey = applicant.portfolioKey,
                links = applicant.links,
            )
    }
}
