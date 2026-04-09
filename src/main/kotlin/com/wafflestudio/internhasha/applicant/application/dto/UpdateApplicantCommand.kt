package com.wafflestudio.internhasha.applicant.application.dto

import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.common.domain.Link

data class UpdateApplicantCommand(
    val applicantId: String,
    val enrollYear: Int?,
    val department: String?,
    val slogan: String?,
    val positions: List<String>?,
    val explanation: String?,
    val stacks: List<String>?,
    val profileImageKey: String?,
    val cvKey: String?,
    val portfolioKey: String?,
    val links: List<Link>?,
) {
    fun toUpdatedDomain(applicant: Applicant): Applicant {
        return applicant.copy(
            enrollYear = this.enrollYear ?: applicant.enrollYear,
            department = this.department ?: applicant.department,
            positions = this.positions ?: applicant.positions,
            slogan = this.slogan ?: applicant.slogan,
            explanation = this.explanation ?: applicant.explanation,
            stacks = this.stacks ?: applicant.stacks,
            profileImageKey = this.profileImageKey ?: applicant.profileImageKey,
            cvKey = this.cvKey ?: applicant.cvKey,
            portfolioKey = this.portfolioKey ?: applicant.portfolioKey,
            links = this.links ?: applicant.links,
        )
    }
}
