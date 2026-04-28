package com.wafflestudio.internhasha.post.repository

import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.post.domain.Position
import com.wafflestudio.internhasha.post.service.dto.PositionIdAndCoffeeChatCount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PositionRepository : JpaRepository<Position, String>, QPositionRepository {
    @Query(
        """
        SELECT p FROM Position p
        JOIN FETCH p.company c
        WHERE p.id = :positionId
    """,
    )
    fun findByIdOrNullFetchCompany(positionId: String): Position?

    @Query(
        """
        SELECT
            p.id as positionId,
            count(cc) as coffeeChatCount
        FROM Position p
        LEFT JOIN CoffeeChat cc ON p.id = cc.position.id
        WHERE p.id in :positionIds
        GROUP BY p.id
    """,
    )
    fun countCoffeeChatByPositionIds(positionIds: List<String>): List<PositionIdAndCoffeeChatCount>

    @Query("SELECT p FROM Position p WHERE p.id IN :ids")
    fun findAllByIdIn(
        @Param("ids") ids: List<String>,
        pageable: Pageable,
    ): Page<Position>

    @Query(
        "SELECT p FROM Position p " +
            "JOIN p.company c " +
            "WHERE c.user = :user",
    )
    fun findByUser(
        @Param("user") user: User,
    ): List<Position>
}
