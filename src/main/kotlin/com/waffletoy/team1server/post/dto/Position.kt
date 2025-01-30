package com.waffletoy.team1server.post.dto

import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.post.persistence.PositionEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import java.time.LocalDateTime

data class Position(
    val id: String,
    val title: String,
    val category: String,
    val detail: String? = null,
    val headcount: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val employmentEndDate: LocalDateTime? = null,
    val isActive: Boolean
) {
    companion object {
        fun fromEntity(entity: PositionEntity): Position {
            return Position(
                id = entity.id,
                title = entity.title,
                category = entity.category.displayName(),
                detail = entity.detail,
                headcount = entity.headcount,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                employmentEndDate = entity.employmentEndDate,
                isActive = entity.isActive
            )
        }
    }
}