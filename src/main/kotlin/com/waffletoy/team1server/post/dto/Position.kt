package com.waffletoy.team1server.post.dto

import com.waffletoy.team1server.post.persistence.PositionEntity
import java.time.LocalDateTime

data class Position(
    val id: String,
    val isActive: Boolean,
    val positionTitle: String, //모집 직무 이름
    val positionType: String, //직무 유형
    val headCount: Int, //모집 인원수
    val salary: Int? = null, //월급, null이면 "추후 협의"로 저장
    val detail: String, //상세 공고 글
    val employmentEndDate: LocalDateTime? = null, //채용 마감일, null이면 "상시모집"
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(entity: PositionEntity): Position {
            return Position(
                id = entity.id,
                isActive = entity.isActive,
                positionTitle = entity.positionTitle,
                positionType = entity.positionType.displayName(),
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
