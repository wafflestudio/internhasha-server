package com.wafflestudio.internhasha.post.persistence

import com.wafflestudio.internhasha.auth.persistence.UserEntity
import com.wafflestudio.internhasha.coffeeChat.domain.PositionIdAndCoffeeChatCount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PositionRepository : JpaRepository<PositionEntity, String>, QPositionRepository {
    @Query(
        """
        SELECT
            p.id as positionId,
            count(cc) as coffeeChatCount
        FROM PositionEntity p
        LEFT JOIN CoffeeChatEntity cc ON p.id = cc.position.id
        WHERE p.id in :positionIds
        GROUP BY p.id
    """,
    )
    fun countCoffeeChatByPositionIds(positionIds: List<String>): List<PositionIdAndCoffeeChatCount>

    @Query("SELECT p FROM PositionEntity p WHERE p.id IN :ids")
    fun findAllByIdIn(
        @Param("ids") ids: List<String>,
        pageable: Pageable,
    ): Page<PositionEntity>

    @Query(
        "SELECT p FROM PositionEntity p " +
            "JOIN p.company c " +
            "WHERE c.user = :user",
    )
    fun findByUser(
        @Param("user") user: UserEntity,
    ): List<PositionEntity>
}
