package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.user.persistence.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BookmarkRepository : JpaRepository<BookmarkEntity, String> {
    fun deleteByUserAndPosition(
        user: UserEntity?,
        role: PositionEntity?,
    )

    fun existsByUserAndPosition(
        user: UserEntity?,
        role: PositionEntity?,
    ): Boolean

    fun findAllByUser(user: UserEntity): List<BookmarkEntity>
}
