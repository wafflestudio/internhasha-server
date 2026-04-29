package com.wafflestudio.internhasha.applicant.controller.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.internhasha.applicant.service.dto.ApplicantDto
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.common.dto.Link
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
        fun fromDto(applicantDto: ApplicantDto) =
            ApplicantResponse(
                id = applicantDto.id,
                name = applicantDto.name,
                createdAt = applicantDto.createdAt,
                updatedAt = applicantDto.updatedAt,
                userRole = applicantDto.userRole,
                email = applicantDto.email,
                enrollYear = applicantDto.enrollYear,
                department = applicantDto.department,
                positions = applicantDto.positions,
                slogan = applicantDto.slogan,
                explanation = applicantDto.explanation,
                stacks = applicantDto.stacks,
                imageKey = applicantDto.imageKey,
                cvKey = applicantDto.cvKey,
                portfolioKey = applicantDto.portfolioKey,
                links = applicantDto.links,
            )
    }
}
