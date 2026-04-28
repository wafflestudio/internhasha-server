package com.wafflestudio.internhasha.post.service.dto

import com.wafflestudio.internhasha.company.service.dto.CompanyDto
import com.wafflestudio.internhasha.post.domain.Category
import com.wafflestudio.internhasha.post.domain.Position
import java.time.LocalDateTime

data class PositionDto(
    val id: String,
    val company: CompanyDto,
    val positionTitle: String,
    val positionType: Category,
    val headCount: Long,
    val salary: Long? = null,
    val detail: String,
    val employmentEndDate: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(entity: Position) =
            PositionDto(
                id = entity.requireId(),
                company = CompanyDto.fromEntity(entity.company),
                positionTitle = entity.positionTitle,
                positionType = entity.positionType,
                headCount = entity.headCount,
                salary = entity.salary,
                detail = entity.detail,
                employmentEndDate = entity.employmentEndDate,
                createdAt = requireNotNull(entity.createdAt),
                updatedAt = requireNotNull(entity.updatedAt),
            )
    }
}
