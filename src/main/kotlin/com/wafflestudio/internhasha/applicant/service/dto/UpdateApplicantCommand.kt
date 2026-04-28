package com.wafflestudio.internhasha.applicant.service.dto

import com.wafflestudio.internhasha.common.dto.Link

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
)
