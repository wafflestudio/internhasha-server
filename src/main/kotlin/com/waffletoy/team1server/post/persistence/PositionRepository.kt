package com.waffletoy.team1server.post.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PositionRepository : JpaRepository<PositionEntity, String>, JpaSpecificationExecutor<PositionEntity> {
    @Query("SELECT p FROM PositionEntity p WHERE p.id IN :ids")
    fun findAllByIdIn(
        @Param("ids") ids: List<String>,
        pageable: Pageable,
    ): Page<PositionEntity>
}
