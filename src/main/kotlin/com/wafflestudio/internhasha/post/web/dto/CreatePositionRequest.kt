package com.wafflestudio.internhasha.post.web.dto

import com.wafflestudio.internhasha.post.application.dto.CreatePositionCommand
import com.wafflestudio.internhasha.post.domain.Category
import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class CreatePositionRequest(
    @field:NotBlank(message = "Position title is required.")
    val positionTitle: String,
    @field:NotNull(message = "Position type (category) is required.")
    val positionType: Category,
    @field:NotBlank(message = "Position detail is required.")
    val detail: String,
    @field:Max(value = 9999, message = "Headcount must not exceed 9999.")
    @field:Min(value = 0, message = "Headcount must be at least 0.")
    val headCount: Long,
    val salary: Long? = null,
    val employmentEndDate: LocalDateTime? = null,
) {
    fun toCommand(companyId: String) =
        CreatePositionCommand(
            positionTitle = this.positionTitle,
            positionType = this.positionType,
            detail = this.detail,
            headCount = this.headCount,
            salary = this.salary,
            employmentEndDate = this.employmentEndDate,
            companyId = companyId,
        )
}

typealias UpdatePositionRequest = CreatePositionRequest
