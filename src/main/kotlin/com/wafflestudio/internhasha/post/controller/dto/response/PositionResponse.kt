package com.wafflestudio.internhasha.post.controller.dto.response

import com.wafflestudio.internhasha.company.controller.dto.CompanyResponse
import com.wafflestudio.internhasha.post.service.dto.PositionDto
import java.time.LocalDateTime

data class PositionResponse(
    val id: String,
    val company: CompanyResponse,
    val positionTitle: String,
    val positionType: String,
    val headCount: Long,
    val salary: Long? = null,
    val detail: String,
    val employmentEndDate: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDto(position: PositionDto) =
            PositionResponse(
                id = position.id,
                company = CompanyResponse.fromDto(position.company),
                positionTitle = position.positionTitle,
                positionType = position.positionType.displayName(),
                headCount = position.headCount,
                salary = position.salary,
                detail = position.detail,
                employmentEndDate = position.employmentEndDate,
                createdAt = position.createdAt,
                updatedAt = position.updatedAt,
            )
    }
}
