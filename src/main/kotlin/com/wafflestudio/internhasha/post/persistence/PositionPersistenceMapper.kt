package com.wafflestudio.internhasha.post.persistence

import com.wafflestudio.internhasha.company.persistence.CompanyPersistenceMapper
import com.wafflestudio.internhasha.post.domain.Position
import org.springframework.stereotype.Component

@Component
class PositionPersistenceMapper(
    private val companyMapper: CompanyPersistenceMapper,
) {
    fun toDomain(entity: PositionEntity) =
        Position(
            id = requireNotNull(entity.id) { "PositionEntity id can not be null" },
            company = companyMapper.toCompany(entity.company),
            positionTitle = entity.positionTitle,
            positionType = entity.positionType,
            headCount = entity.headCount,
            salary = entity.salary,
            detail = entity.detail,
            employmentEndDate = entity.employmentEndDate,
            createdAt = requireNotNull(entity.createdAt) { "Timestamp can not be null" },
            updatedAt = requireNotNull(entity.updatedAt) { "Timestamp can not be null" },
        )
}
