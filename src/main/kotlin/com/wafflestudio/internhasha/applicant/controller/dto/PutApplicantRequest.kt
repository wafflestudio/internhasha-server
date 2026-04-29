package com.wafflestudio.internhasha.applicant.controller.dto

import com.wafflestudio.internhasha.applicant.service.dto.UpdateApplicantCommand
import com.wafflestudio.internhasha.common.dto.Link
import com.wafflestudio.internhasha.common.dto.LinkRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.*

data class PutApplicantRequest(
    @field:NotNull(message = "enrollYear is required")
    @field:Min(value = 1000, message = "enrollYear must be a valid 4-digit integer")
    @field:Max(value = 9999, message = "enrollYear must be a valid 4-digit integer")
    var enrollYear: Int? = null,
    @field:NotBlank(message = "department is required")
    val department: String,
    val positions: List<String>? = null,
    @field:Size(max = 100, message = "slogan must be at most 100 characters")
    val slogan: String? = null,
    @field:Size(max = 1000, message = "explanation must be at most 1000 characters")
    val explanation: String? = null,
    @field:Size(max = 10, message = "the number of stacks cannot be more than 10")
    val stacks: List<
        @Size(max = 30, message = "Each stack cannot be more than 30 characters")
        String,
    >? = null,
    @field:Size(max = 512, message = "imageKey cannot be more than 512 characters")
    val imageKey: String? = null,
    @field:NotNull(message = "cvKey is required")
    @field:Size(max = 512, message = "cvKey cannot be more than 512 characters")
    var cvKey: String? = null,
    @field:Size(max = 512, message = "portfolioKey cannot be more than 512 characters")
    val portfolioKey: String? = null,
    @field:Size(max = 5, message = "the number of links cannot be more than 5")
    val links: List<@Valid LinkRequest>? = null,
) {
    fun toCommand(applicantId: String) =
        UpdateApplicantCommand(
            applicantId = applicantId,
            enrollYear = this.enrollYear,
            department = this.department,
            slogan = this.slogan,
            positions = this.positions,
            explanation = this.explanation,
            stacks = this.stacks,
            profileImageKey = this.imageKey,
            cvKey = this.cvKey,
            portfolioKey = this.portfolioKey,
            links = this.links?.map { Link(link = it.link, description = it.description) },
        )
}
