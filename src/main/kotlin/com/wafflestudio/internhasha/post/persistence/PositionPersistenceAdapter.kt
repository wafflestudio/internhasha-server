package com.wafflestudio.internhasha.post.persistence

import com.wafflestudio.internhasha.coffeeChat.domain.PositionIdAndCoffeeChatCount
import com.wafflestudio.internhasha.company.persistence.CompanyRepository
import com.wafflestudio.internhasha.post.application.dto.CreatePositionCommand
import com.wafflestudio.internhasha.post.application.dto.UpdatePositionCommand
import com.wafflestudio.internhasha.post.application.port.PositionPersistencePort
import com.wafflestudio.internhasha.post.domain.Position
import com.wafflestudio.internhasha.post.domain.PostNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class PositionPersistenceAdapter(
    private val bookmarkRepository: BookmarkRepository,
    private val positionRepository: PositionRepository,
    private val companyRepository: CompanyRepository,
    private val mapper: PositionPersistenceMapper,
) : PositionPersistencePort {
    override fun searchPosition(
        cond: PositionSearchCond,
        pageable: Pageable,
    ): Page<Position> {
        return positionRepository.searchPosition(cond, pageable)
            .map { mapper.toDomain(it) }
    }

    override fun findPositionById(positionId: String): Position? {
        return positionRepository.findByIdOrNull(positionId)?.let { mapper.toDomain(it) }
    }

    override fun countCoffeeChatByPositionIds(positionIds: List<String>): List<PositionIdAndCoffeeChatCount> {
        return positionRepository.countCoffeeChatByPositionIds(positionIds)
    }

    override fun createPosition(command: CreatePositionCommand): Position {
        return positionRepository.save(
            PositionEntity(
                positionTitle = command.positionTitle,
                positionType = command.positionType,
                detail = command.detail,
                headCount = command.headCount,
                employmentEndDate = command.employmentEndDate,
                salary = command.salary,
                companyEntity = companyRepository.getReferenceById(command.companyId),
            ),
        ).let { mapper.toDomain(it) }
    }

    override fun updatePosition(
        positionId: String,
        command: UpdatePositionCommand,
    ): Position {
        val positionEntity =
            positionRepository.findByIdOrNull(positionId)
                ?: throw PostNotFoundException()
        return positionEntity.apply {
            positionTitle = command.positionTitle
            positionType = command.positionType
            detail = command.detail
            headCount = command.headCount
            employmentEndDate = command.employmentEndDate
            salary = command.salary
        }.let { mapper.toDomain(it) }
    }

    fun deleteAllBookmarkByUserId(userId: String) {
        bookmarkRepository.deleteAllByUserId(userId)
    }
}
