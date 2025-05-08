package com.wafflestudio.internhasha.post.dto

import com.wafflestudio.internhasha.post.Category
import com.wafflestudio.internhasha.post.persistence.PositionEntity
import java.time.LocalDateTime

data class Position(
    val id: String,
    val positionTitle: String,
    val positionType: Category,
    val headCount: Int,
    val salary: Int? = null,
    val detail: String,
    val employmentEndDate: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(entity: PositionEntity): Position {
            return Position(
                id = entity.id,
                positionTitle = entity.positionTitle,
                positionType = entity.positionType,
                headCount = entity.headCount,
                salary = entity.salary,
                detail = entity.detail,
                employmentEndDate = entity.employmentEndDate,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
        }
    }
}
