package com.wafflestudio.internhasha.post.application.port

import com.wafflestudio.internhasha.coffeeChat.domain.PositionIdAndCoffeeChatCount
import com.wafflestudio.internhasha.post.application.dto.CreatePositionCommand
import com.wafflestudio.internhasha.post.application.dto.UpdatePositionCommand
import com.wafflestudio.internhasha.post.domain.Position
import com.wafflestudio.internhasha.post.persistence.PositionSearchCond
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PositionPersistencePort {
    fun searchPosition(
        cond: PositionSearchCond,
        pageable: Pageable,
    ): Page<Position>

    fun findPositionById(positionId: String): Position?

    fun countCoffeeChatByPositionIds(positionIds: List<String>): List<PositionIdAndCoffeeChatCount>

    fun createPosition(command: CreatePositionCommand): Position

    fun updatePosition(
        positionId: String,
        command: UpdatePositionCommand,
    ): Position
}
