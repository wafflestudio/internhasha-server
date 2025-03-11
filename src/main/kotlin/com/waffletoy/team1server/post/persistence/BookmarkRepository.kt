package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.auth.persistence.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BookmarkRepository : JpaRepository<BookmarkEntity, String> {
    fun findByUserAndPosition(
        user: UserEntity,
        position: PositionEntity,
    ): BookmarkEntity?

    fun findByUser(user: UserEntity): List<BookmarkEntity>

    @Query("SELECT b.position FROM BookmarkEntity b WHERE b.user = :user")
    fun findPositionsByUser(
        @Param("user") user: UserEntity,
        pageable: Pageable,
    ): Page<PositionEntity>

    fun deleteAllByUser(user: UserEntity)
}
